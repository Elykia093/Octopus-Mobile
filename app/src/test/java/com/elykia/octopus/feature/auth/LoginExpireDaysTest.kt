package com.elykia.octopus.feature.auth

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LoginExpireDaysTest {
    @Test
    fun parseLoginExpireDaysAcceptsTrimmedPositiveDays() {
        assertThat(parseLoginExpireDays(" 7 ")).isEqualTo(7)
        assertThat(parseLoginExpireDays("3650")).isEqualTo(3650)
    }

    @Test
    fun parseLoginExpireDaysRejectsInvalidOrUnsafeValues() {
        assertThat(parseLoginExpireDays("")).isNull()
        assertThat(parseLoginExpireDays("abc")).isNull()
        assertThat(parseLoginExpireDays("0")).isNull()
        assertThat(parseLoginExpireDays("-1")).isNull()
        assertThat(parseLoginExpireDays("3651")).isNull()
    }
}
