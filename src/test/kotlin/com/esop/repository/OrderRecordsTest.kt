package com.esop.repository

import com.esop.schema.Order
import com.esop.schema.OrderType.BUY
import com.esop.schema.OrderType.SELL
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class OrderRecordsTest {
    private lateinit var orderRecords: OrderRecords

    @BeforeEach
    fun setup() {
        orderRecords = OrderRecords()
    }

    @Test
    fun `should return sell order`() {
        val sellOrder = Order(10, SELL, 10, "sankar")
        orderRecords.addSellOrder(sellOrder)
        val expectedOrderType = SELL

        val response = orderRecords.getBestSellOrder(sellOrder.getPrice())

        assertEquals(expectedOrderType, response?.getType())
    }


    @Test
    fun `should return buy order`() {
        val buyOrder = Order(10, BUY, 10, "sankar")
        orderRecords.addBuyOrder(buyOrder)
        val expectedOrderType = BUY

        val response = orderRecords.getBuyOrderById(buyOrder.orderID)

        assertEquals(expectedOrderType, response?.getType())
    }


    @Test
    fun `it should remove buy order`() {
        val buyOrder = Order(10, BUY, 10, "sankar")
        orderRecords.addBuyOrder(buyOrder)
        val response = orderRecords.getBuyOrderById(buyOrder.orderID)

        orderRecords.removeBuyOrder(response!!)

        assertNull(orderRecords.getBuyOrderById(buyOrder.orderID))
    }

    @Test
    fun `it should remove sell order`() {
        val sellOrder = Order(10, SELL, 10, "sankar")
        orderRecords.addSellOrder(sellOrder)
        val response = orderRecords.getSellOrderById(sellOrder.orderID)

        orderRecords.removeSellOrder(response!!)

        assertNull(orderRecords.getSellOrderById(sellOrder.orderID))
    }

}