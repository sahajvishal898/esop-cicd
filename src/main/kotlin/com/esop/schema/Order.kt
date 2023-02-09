package com.esop.schema

import com.esop.schema.ESOPType.*
import com.esop.schema.OrderStatus.*
import com.esop.schema.OrderType.*

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
    var inventoryPriority = NON_PERFORMANCE
    var remainingQuantity = quantity

    init {
        if (isTypeSellAndEsopTypePerformance()) {
            inventoryPriority = PERFORMANCE
        } else if (isTypeSellAndEsopTypeNonPerformance()) {
            inventoryPriority = NON_PERFORMANCE
        }
    }

    private fun isTypeSellAndEsopTypePerformance() = type == SELL && esopType == PERFORMANCE

    private fun isTypeSellAndEsopTypeNonPerformance() = type == SELL && esopType == NON_PERFORMANCE
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

    fun getEsopType(): ESOPType{
        return esopType
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

    fun addOrderFilledLogs(orderFilledLog: OrderFilledLog) {
        orderFilledLogs.add(orderFilledLog)
    }
}