package com.elykia.octopus.core.data.repository

import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.common.DispatchersProvider
import com.elykia.octopus.core.data.remote.DataTransferApiService
import com.elykia.octopus.core.data.remote.NetworkExecutor
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Test
import java.lang.reflect.Proxy
import java.util.concurrent.atomic.AtomicBoolean

class ImportPayloadCheckTest {
    @Test
    fun importFileNameReplacesPathSeparatorsAndUnsafeCharacters() {
        val fileName = """..\evil/path:*?"<>|file.json"""

        val sanitized = fileName.sanitizeImportFileName()

        assertThat(sanitized).isEqualTo(".._evil_path_______file.json")
        assertThat(sanitized).doesNotContain("\\")
        assertThat(sanitized).doesNotContain("/")
        assertThat(sanitized).doesNotContain(":")
        assertThat(sanitized).doesNotContain("*")
        assertThat(sanitized).doesNotContain("?")
        assertThat(sanitized).doesNotContain("\"")
        assertThat(sanitized).doesNotContain("<")
        assertThat(sanitized).doesNotContain(">")
        assertThat(sanitized).doesNotContain("|")
    }

    @Test
    fun importFileNameFallsBackForBlankAndDotNames() {
        assertThat("   ".sanitizeImportFileName()).isEqualTo("octopus-import.json")
        assertThat(".".sanitizeImportFileName()).isEqualTo("octopus-import.json")
        assertThat("..".sanitizeImportFileName()).isEqualTo("octopus-import.json")
    }

    @Test
    fun importFileNameLimitsLength() {
        val sanitized = ("a".repeat(120) + ".json").sanitizeImportFileName()

        assertThat(sanitized).hasLength(80)
    }

    @Test
    fun importPayloadAcceptsValidJson() {
        assertThat("""{"settings":[]}""".encodeToByteArray().isValidJsonPayload()).isTrue()
        assertThat("""[]""".encodeToByteArray().isValidJsonPayload()).isTrue()
    }

    @Test
    fun importPayloadRejectsInvalidJson() {
        assertThat("not json".encodeToByteArray().isValidJsonPayload()).isFalse()
    }

    @Test
    fun importPayloadRejectsJsonPrimitive() {
        assertThat("123".encodeToByteArray().isValidJsonPayload()).isFalse()
        assertThat(""""not a backup"""".encodeToByteArray().isValidJsonPayload()).isFalse()
    }

    @Test
    fun importDataRejectsOversizedContentBeforeCallingService() = runBlocking {
        val serviceCalled = AtomicBoolean(false)
        val repository = DataTransferRepository(
            apiService = failingService { serviceCalled.set(true) },
            executor = NetworkExecutor(
                Json {
                    ignoreUnknownKeys = true
                    explicitNulls = false
                    isLenient = true
                }
            ),
            dispatchers = DispatchersProvider(),
        )

        val result = repository.importData(
            fileName = "settings.json",
            content = ByteArray(MAX_IMPORT_FILE_BYTES + 1),
        )

        assertThat(result).isInstanceOf(AppResult.Error::class.java)
        assertThat((result as AppResult.Error).message).isEqualTo("导入文件过大，请选择 20 MB 以内的文件。")
        assertThat(serviceCalled.get()).isFalse()
    }

    @Test
    fun importDataRejectsInvalidJsonBeforeCallingService() = runBlocking {
        val serviceCalled = AtomicBoolean(false)
        val repository = DataTransferRepository(
            apiService = failingService { serviceCalled.set(true) },
            executor = NetworkExecutor(
                Json {
                    ignoreUnknownKeys = true
                    explicitNulls = false
                    isLenient = true
                }
            ),
            dispatchers = DispatchersProvider(),
        )

        val result = repository.importData(
            fileName = "settings.json",
            content = "not json".encodeToByteArray(),
        )

        assertThat(result).isInstanceOf(AppResult.Error::class.java)
        assertThat((result as AppResult.Error).message).isEqualTo("导入文件不是有效的 JSON。")
        assertThat(serviceCalled.get()).isFalse()
    }

    @Test
    fun exportDataRethrowsCancellationException() = runBlocking {
        val repository = DataTransferRepository(
            apiService = exportCancellingService(),
            executor = NetworkExecutor(
                Json {
                    ignoreUnknownKeys = true
                    explicitNulls = false
                    isLenient = true
                }
            ),
            dispatchers = DispatchersProvider(),
        )
        var thrown: CancellationException? = null

        try {
            repository.exportData()
        } catch (exception: CancellationException) {
            thrown = exception
        }

        assertThat(thrown).isNotNull()
        assertThat(thrown?.message).isEqualTo("export left")
    }
}

private fun failingService(onCall: () -> Unit): DataTransferApiService =
    Proxy.newProxyInstance(
        DataTransferApiService::class.java.classLoader,
        arrayOf(DataTransferApiService::class.java),
    ) { _, method, _ ->
        when (method.name) {
            "toString" -> "FailingDataTransferApiService"
            "hashCode" -> System.identityHashCode(method)
            "equals" -> false
            else -> {
                onCall()
                throw AssertionError("DataTransferApiService should not be called.")
            }
        }
    } as DataTransferApiService

private fun exportCancellingService(): DataTransferApiService =
    Proxy.newProxyInstance(
        DataTransferApiService::class.java.classLoader,
        arrayOf(DataTransferApiService::class.java),
    ) { _, method, _ ->
        when (method.name) {
            "exportData" -> throw CancellationException("export left")
            "toString" -> "ExportCancellingDataTransferApiService"
            "hashCode" -> System.identityHashCode(method)
            "equals" -> false
            else -> throw AssertionError("Only exportData should be called.")
        }
    } as DataTransferApiService
