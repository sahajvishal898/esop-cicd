package com.esop.exceptions

import com.esop.constant.errors
import com.esop.schema.ESOPType
import io.micronaut.http.HttpStatus

open class HttpException(val status: HttpStatus, message: String) : RuntimeException(message)

class InventoryLimitExceededException : HttpException(HttpStatus.BAD_REQUEST, errors["INVENTORY_LIMIT_EXCEEDED"]!!)

class WalletLimitExceededException : HttpException(HttpStatus.BAD_REQUEST, errors["WALLET_LIMIT_EXCEEDED"]!!)

class InsufficientFundsException() : Throwable(errors["INSUFFICIENT_FUNDS"])

class InsufficientInventoryException(val type: ESOPType) : Throwable("Insufficient $type inventory") {}

class UserDoesNotExistException() : Throwable(errors["USER_DOES_NOT_EXISTS"])

class InvalidPreOrderPlaceException(val errorList: List<String>) : Throwable()