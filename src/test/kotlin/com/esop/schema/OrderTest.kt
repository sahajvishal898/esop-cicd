package com.esop.schema

import com.esop.schema.OrderStatus.COMPLETED
import com.esop.schema.OrderStatus.PARTIAL
import com.esop.schema.OrderType.BUY
import com.esop.schema.OrderType.SELL
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class OrderTest {
    @Test
    fun `it should update remaining quantity`() {
        val buy = Order(10, BUY, 10, "sankar")
        val expectedRemainingQuantity = 5L

        buy.updateRemainingQuantity(5L)

        assertEquals(expectedRemainingQuantity, buy.remainingQuantity)
    }

    @Test
    fun `it should set the status as completed`() {
        val buy = Order(10, BUY, 10, "sankar")
        buy.remainingQuantity = 0

        buy.updateStatus()

        assertEquals(COMPLETED, buy.orderStatus)
    }

    @Test
    fun `it should set the status as partial`() {
        val buy = Order(10, BUY, 10, "sankar")
        buy.remainingQuantity = 5

        buy.updateStatus()

        assertEquals(PARTIAL, buy.orderStatus)
    }

    @Test
    fun `it should add order log`() {
        val buyOrder = Order(10, BUY, 10, "Sankar")
        val sellOrder = Order(10, SELL, 10, "Kajal")

        buyOrder.updateOrderLogs(10, 10, sellOrder)

        assertEquals(1, buyOrder.orderFilledLogs.size)
    }
}