package com.example.qup


import junit.framework.Assert.assertEquals
import org.junit.Test


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun calculateEstimatedQueueTime() {
        assertEquals(1, com.example.qup.helpers.calculateEstimatedQueueTime(1, 1, 60))
    }
}