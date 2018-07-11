package ir.malv.getcards

import org.junit.Assert.*
import org.junit.Test

class ToolsTest {

    @Test
    fun getRandomNumberInRange() {
        val num = Tools.getRandomNumberInRange(0, 10)
        val range = 1..10
        assertTrue(range.contains(num))
        assertTrue(range.toList().size == 10)
    }
}