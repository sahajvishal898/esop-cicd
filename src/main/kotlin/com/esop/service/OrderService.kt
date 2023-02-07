package com.esop.service


import com.esop.constant.errors
import com.esop.repository.UserRecords
import com.esop.schema.History
import com.esop.schema.Order
import com.esop.schema.OrderFilledLog
import com.esop.schema.PlatformFee.Companion.addPlatformFee
import com.esop.schema.User
import jakarta.inject.Singleton
import kotlin.math.round

private const val TWO_PERCENT = 0.02

@Singleton
class OrderService(private val userRecords: UserRecords) {
    companion object {
        private var orderId = 1L

        var buyOrders = mutableListOf<Order>()
        var sellOrders = mutableListOf<Order>()
    }

    @Synchronized
    fun generateOrderId(): Long {
        return orderId++
    }

    private fun updateOrderDetails(
        orderQuantity: Long,
        unfulfilledOrderQuantity: Long,
        sellerOrder: Order,
        buyerOrder: Order
    ) {
        // Deduct money of quantity taken from buyer
        val currentTradeQuantity = orderQuantity - unfulfilledOrderQuantity
        val sellAmount = sellerOrder.getPrice() * (currentTradeQuantity)
        val buyer = userRecords.getUser(buyerOrder.getUserName())!!
        val seller = userRecords.getUser(sellerOrder.getUserName())!!
        var platformFee = 0L


        if (sellerOrder.esopType == "NON_PERFORMANCE")
            platformFee = round(sellAmount * TWO_PERCENT).toLong()

        updateWalletBalances(sellAmount, platformFee, buyer, seller)


        seller.transferLockedESOPsTo(buyer, sellerOrder.esopType, currentTradeQuantity)

        val amountToBeReleased = (buyerOrder.getPrice() - sellerOrder.getPrice()) * (currentTradeQuantity)
        buyer.userWallet.moveMoneyFromLockedToFree(amountToBeReleased)

    }

    private fun updateWalletBalances(
        sellAmount: Long,
        platformFee: Long,
        buyer: User,
        seller: User
    ) {
        val adjustedSellAmount = sellAmount - platformFee
        addPlatformFee(platformFee)

        buyer.userWallet.removeMoneyFromLockedState(sellAmount)
        seller.userWallet.addMoneyToWallet(adjustedSellAmount)
    }


    private fun sortAscending(): List<Order> {
        return sellOrders.sortedWith(object : Comparator<Order> {
            override fun compare(o1: Order, o2: Order): Int {

                if (o1.inventoryPriority != o2.inventoryPriority)
                    return o1.inventoryPriority - o2.inventoryPriority

                if (o1.inventoryPriority == 1) {
                    if (o1.timeStamp < o2.timeStamp)
                        return -1
                    return 1
                }

                if (o1.getPrice() == o2.getPrice()) {
                    if (o1.timeStamp < o2.timeStamp)
                        return -1
                    return 1
                }
                if (o1.getPrice() < o2.getPrice())
                    return -1
                return 1
            }
        })
    }

    fun placeOrder(order: Order): Map<String, Any> {
        var inventoryPriority = 2
        if (order.esopType == "PERFORMANCE") {
            inventoryPriority -= 1
        }
        order.orderID = generateOrderId()
        order.inventoryPriority = inventoryPriority
        order.remainingQuantity = order.getQuantity()

        if (order.getType() == "BUY") {
            executeBuyOrder(order)
        } else {
            executeSellOrder(order)
        }
        userRecords.getUser(order.getUserName())?.orderList?.add(order)
        return mapOf("orderId" to order.orderID)
    }

    private fun executeSellOrder(order: Order) {
        sellOrders.add(order)
        val sortedBuyOrders =
            buyOrders.sortedWith(compareByDescending<Order> { it.getPrice() }.thenBy { it.timeStamp })

        for (bestBuyOrder in sortedBuyOrders) {
            if ((order.getPrice() <= bestBuyOrder.getPrice()) && (bestBuyOrder.remainingQuantity > 0)) {
                val prevQuantity = order.remainingQuantity
                if (order.remainingQuantity < bestBuyOrder.remainingQuantity) {

                    val buyOrderLog =
                        OrderFilledLog(
                            order.remainingQuantity,
                            bestBuyOrder.getPrice(),
                            null,
                            order.getUserName(),
                            null
                        )
                    val sellOrderLog = OrderFilledLog(
                        order.remainingQuantity,
                        bestBuyOrder.getPrice(),
                        order.esopType,
                        null,
                        bestBuyOrder.getUserName()
                    )

                    bestBuyOrder.remainingQuantity = bestBuyOrder.remainingQuantity - order.remainingQuantity
                    bestBuyOrder.orderStatus = "PARTIAL"
                    bestBuyOrder.orderFilledLogs.add(buyOrderLog)

                    order.remainingQuantity = 0
                    order.orderStatus = "COMPLETED"
                    order.orderFilledLogs.add(sellOrderLog)
                    sellOrders.remove(order)

                    updateOrderDetails(
                        prevQuantity,
                        order.remainingQuantity,
                        order,
                        bestBuyOrder
                    )

                } else if (order.remainingQuantity > bestBuyOrder.remainingQuantity) {

                    val buyOrderLog =
                        OrderFilledLog(
                            bestBuyOrder.remainingQuantity,
                            order.getPrice(),
                            null,
                            order.getUserName(),
                            null
                        )
                    val sellOrderLog = OrderFilledLog(
                        bestBuyOrder.remainingQuantity,
                        order.getPrice(),
                        order.esopType,
                        null,
                        bestBuyOrder.getUserName()
                    )


                    order.remainingQuantity = order.remainingQuantity - bestBuyOrder.remainingQuantity
                    order.orderStatus = "PARTIAL"
                    order.orderFilledLogs.add(sellOrderLog)

                    bestBuyOrder.remainingQuantity = 0
                    bestBuyOrder.orderStatus = "COMPLETED"
                    bestBuyOrder.orderFilledLogs.add(buyOrderLog)
                    buyOrders.remove(bestBuyOrder)

                    updateOrderDetails(
                        prevQuantity,
                        order.remainingQuantity,
                        order,
                        bestBuyOrder
                    )
                } else {
                    val buyOrderLog =
                        OrderFilledLog(
                            bestBuyOrder.remainingQuantity,
                            order.getPrice(),
                            null,
                            order.getUserName(),
                            null
                        )
                    val sellOrderLog = OrderFilledLog(
                        order.remainingQuantity,
                        order.getPrice(),
                        order.esopType,
                        null,
                        bestBuyOrder.getUserName()
                    )

                    bestBuyOrder.remainingQuantity = 0
                    bestBuyOrder.orderStatus = "COMPLETED"
                    bestBuyOrder.orderFilledLogs.add(buyOrderLog)
                    buyOrders.remove(bestBuyOrder)

                    order.remainingQuantity = 0
                    order.orderStatus = "COMPLETED"
                    order.orderFilledLogs.add(sellOrderLog)
                    sellOrders.remove(order)

                    updateOrderDetails(
                        prevQuantity,
                        order.remainingQuantity,
                        order,
                        bestBuyOrder
                    )

                }
            }
        }
    }

    private fun executeBuyOrder(order: Order) {
        buyOrders.add(order)
        val sortedSellOrders = sortAscending()


        for (bestSellOrder in sortedSellOrders) {
            if ((order.getPrice() >= bestSellOrder.getPrice()) && (bestSellOrder.remainingQuantity > 0)) {
                val prevQuantity = order.remainingQuantity
                if (order.remainingQuantity < bestSellOrder.remainingQuantity) {

                    val buyOrderLog = OrderFilledLog(
                        order.remainingQuantity,
                        bestSellOrder.getPrice(),
                        null,
                        bestSellOrder.getUserName(),
                        null
                    )
                    val sellOrderLog = OrderFilledLog(
                        order.remainingQuantity,
                        bestSellOrder.getPrice(),
                        bestSellOrder.esopType,
                        null,
                        order.getUserName()
                    )

                    bestSellOrder.remainingQuantity = bestSellOrder.remainingQuantity - order.remainingQuantity
                    bestSellOrder.orderStatus = "PARTIAL"
                    bestSellOrder.orderFilledLogs.add(sellOrderLog)

                    order.remainingQuantity = 0
                    order.orderStatus = "COMPLETED"
                    order.orderFilledLogs.add(buyOrderLog)
                    buyOrders.remove(order)

                    updateOrderDetails(
                        prevQuantity,
                        order.remainingQuantity,
                        bestSellOrder,
                        order
                    )

                } else if (order.remainingQuantity > bestSellOrder.remainingQuantity) {

                    val buyOrderLog = OrderFilledLog(
                        bestSellOrder.remainingQuantity,
                        order.getPrice(),
                        null,
                        bestSellOrder.getUserName(),
                        null
                    )
                    val sellOrderLog = OrderFilledLog(
                        bestSellOrder.remainingQuantity,
                        order.getPrice(),
                        bestSellOrder.esopType,
                        null
                    )

                    order.remainingQuantity = order.remainingQuantity - bestSellOrder.remainingQuantity
                    order.orderStatus = "PARTIAL"
                    order.orderFilledLogs.add(sellOrderLog)

                    bestSellOrder.remainingQuantity = 0
                    bestSellOrder.orderStatus = "COMPLETED"
                    bestSellOrder.orderFilledLogs.add(buyOrderLog)
                    sellOrders.remove(bestSellOrder)

                    updateOrderDetails(
                        prevQuantity,
                        order.remainingQuantity,
                        bestSellOrder,
                        order
                    )
                } else {
                    val buyOrderLog = OrderFilledLog(
                        bestSellOrder.remainingQuantity,
                        bestSellOrder.getPrice(),
                        null,
                        bestSellOrder.getUserName()
                    )
                    val sellOrderLog = OrderFilledLog(
                        order.remainingQuantity,
                        bestSellOrder.getPrice(),
                        bestSellOrder.esopType,
                        order.getUserName(),
                        null
                    )

                    bestSellOrder.remainingQuantity = 0
                    bestSellOrder.orderStatus = "COMPLETED"
                    bestSellOrder.orderFilledLogs.add(buyOrderLog)
                    sellOrders.remove(bestSellOrder)

                    order.remainingQuantity = 0
                    order.orderStatus = "COMPLETED"
                    order.orderFilledLogs.add(sellOrderLog)
                    buyOrders.remove(order)

                    updateOrderDetails(
                        prevQuantity,
                        order.remainingQuantity,
                        bestSellOrder,
                        order
                    )

                }

            }
        }
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

