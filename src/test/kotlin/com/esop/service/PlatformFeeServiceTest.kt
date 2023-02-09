package com.esop.service

import com.esop.schema.ESOPType.NON_PERFORMANCE
import com.esop.schema.ESOPType.PERFORMANCE
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigInteger


class PlatformFeeServiceTest {

    private lateinit var platformFeeService: PlatformFeeService

    @BeforeEach
    fun `it should set the Platform fee to zero`() {
        platformFeeService = PlatformFeeService()
    }

    @Test
    fun `it should deduct platform fee from the given amount as ESOP type is NON_PERFORMANCE`() {
        val amount = 1000L

        val actualAmount = platformFeeService.deductPlatformFeeFrom(amount, NON_PERFORMANCE)

        val expectedAmount = 980L
        val expectedPlatformFee = BigInteger("20")
        assertEquals(expectedAmount, actualAmount)
        assertEquals(expectedPlatformFee, platformFeeService.getPlatformFee())
    }

    @Test
    fun `it should not deduct platform fee from the given amount as ESOP type is PERFORMANCE`() {
        val amount = 1000L

        val actualAmount = platformFeeService.deductPlatformFeeFrom(amount, PERFORMANCE)

        val expectedAmount = 1000L
        val expectedPlatformFee = BigInteger("0")
        assertEquals(expectedAmount, actualAmount)
        assertEquals(expectedPlatformFee, platformFeeService.getPlatformFee())
    }

    @Test
    fun `it should throw exception if a invalid traded amount is being passed`() {
        val tradedAmount: Long = -100

        Assertions.assertThrows(IllegalArgumentException::class.java) {
            platformFeeService.deductPlatformFeeFrom(tradedAmount, NON_PERFORMANCE)
        }
    }
}