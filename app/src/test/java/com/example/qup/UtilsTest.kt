package com.example.qup

import com.example.qup.helpers.calculateEstimatedQueueTime
import org.junit.Assert.assertEquals
import org.junit.Test


class UtilsTest {
    @Test
    fun testCalculateEstimatedQueueTime() {
        assertEquals(1, calculateEstimatedQueueTime(1, 1, 60))      //(1/1)*60 = 60/60 = 1min           //simple case
        assertEquals(2, calculateEstimatedQueueTime(10, 5, 60))      //(10/2)*60 = 120/60 = 2min
        assertEquals(3, calculateEstimatedQueueTime(10, 4, 60))      //(10/4)*60 = 150/60 = 2.5min -> 3min          //round up
        assertEquals(11, calculateEstimatedQueueTime(120, 13, 72))      //(120/13)*72 = 663.62/60 = 11.07 -> 11mins     //round down
        assertEquals(17, calculateEstimatedQueueTime(742, 32, 43))      //(742/32)*43 = 997.063/60 = 16.62min -> 17 mins        //complicated case
        assertEquals(0, calculateEstimatedQueueTime(0, 1, 0))      //(0/1)*0 = 0/60 = 0min           //min case
    }
}