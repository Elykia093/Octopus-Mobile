package com.elykia.octopus.core.data.repository

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ExportPayloadCheckTest {
    @Test
    fun exportPayloadRejectsSensitiveKeys() {
        val payload = """
            {
              "settings": [
                {
                  "name": "openai",
                  "channel_key": "ck-secret"
                }
              ]
            }
        """.trimIndent().encodeToByteArray()

        val result = payload.checkExportPayload()

        assertThat(result).isEqualTo(ExportPayloadCheck.SensitiveField("$.settings[0].channel_key"))
    }

    @Test
    fun exportPayloadRejectsCamelCaseSensitiveKeys() {
        val payload = """{"apiKey":"sk-secret","nested":{"clientSecret":"secret"}}""".encodeToByteArray()

        val result = payload.checkExportPayload()

        assertThat(result).isEqualTo(ExportPayloadCheck.SensitiveField("$.apiKey"))
    }

    @Test
    fun exportPayloadRejectsAuthTokenFields() {
        val payload = """{"auth_token":"raw-token"}""".encodeToByteArray()

        val result = payload.checkExportPayload()

        assertThat(result).isEqualTo(ExportPayloadCheck.SensitiveField("$.auth_token"))
    }

    @Test
    fun exportPayloadRejectsPrivateAndAccessKeyFields() {
        val payload = """
            {
              "providers": [
                {
                  "service_account_private_key": "-----BEGIN PRIVATE KEY-----",
                  "access_key": "ak-secret"
                }
              ]
            }
        """.trimIndent().encodeToByteArray()

        val result = payload.checkExportPayload()

        assertThat(result).isEqualTo(ExportPayloadCheck.SensitiveField("$.providers[0].service_account_private_key"))
    }

    @Test
    fun exportPayloadAllowsNonSecretReferences() {
        val payload = """
            {
              "stats": [
                {
                  "api_key_id": 12,
                  "request_api_key_name": "mobile",
                  "input_token": 1200,
                  "output_token": 300
                }
              ],
              "settings": {
                "cors_allow_origins": ""
              }
            }
        """.trimIndent().encodeToByteArray()

        val result = payload.checkExportPayload()

        assertThat(result).isEqualTo(ExportPayloadCheck.Safe)
    }

    @Test
    fun exportPayloadRejectsSensitiveHeaderValues() {
        val payload = """
            {
              "custom_header": [
                {
                  "header_key": "Authorization",
                  "header_value": "Bearer upstream-secret"
                }
              ]
            }
        """.trimIndent().encodeToByteArray()

        val result = payload.checkExportPayload()

        assertThat(result).isEqualTo(ExportPayloadCheck.SensitiveField("$.custom_header[0].header_value"))
    }

    @Test
    fun exportPayloadRejectsSecretLookingValues() {
        val payload = """{"notes":["created with sk-live-secret"]}""".encodeToByteArray()

        val result = payload.checkExportPayload()

        assertThat(result).isEqualTo(ExportPayloadCheck.SensitiveField("$.notes[0]"))
    }

    @Test
    fun exportPayloadRejectsPemPrivateKeyValues() {
        val payload = """
            {
              "notes": "-----BEGIN PRIVATE KEY-----\nredacted\n-----END PRIVATE KEY-----"
            }
        """.trimIndent().encodeToByteArray()

        val result = payload.checkExportPayload()

        assertThat(result).isEqualTo(ExportPayloadCheck.SensitiveField("$.notes"))
    }

    @Test
    fun exportPayloadRejectsSensitiveKeyQueryValues() {
        val payload = """
            {
              "callback": "https://example.test/import?access_key=ak-secret&private_key=pk-secret&secret_key=sk-secret"
            }
        """.trimIndent().encodeToByteArray()

        val result = payload.checkExportPayload()

        assertThat(result).isEqualTo(ExportPayloadCheck.SensitiveField("$.callback"))
    }

    @Test
    fun exportPayloadRejectsInvalidJson() {
        val result = "not json".encodeToByteArray().checkExportPayload()

        assertThat(result).isEqualTo(ExportPayloadCheck.InvalidJson)
    }
}
