package com.esop.schema

import com.esop.dto.AddInventoryDTO
import com.esop.dto.AddWalletDTO

class User(
    var firstName: String,
    var lastName: String,
    var phoneNumber: String,
    var email: String,
    var username: String
) {
    val userWallet: Wallet = Wallet()
    val userNonPerfInventory: Inventory = Inventory(type = ESOPType.NON_PERFORMANCE)
    val userPerformanceInventory: Inventory = Inventory(type = ESOPType.PERFORMANCE)
    val orderList: ArrayList<Order> = ArrayList()

    fun addToWallet(walletData: AddWalletDTO): String {
        userWallet.addMoneyToWallet(walletData.price!!)
        return "${walletData.price} amount added to account."
    }

    fun addToInventory(inventoryData: AddInventoryDTO): String {
        if (inventoryData.esopType.toString().uppercase() == "NON_PERFORMANCE") {
            userNonPerfInventory.addESOPsToInventory(inventoryData.quantity!!)
            return "${inventoryData.quantity} Non-Performance ESOPs added to account."
        } else if (inventoryData.esopType.toString().uppercase() == "PERFORMANCE") {
            userPerformanceInventory.addESOPsToInventory(inventoryData.quantity!!)
            return "${inventoryData.quantity} Performance ESOPs added to account."
        }
        return "None"
    }

    fun lockPerformanceInventory(quantity: Long) {
        userPerformanceInventory.moveESOPsFromFreeToLockedState(quantity)
    }

    fun lockNonPerformanceInventory(quantity: Long) {
        userNonPerfInventory.moveESOPsFromFreeToLockedState(quantity)
    }

    fun lockAmount(price: Long) {
        userWallet.moveMoneyFromFreeToLockedState(price)
    }

    private fun getInventory(type: ESOPType): Inventory {
        if (type == ESOPType.PERFORMANCE) return userPerformanceInventory
        return userNonPerfInventory
    }

    fun transferLockedESOPsTo(buyer: User, esopType: ESOPType, currentTradeQuantity: Long) {
        this.getInventory(esopType).removeESOPsFromLockedState(currentTradeQuantity)
        buyer.getInventory(ESOPType.NON_PERFORMANCE).addESOPsToInventory(currentTradeQuantity)
    }
}