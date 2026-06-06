package com.elykia.octopus.core.data.repository

import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.common.DispatchersProvider
import com.elykia.octopus.core.data.model.ImportResult
import com.elykia.octopus.core.data.remote.NetworkExecutor
import com.elykia.octopus.core.data.remote.OctopusApiService
import com.elykia.octopus.core.data.remote.readUtf8Limited
import com.elykia.octopus.core.data.remote.sanitizeErrorMessage
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataTransferRepository @Inject constructor(
    private val apiService: OctopusApiService,
    private val executor: NetworkExecutor,
    private val dispatchers: DispatchersProvider,
) {
    suspend fun exportData(
        includeLogs: Boolean = false,
        includeStats: Boolean = false,
    ): AppResult<ByteArray> = withContext(dispatchers.io) {
        try {
            val response = apiService.exportData(includeLogs = includeLogs, includeStats = includeStats)
            if (!response.isSuccessful) {
                val message = response.errorBody()
                    ?.readUtf8Limited(EXPORT_ERROR_BODY_BYTES)
                    ?.takeIf { it.isNotBlank() }
                    ?.sanitizeErrorMessage()
                    ?: "导出失败，状态码：${response.code()}"
                return@withContext AppResult.Error(message)
            }

            val body = response.body() ?: return@withContext AppResult.Error("导出数据为空。")
            val bytes = try {
                body.readBytesLimited(MAX_EXPORT_FILE_BYTES)
            } catch (exception: ExportPayloadTooLargeException) {
                return@withContext AppResult.Error("导出文件过大，请在服务端缩小导出范围后重试。")
            }

            when {
                bytes.isEmpty() -> AppResult.Error("导出数据为空。")
                else -> when (val check = bytes.checkExportPayload()) {
                    ExportPayloadCheck.Safe -> AppResult.Success(bytes)
                    ExportPayloadCheck.InvalidJson -> AppResult.Error("导出数据不是有效的 JSON，已拒绝保存。")
                    is ExportPayloadCheck.SensitiveField -> AppResult.Error("导出数据包含敏感字段 ${check.path}，已拒绝保存。")
                }
            }
        } catch (exception: IOException) {
            AppResult.Error((exception.message ?: "导出失败。").sanitizeErrorMessage(), exception)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            AppResult.Error((exception.message ?: "导出失败。").sanitizeErrorMessage(), exception)
        }
    }

    suspend fun importData(
        fileName: String,
        content: ByteArray,
    ): AppResult<ImportResult> = withContext(dispatchers.io) {
        if (content.isEmpty()) {
            return@withContext AppResult.Error("导入文件为空。")
        }
        if (content.size > MAX_IMPORT_FILE_BYTES) {
            return@withContext AppResult.Error("导入文件过大，请选择 20 MB 以内的文件。")
        }
        if (!content.isValidJsonPayload()) {
            return@withContext AppResult.Error("导入文件不是有效的 JSON。")
        }

        val part = MultipartBody.Part.createFormData(
            name = "file",
            filename = fileName.sanitizeImportFileName(),
            body = content.toRequestBody(IMPORT_JSON_MEDIA_TYPE),
        )
        executor.execute { apiService.importData(part) }
    }
}

internal sealed class ExportPayloadCheck {
    data object Safe : ExportPayloadCheck()
    data object InvalidJson : ExportPayloadCheck()
    data class SensitiveField(val path: String) : ExportPayloadCheck()
}

internal fun ByteArray.checkExportPayload(): ExportPayloadCheck {
    val payload = runCatching {
        EXPORT_JSON.parseToJsonElement(decodeToString())
    }.getOrElse { exception ->
        if (exception is SerializationException || exception is IllegalArgumentException) {
            return ExportPayloadCheck.InvalidJson
        }
        return ExportPayloadCheck.InvalidJson
    }

    return payload.findSensitiveExportFieldPath()
        ?.let(ExportPayloadCheck::SensitiveField)
        ?: ExportPayloadCheck.Safe
}

internal fun String.sanitizeImportFileName(): String {
    val sanitized = replace(IMPORT_FILE_NAME_UNSAFE_PATTERN, "_")
        .trim()
        .take(MAX_IMPORT_FILE_NAME_LENGTH)

    return sanitized
        .takeUnless { it.isBlank() || it == "." || it == ".." }
        ?: DEFAULT_IMPORT_FILE_NAME
}

internal fun ByteArray.isValidJsonPayload(): Boolean =
    runCatching {
        EXPORT_JSON.parseToJsonElement(decodeToString())
    }.getOrNull()
        ?.let { it is JsonObject || it is JsonArray }
        ?: false

private fun ResponseBody.readBytesLimited(maxBytes: Int): ByteArray {
    val declaredLength = contentLength()
    if (declaredLength > maxBytes) {
        throw ExportPayloadTooLargeException()
    }

    return use { body ->
        val output = ByteArrayOutputStream()
        body.byteStream().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var total = 0
            while (true) {
                val read = input.read(buffer)
                if (read == -1) break
                total += read
                if (total > maxBytes) {
                    throw ExportPayloadTooLargeException()
                }
                output.write(buffer, 0, read)
            }
        }
        output.toByteArray()
    }
}

private fun JsonElement.findSensitiveExportFieldPath(path: String = "$"): String? = when (this) {
    is JsonObject -> {
        entries.firstNotNullOfOrNull { (key, _) ->
            val nextPath = "$path.$key"
            if (key.isSensitiveExportField()) {
                nextPath
            } else {
                null
            }
        } ?: findSensitiveHeaderValuePath(path)
            ?: entries.firstNotNullOfOrNull { (key, value) ->
                value.findSensitiveExportFieldPath("$path.$key")
            }
    }
    is JsonArray -> {
        mapIndexedNotNull { index, value ->
            value.findSensitiveExportFieldPath("$path[$index]")
        }.firstOrNull()
    }
    is JsonPrimitive -> {
        if (contentOrNull?.hasSensitiveExportValue() == true) path else null
    }
}

private fun String.isSensitiveExportField(): Boolean {
    val normalized = filter { it.isLetterOrDigit() }.lowercase()
    return normalized in SENSITIVE_EXPORT_FIELDS ||
        normalized.endsWith("apikey") ||
        normalized.endsWith("channelkey") ||
        normalized.endsWith("accesskey") ||
        normalized.endsWith("privatekey") ||
        normalized.endsWith("secretkey") ||
        (normalized.endsWith("token") && normalized !in NON_SECRET_EXPORT_TOKEN_FIELDS) ||
        normalized.endsWith("secret") ||
        normalized.endsWith("password")
}

private fun JsonObject.findSensitiveHeaderValuePath(path: String): String? {
    val hasSensitiveHeaderKey = entries.any { (key, value) ->
        key.normalizedExportKey() in HEADER_KEY_FIELDS &&
            (value as? JsonPrimitive)?.contentOrNull?.isSensitiveHeaderName() == true
    }
    if (!hasSensitiveHeaderKey) {
        return null
    }

    return entries.firstNotNullOfOrNull { (key, _) ->
        if (key.normalizedExportKey() in HEADER_VALUE_FIELDS) "$path.$key" else null
    } ?: path
}

private fun String.hasSensitiveExportValue(): Boolean =
    EXPORT_AUTHORIZATION_PATTERN.containsMatchIn(this) ||
        EXPORT_BEARER_PATTERN.containsMatchIn(this) ||
        EXPORT_API_KEY_PATTERN.containsMatchIn(this) ||
        EXPORT_JWT_PATTERN.containsMatchIn(this) ||
        EXPORT_PEM_PRIVATE_KEY_PATTERN.containsMatchIn(this) ||
        EXPORT_SENSITIVE_QUERY_PATTERN.containsMatchIn(this)

private fun String.isSensitiveHeaderName(): Boolean =
    normalizedExportKey() in SENSITIVE_HEADER_NAMES

private fun String.normalizedExportKey(): String =
    filter { it.isLetterOrDigit() }.lowercase()

private val EXPORT_JSON = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

private val SENSITIVE_EXPORT_FIELDS = setOf(
    "apikey",
    "xapikey",
    "channelkey",
    "token",
    "accesstoken",
    "refreshtoken",
    "authorization",
    "accesskey",
    "secret",
    "secretkey",
    "clientsecret",
    "privatekey",
    "password",
    "passwd",
)

private val NON_SECRET_EXPORT_TOKEN_FIELDS = setOf(
    "inputtoken",
    "outputtoken",
    "totaltoken",
    "totaltokens",
    "prompttoken",
    "prompttokens",
    "completiontoken",
    "completiontokens",
)

private val HEADER_KEY_FIELDS = setOf("headerkey", "key", "name")
private val HEADER_VALUE_FIELDS = setOf("headervalue", "value")
private val SENSITIVE_HEADER_NAMES = setOf(
    "authorization",
    "xapikey",
    "apikey",
    "channelkey",
)

private val EXPORT_AUTHORIZATION_PATTERN = Regex(
    pattern = """(?i)\bauthorization\s*[:=]\s*(bearer\s+)?[^\s,;}\]]+""",
)
private val EXPORT_BEARER_PATTERN = Regex(
    pattern = """(?i)\bBearer\s+[A-Za-z0-9._~+/\-=]+""",
)
private val EXPORT_API_KEY_PATTERN = Regex(
    pattern = """\bsk-[A-Za-z0-9_-]{3,}\b""",
)
private val EXPORT_JWT_PATTERN = Regex(
    pattern = """\b[A-Za-z0-9_-]{20,}\.[A-Za-z0-9_-]{20,}\.[A-Za-z0-9_-]{10,}\b""",
)
private val EXPORT_PEM_PRIVATE_KEY_PATTERN = Regex(
    pattern = """-----BEGIN\s+(?:[A-Z]+\s+)?PRIVATE\s+KEY-----""",
    option = RegexOption.IGNORE_CASE,
)
private val EXPORT_SENSITIVE_QUERY_PATTERN = Regex(
    pattern = """(?i)\b(?:api[_-]?key|x[_-]?api[_-]?key|channel[_-]?key|access[_-]?key|private[_-]?key|secret[_-]?key|client[_-]?private[_-]?key|access[_-]?token|refresh[_-]?token|token|password|passwd|secret|client[_-]?secret)=([^&\s]+)""",
)

private const val MAX_EXPORT_FILE_BYTES = 20 * 1024 * 1024
internal const val MAX_IMPORT_FILE_BYTES = 20 * 1024 * 1024
private const val MAX_IMPORT_FILE_NAME_LENGTH = 80
private const val EXPORT_ERROR_BODY_BYTES = 64 * 1024
private const val DEFAULT_IMPORT_FILE_NAME = "octopus-import.json"

private val IMPORT_FILE_NAME_UNSAFE_PATTERN = Regex("""[\\/:*?"<>|\p{Cntrl}]""")
private val IMPORT_JSON_MEDIA_TYPE = "application/json".toMediaType()

private class ExportPayloadTooLargeException : IOException()
