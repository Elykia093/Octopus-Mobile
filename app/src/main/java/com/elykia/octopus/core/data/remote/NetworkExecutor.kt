package com.elykia.octopus.core.data.remote

import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.data.model.ApiEnvelope
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkExecutor @Inject constructor() {
    suspend fun <T> execute(block: suspend () -> ApiEnvelope<T>): AppResult<T> {
        return try {
            val response = block()
            val data = response.data
            if (data != null) {
                AppResult.Success(data)
            } else {
                AppResult.Error(response.message ?: "响应为空。")
            }
        } catch (exception: ApiException) {
            AppResult.Error(exception.message, exception)
        } catch (exception: HttpException) {
            AppResult.Error(exception.message(), exception)
        } catch (exception: IOException) {
            AppResult.Error(exception.message ?: "网络错误。", exception)
        } catch (exception: SerializationException) {
            AppResult.Error(exception.message ?: "数据解析失败。", exception)
        }
    }

    suspend fun executeUnit(block: suspend () -> ApiEnvelope<String?>): AppResult<String?> {
        return try {
            val response = block()
            AppResult.Success(response.data)
        } catch (exception: ApiException) {
            AppResult.Error(exception.message, exception)
        } catch (exception: HttpException) {
            AppResult.Error(exception.message(), exception)
        } catch (exception: IOException) {
            AppResult.Error(exception.message ?: "网络错误。", exception)
        } catch (exception: SerializationException) {
            AppResult.Error(exception.message ?: "数据解析失败。", exception)
        }
    }
}
