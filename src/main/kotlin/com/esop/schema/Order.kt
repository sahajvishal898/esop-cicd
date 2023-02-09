package com.esop.schema

import com.esop.schema.ESOPType.NON_PERFORMANCE
import com.esop.schema.OrderStatus.*
import com.esop.schema.OrderType.BUY

class Order(
    private var quantity: Long,
    private var type: OrderType,
    private var price: Long,
    private var userName: String,
    private var esopType: ESOPType = NON_PERFORMANCE
) {
    var timeStamp = System.currentTimeMillis()
    var orderStatus: OrderStatus = PENDING // COMPLETED, PARTIAL, PENDING
    var orderFilledLogs: MutableList<OrderFilledLog> = mutableListOf()
    var orderID: Long = -1
    var remainingQuantity = quantity

    fun getQuantity(): Long {
        return quantity
    }

    fun getPrice(): Long {
        return price
    }

    fun getType(): OrderType {
        return type
    }

    fun getUserName(): String {
        return userName
    }

    fun getEsopType(): ESOPType {
        return esopType
    }

    fun updateRemainingQuantityAndStatus(quantityToBeUpdated: Long) {
        updateRemainingQuantity(quantityToBeUpdated)
        updateStatus()
    }

    fun updateRemainingQuantity(quantityToBeUpdated: Long) {
        remainingQuantity -= quantityToBeUpdated
    }

    fun updateStatus() {
        if (remainingQuantity == 0L) {
            orderStatus = COMPLETED
        } else if (remainingQuantity != quantity) {
            orderStatus = PARTIAL
        }
    }

    fun updateOrderLogs(
        orderExecutionQuantity: Long,
        orderExecutionPrice: Long,
        order: Order
    ) {
        orderFilledLogs.add(
            OrderFilledLog(
                orderExecutionQuantity,
                orderExecutionPrice,
                if (type == BUY) order.getEsopType() else null,
                order.getUserName()
            )
        )
    }
}