package com.esop.schema

data class OrderFilledLog(
    var quantity: Long = 0,
    var amount: Long = 0,
    var esopType: ESOPType? = null,
    var userName: String? = null
)