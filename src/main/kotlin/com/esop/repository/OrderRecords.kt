package com.esop.repository

import com.esop.schema.ESOPType.NON_PERFORMANCE
import com.esop.schema.ESOPType.PERFORMANCE
import com.esop.schema.Order
import jakarta.inject.Singleton

@Singleton
class OrderRecords {
    private var orderId = 1L
    private var buyOrders = mutableListOf<Order>()
    private var sellOrders = mutableListOf<Order>()

    @Synchronized
    fun generateOrderId(): Long {
        return orderId++
    }

    fun addBuyOrder(buyOrder: Order) {
        buyOrder.orderID = generateOrderId()
        buyOrders.add(buyOrder)
    }

    fun addSellOrder(sellOrder: Order) {
        sellOrder.orderID = generateOrderId()
        sellOrders.add(sellOrder)
    }

    fun removeBuyOrder(buyOrder: Order) {
        buyOrders.remove(buyOrder)
    }

    fun removeSellOrder(sellOrder: Order) {
        sellOrders.remove(sellOrder)
    }

    fun getBestBuyOrder(price: Long): Order? {
        var bestBuyOrder: Order? = null
        if (buyOrders.isNotEmpty()) {
            val sortedBuyOrders = sortBuyOrders()
            if (sortedBuyOrders[0].getPrice() >= price) bestBuyOrder = sortedBuyOrders[0]
        }
        return bestBuyOrder
    }

    fun getBestSellOrder(price: Long): Order? {
        var bestSellOrder: Order? = null
        if (sellOrders.isNotEmpty()) {
            val sortedSellOrders = sortSellOrders()
            for (sellOrder in sortedSellOrders) {
                if (sellOrder.getPrice() <= price) {
                    bestSellOrder = sellOrder
                    break
                }
            }
        }
        return bestSellOrder
    }

    private fun sortBuyOrders(): List<Order> {
        return buyOrders.sortedWith(compareByDescending<Order> { it.getPrice() }.thenBy { it.timeStamp })
    }

    private fun sortSellOrders(): List<Order> {
        return sellOrders.sortedWith(object : Comparator<Order> {
            override fun compare(firstOrder: Order, secondOrder: Order): Int {
                val priorityComparison = firstOrder.getEsopType().compareTo(secondOrder.getEsopType())
                if (priorityComparison != 0) return priorityComparison
                when (firstOrder.getEsopType()) {
                    NON_PERFORMANCE -> {
                        val priceComparison = firstOrder.getPrice().compareTo(secondOrder.getPrice())
                        if (priceComparison != 0) return priceComparison
                        return firstOrder.timeStamp.compareTo(secondOrder.timeStamp)
                    }

                    PERFORMANCE -> return firstOrder.timeStamp.compareTo(secondOrder.timeStamp)
                    else -> return 1
                }
            }
        })
    }

    fun getBuyOrderById(orderId: Long): Order? {
        return buyOrders.filter { it.orderID == orderId }.elementAtOrNull(0)
    }

    fun getSellOrderById(orderId: Long): Order? {
        return sellOrders.filter { it.orderID == orderId }.elementAtOrNull(0)
    }
}