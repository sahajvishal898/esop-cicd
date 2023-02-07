package com.esop.repository

import com.esop.schema.Order
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class OrderRecordsTest{
    lateinit var orderRecords: OrderRecords

    @BeforeEach
    fun setup(){
        orderRecords = OrderRecords()
    }

    @Test
    fun `should return sell order`(){
        val sellOrder = Order(10,"SELL",10,"sankar")
        orderRecords.addSellOrder(sellOrder)
        val expectedOrderType = "SELL"

        val response = orderRecords.getSellOrder()

        assertEquals(expectedOrderType,response?.getType())
    }

    @Test
    fun `getSellOrder should return empty`(){
        val response = orderRecords.getSellOrder()

        assertNull(response)
    }

    @Test
    fun `should return buy order`(){
        val buyOrder = Order(10,"BUY",10,"sankar")
        orderRecords.addBuyOrder(buyOrder)
        val expectedOrderType = "BUY"

        val response = orderRecords.getBuyOrder()

        assertEquals(expectedOrderType,response?.getType())
    }

    @Test
    fun `getBuyOrder should return empty`(){
        val response = orderRecords.getBuyOrder()

        assertNull(response)
    }

    @Test
    fun `it should remove buy order`(){
        val buyOrder = Order(10,"BUY",10,"sankar")
        orderRecords.addBuyOrder(buyOrder)
        val response = orderRecords.getBuyOrder()

        orderRecords.removeBuyOrder(response!!)

        assertNull(orderRecords.getBuyOrder())
    }

    @Test
    fun `it should remove sell order`(){
        val sellOrder = Order(10,"SELL",10,"sankar")
        orderRecords.addSellOrder(sellOrder)
        val response = orderRecords.getSellOrder()

        orderRecords.removeSellOrder(response!!)

        assertNull(orderRecords.getSellOrder())
    }

}