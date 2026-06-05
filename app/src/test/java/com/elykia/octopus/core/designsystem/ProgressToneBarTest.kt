package com.elykia.octopus.core.designsystem

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ProgressToneBarTest {
    @Test
    fun normalizedProgressClampsToProgressRange() {
        assertThat((-0.25f).normalizedProgress()).isEqualTo(0f)
        assertThat(0.5f.normalizedProgress()).isEqualTo(0.5f)
        assertThat(1.25f.normalizedProgress()).isEqualTo(1f)
    }

    @Test
    fun normalizedProgressTreatsInvalidValuesAsZero() {
        assertThat(Float.NaN.normalizedProgress()).isEqualTo(0f)
        assertThat(Float.POSITIVE_INFINITY.normalizedProgress()).isEqualTo(0f)
        assertThat(Float.NEGATIVE_INFINITY.normalizedProgress()).isEqualTo(0f)
    }
}
