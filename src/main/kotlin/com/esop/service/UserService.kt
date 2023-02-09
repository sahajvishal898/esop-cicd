package com.esop.service

import com.esop.constant.errors
import com.esop.dto.AddInventoryDTO
import com.esop.dto.AddWalletDTO
import com.esop.dto.UserCreationDTO
import com.esop.exceptions.UserDoesNotExistException
import com.esop.repository.UserRecords
import com.esop.schema.ESOPType
import com.esop.schema.ESOPType.NON_PERFORMANCE
import com.esop.schema.ESOPType.PERFORMANCE
import com.esop.schema.Order
import com.esop.schema.OrderType.BUY
import com.esop.schema.OrderType.SELL
import com.esop.schema.User
import jakarta.inject.Singleton

@Singleton
class UserService(
    private val userRecords: UserRecords,
    private val platformFeeService: PlatformFeeService
) {
    fun getUser(userName: String): User? {
        return userRecords.getUser(userName)
    }

    private fun checkAndLockInventory(order: Order, user: User) {
        when (order.getEsopType()) {
            PERFORMANCE -> user.lockPerformanceInventory(order.getQuantity())
            NON_PERFORMANCE -> user.lockNonPerformanceInventory(order.getQuantity())
        }
    }

    fun checkUserDetailsForOrder(order: Order) {
        if (!userRecords.checkIfUserExists(order.getUserName())) {
            throw UserDoesNotExistException()
        }

        val user = userRecords.getUser(order.getUserName())!!
        val wallet = user.userWallet
        val nonPerformanceInventory = user.userNonPerfInventory

        when (order.getType()) {
            BUY -> {
                nonPerformanceInventory.willInventoryOverflowOnAdding(order.getQuantity())
                user.lockAmount(order.getPrice() * order.getQuantity())
            }

            SELL -> {
                wallet.willWalletOverflowOnAdding(order.getPrice() * order.getQuantity())
                checkAndLockInventory(order, user)
            }
        }
    }


    fun registerUser(userData: UserCreationDTO): Map<String, String> {
        val user = User(
            userData.firstName!!.trim(),
            userData.lastName!!.trim(),
            userData.phoneNumber!!,
            userData.email!!,
            userData.username!!
        )
        userRecords.addUser(user)
        userRecords.addEmail(user.email)
        userRecords.addPhoneNumber(user.phoneNumber)
        return mapOf(
            "firstName" to user.firstName,
            "lastName" to user.lastName,
            "phoneNumber" to user.phoneNumber,
            "email" to user.email,
            "username" to user.username
        )
    }

    fun accountInformation(userName: String): Map<String, Any?> {

        if (!userRecords.checkIfUserExists(userName)) {
            throw UserDoesNotExistException()
        }

        val user = userRecords.getUser(userName)!!
        return mapOf(
            "firstName" to user.firstName,
            "lastName" to user.lastName,
            "phoneNumber" to user.phoneNumber,
            "email" to user.email,
            "wallet" to mapOf(
                "free" to user.userWallet.getFreeMoney(), "locked" to user.userWallet.getLockedMoney()
            ),
            "inventory" to arrayListOf<Any>(
                mapOf(
                    "type" to "PERFORMANCE",
                    "free" to user.userPerformanceInventory.getFreeInventory(),
                    "locked" to user.userPerformanceInventory.getLockedInventory()
                ), mapOf(
                    "type" to "NON_PERFORMANCE",
                    "free" to user.userNonPerfInventory.getFreeInventory(),
                    "locked" to user.userNonPerfInventory.getLockedInventory()
                )
            )
        )
    }


    fun addingInventory(inventoryData: AddInventoryDTO, userName: String): Map<String, Any> {
        if (!userRecords.checkIfUserExists(userName)) {
            throw UserDoesNotExistException()
        }
        return mapOf("message" to userRecords.getUser(userName)!!.addToInventory(inventoryData))
    }

    fun addingMoney(walletData: AddWalletDTO, userName: String): Map<String, Any> {
        if (!userRecords.checkIfUserExists(userName)) {
            throw UserDoesNotExistException()
        }
        return mapOf("message" to userRecords.getUser(userName)!!.addToWallet(walletData))
    }

    fun checkIfUserExists(userName: String): Boolean {
        return userRecords.checkIfUserExists(userName)
    }

    fun updateUserDetails(currentTradeQuantity: Long, sellerOrder: Order, buyerOrder: Order) {
        val sellAmount = sellerOrder.getPrice() * (currentTradeQuantity)
        val buyer = getUser(buyerOrder.getUserName())!!
        val seller = getUser(sellerOrder.getUserName())!!

        updateWalletBalances(sellAmount, sellerOrder.getEsopType(), buyer, seller)

        seller.transferLockedESOPsTo(buyer, sellerOrder.getEsopType(), currentTradeQuantity)

        val amountToBeReleased = (buyerOrder.getPrice() - sellerOrder.getPrice()) * (currentTradeQuantity)
        buyer.userWallet.moveMoneyFromLockedToFree(amountToBeReleased)
    }


    private fun updateWalletBalances(
        sellAmount: Long, esopType: ESOPType, buyer: User, seller: User
    ) {
        val adjustedSellAmount = platformFeeService.deductPlatformFeeFrom(sellAmount, esopType)

        buyer.userWallet.removeMoneyFromLockedState(sellAmount)
        seller.userWallet.addMoneyToWallet(adjustedSellAmount)
    }

}