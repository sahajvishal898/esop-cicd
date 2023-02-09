package com.esop.schema

data class History(
    var orderId: Long,
    var quantity: Long,
    var type: String,
    var price: Long,
    var status: OrderStatus,
    var filled: MutableList<OrderFilledLog>
)