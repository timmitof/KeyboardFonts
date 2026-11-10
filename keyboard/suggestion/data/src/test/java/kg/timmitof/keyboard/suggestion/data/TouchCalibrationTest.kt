package kg.timmitof.keyboard.suggestion.data

import kg.timmitof.keyboard.suggestion.domain.model.TypedKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class TouchCalibrationTest {

    @Test
    fun `без замеров касания не меняются`() {
        val touches = listOf(TypedKey('a', 0.2f, 0.3f))

        assertEquals(touches, TouchCalibration().correct(touches))
    }

    @Test
    fun `привычный сдвиг вычитается из касаний`() {
        val calibration = TouchCalibration()
        repeat(TouchCalibration.MAX_SAMPLES) { calibration.observe(listOf(TypedKey('a', 0f, 0.2f))) }

        val corrected = calibration.correct(listOf(TypedKey('a', 0f, 0.2f))).single()

        assertTrue("${corrected.offsetY}", abs(corrected.offsetY) < 0.03f)
    }

    @Test
    fun `редкая буква берет общий сдвиг`() {
        val calibration = TouchCalibration()
        repeat(TouchCalibration.MAX_SAMPLES) { calibration.observe(listOf(TypedKey('a', 0f, 0.2f))) }

        val rare = calibration.correct(listOf(TypedKey('z', 0f, 0.2f))).single()

        assertTrue("${rare.offsetY}", rare.offsetY < 0.2f && rare.offsetY > 0f)
    }

    @Test
    fun `сохраняется и восстанавливается`() {
        val calibration = TouchCalibration()
        repeat(50) { calibration.observe(listOf(TypedKey('a', 0.1f, 0.2f))) }

        val restored = TouchCalibration().apply { restore(calibration.export().asSequence()) }
        val touch = listOf(TypedKey('a', 0.1f, 0.2f))

        assertEquals(calibration.correct(touch), restored.correct(touch))
    }
}
