package com.elykia.octopus.core.data.local

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.IOException
import javax.crypto.AEADBadTagException

class SecureSessionStoreTest {
    @Test
    fun recoveryIsAllowedForEncryptedPreferencesCorruption() {
        assertThat(shouldRecoverEncryptedPreferences(AEADBadTagException("bad decrypt"))).isTrue()
        assertThat(shouldRecoverEncryptedPreferences(IllegalStateException("Could not decrypt value"))).isTrue()
        assertThat(shouldRecoverEncryptedPreferences(IllegalStateException("invalid protocol buffer"))).isTrue()
    }

    @Test
    fun recoveryIsAllowedForNestedCorruptionCause() {
        val exception = IllegalStateException("open failed", AEADBadTagException("mac check failed"))

        assertThat(shouldRecoverEncryptedPreferences(exception)).isTrue()
    }

    @Test
    fun recoveryIsRejectedForTransientInitializationFailures() {
        assertThat(shouldRecoverEncryptedPreferences(IOException("keystore temporarily unavailable"))).isFalse()
        assertThat(shouldRecoverEncryptedPreferences(IllegalStateException("provider not ready"))).isFalse()
    }
}
