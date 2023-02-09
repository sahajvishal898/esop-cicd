package com.esop.service

import com.esop.constant.FEE_PERCENTAGE
import com.esop.schema.ESOPType
import jakarta.inject.Singleton
import java.math.BigInteger
import kotlin.math.round
import com.esop.schema.ESOPType.*

@Singleton
class PlatformFeeService {

    private var totalPlatformFee: BigInteger = BigInteger("0")

    fun getPlatformFee(): BigInteger {
        return totalPlatformFee
    }

    private fun calculateFee(tradedAmount: Long) = round(tradedAmount * FEE_PERCENTAGE).toLong()

    fun deductPlatformFeeFrom(tradedAmount: Long, esopType: ESOPType): Long {
        if (tradedAmount < 0) throw IllegalArgumentException("Traded Amount cannot be negative")
        if (esopType == PERFORMANCE) return tradedAmount

        val fee = calculateFee(tradedAmount)
        totalPlatformFee += fee.toBigInteger()
        return tradedAmount - fee
    }
}