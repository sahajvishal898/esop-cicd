package com.esop.service

import com.esop.exceptions.UserDoesNotExistException
import com.esop.repository.OrderRecords
import com.esop.schema.History
import com.esop.schema.Order
import com.esop.schema.OrderStatus.COMPLETED
import com.esop.schema.OrderType.BUY
import jakarta.inject.Singleton
import kotlin.math.min

@Singleton
class OrderService(
    private val userService: UserService,
    private val orderRecords: OrderRecords
) {

    fun placeOrder(order: Order): Long {
        userService.checkUserDetailsForOrder(order)
        if (order.getType() == BUY) {
            executeBuyOrder(order)
        } else {
            executeSellOrder(order)
        }
        userService.getUser(order.getUserName())?.orderList?.add(order)
        return order.orderID
    }

    private fun executeBuyOrder(buyOrder: Order) {
        orderRecords.addBuyOrder(buyOrder)
        while (buyOrder.remainingQuantity != 0L) {
            val bestSellOrder = orderRecords.getBestSellOrder(buyOrder.getPrice()) ?: return
            performOrderMatching(bestSellOrder, buyOrder)
        }
    }

    private fun executeSellOrder(sellOrder: Order) {
        orderRecords.addSellOrder(sellOrder)
        while (sellOrder.remainingQuantity != 0L) {
            val bestBuyOrder = orderRecords.getBestBuyOrder(sellOrder.getPrice()) ?: return
            performOrderMatching(sellOrder, bestBuyOrder)
        }
    }

    private fun performOrderMatching(sellOrder: Order, buyOrder: Order) {
        val orderExecutionPrice = sellOrder.getPrice()
        val orderExecutionQuantity = min(sellOrder.remainingQuantity, buyOrder.remainingQuantity)

        buyOrder.updateRemainingQuantityAndStatus(orderExecutionQuantity)
        sellOrder.updateRemainingQuantityAndStatus(orderExecutionQuantity)

        createOrderFilledLogs(orderExecutionQuantity, orderExecutionPrice, sellOrder, buyOrder)

        userService.updateUserDetails(orderExecutionQuantity, sellOrder, buyOrder)

        removeCompletedOrders(buyOrder, sellOrder)
    }

    private fun removeCompletedOrders(buyOrder: Order, sellOrder: Order) {
        if (buyOrder.orderStatus == COMPLETED) {
            orderRecords.removeBuyOrder(buyOrder)
        }
        if (sellOrder.orderStatus == COMPLETED) {
            orderRecords.removeSellOrder(sellOrder)
        }
    }

    private fun createOrderFilledLogs(
        orderExecutionQuantity: Long, orderExecutionPrice: Long, sellOrder: Order, buyOrder: Order
    ) {
        buyOrder.updateOrderLogs(orderExecutionQuantity, orderExecutionPrice, sellOrder)
        sellOrder.updateOrderLogs(orderExecutionQuantity, orderExecutionPrice, buyOrder)
    }

    fun orderHistory(userName: String): ArrayList<History> {
        if (!userService.checkIfUserExists(userName)) {
            throw UserDoesNotExistException()
        }
        val orderDetails = userService.getUser(userName)!!.orderList
        val orderHistory = ArrayList<History>()

        for (orders in orderDetails) {
            orderHistory.add(
                History(
                    orders.orderID,
                    orders.getQuantity(),
                    orders.getType(),
                    orders.getPrice(),
                    orders.orderStatus,
                    orders.orderFilledLogs
                )
            )
        }
        return orderHistory
    }
}