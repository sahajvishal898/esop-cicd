package com.esop.service


import com.esop.constant.errors
import com.esop.repository.OrderRecords
import com.esop.repository.UserRecords
import com.esop.schema.*
import com.esop.schema.User
import jakarta.inject.Singleton
import kotlin.math.min
import com.esop.schema.ESOPType.*

@Singleton
class OrderService(private val userRecords: UserRecords,
                   private val orderRecords: OrderRecords,
                   private val platformFeeService: PlatformFeeService
) {

    private fun updateOrderDetails(
        currentTradeQuantity: Long,
        sellerOrder: Order,
        buyerOrder: Order
    ) {
        // Deduct money of quantity taken from buyer
        val sellAmount = sellerOrder.getPrice() * (currentTradeQuantity)
        val buyer = userRecords.getUser(buyerOrder.getUserName())!!
        val seller = userRecords.getUser(sellerOrder.getUserName())!!

        updateWalletBalances(sellAmount, sellerOrder.getEsopType(), buyer, seller)

        seller.transferLockedESOPsTo(buyer, sellerOrder.getEsopType(), currentTradeQuantity)

        val amountToBeReleased = (buyerOrder.getPrice() - sellerOrder.getPrice()) * (currentTradeQuantity)
        buyer.userWallet.moveMoneyFromLockedToFree(amountToBeReleased)

    }

    private fun updateWalletBalances(
        sellAmount: Long,
        esopType: ESOPType,
        buyer: User,
        seller: User
    ) {
        val adjustedSellAmount = platformFeeService.deductPlatformFeeFrom(sellAmount, esopType)

        buyer.userWallet.removeMoneyFromLockedState(sellAmount)
        seller.userWallet.addMoneyToWallet(adjustedSellAmount)
    }


    fun placeOrder(order: Order): Long {
        order.orderID = orderRecords.generateOrderId()

        if (order.getType() == "BUY") {
            executeBuyOrder(order)
        } else {
            executeSellOrder(order)
        }
        userRecords.getUser(order.getUserName())?.orderList?.add(order)
        return order.orderID
    }

    private fun executeBuyOrder(buyOrder: Order) {
        orderRecords.addBuyOrder(buyOrder)
        while(buyOrder.remainingQuantity != 0L){
            val bestSellOrder = orderRecords.getBestSellOrder(buyOrder.getPrice())?:return
            performOrderMatching(
                bestSellOrder,
                buyOrder
            )
        }
    }

    private fun executeSellOrder(sellOrder: Order) {
        orderRecords.addSellOrder(sellOrder)
        while(sellOrder.remainingQuantity != 0L){
            val bestBuyOrder = orderRecords.getBestBuyOrder(sellOrder.getPrice())?:return

            performOrderMatching(
                sellOrder,
                bestBuyOrder
            )
        }
    }

    private fun performOrderMatching(sellOrder: Order, buyOrder: Order) {
        val orderExecutionPrice = sellOrder.getPrice()
        val orderExecutionQuantity = min(sellOrder.remainingQuantity, buyOrder.remainingQuantity)

        buyOrder.updateRemainingQuantity(orderExecutionQuantity)
        sellOrder.updateRemainingQuantity(orderExecutionQuantity)

        buyOrder.updateStatus()
        sellOrder.updateStatus()

        createOrderFilledLogs(orderExecutionQuantity, orderExecutionPrice, sellOrder, buyOrder)

        updateOrderDetails(
            orderExecutionQuantity,
            sellOrder,
            buyOrder
        )

        if (buyOrder.orderStatus == "COMPLETED") {
            orderRecords.removeBuyOrder(buyOrder)
        }
        if (sellOrder.orderStatus == "COMPLETED") {
            orderRecords.removeSellOrder(sellOrder)
        }
    }

    private fun createOrderFilledLogs(
        orderExecutionQuantity: Long,
        orderExecutionPrice: Long,
        sellOrder: Order,
        buyOrder: Order
    ) {
        val buyOrderLog = OrderFilledLog(
            orderExecutionQuantity,
            orderExecutionPrice,
            null,
            sellOrder.getUserName()
        )
        val sellOrderLog = OrderFilledLog(
            orderExecutionQuantity,
            orderExecutionPrice,
            sellOrder.getEsopType(),
            buyOrder.getUserName()
        )

        buyOrder.addOrderFilledLogs(buyOrderLog)
        sellOrder.addOrderFilledLogs(sellOrderLog)
    }

    fun orderHistory(userName: String): Any {
        val userErrors = ArrayList<String>()
        if (!userRecords.checkIfUserExists(userName)) {
            errors["USER_DOES_NOT_EXISTS"]?.let { userErrors.add(it) }
            return mapOf("error" to userErrors)
        }
        val orderDetails = userRecords.getUser(userName)!!.orderList
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