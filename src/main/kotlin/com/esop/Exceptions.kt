package com.esop

import com.esop.schema.ESOPType
import io.micronaut.http.HttpStatus

open class HttpException(val status: HttpStatus, message: String) : RuntimeException(message)

class InventoryLimitExceededException : HttpException(HttpStatus.BAD_REQUEST, "Inventory Limit exceeded")

class WalletLimitExceededException : HttpException(HttpStatus.BAD_REQUEST, "Wallet Limit exceeded")

class PlatformFeeLessThanZeroException : Exception("Platform fee cannot be less than zero")

class InsufficientFundsException(val errorMessage: String = "Insufficient Funds"): Throwable()

class InsufficientInventoryException(val type: ESOPType): Throwable("Insufficient $type inventory"){}

class UserDoesNotExistException(val errorMessage: String): Throwable()

class InvalidPreOrderPlaceException(val errorList: List<String>): Throwable()