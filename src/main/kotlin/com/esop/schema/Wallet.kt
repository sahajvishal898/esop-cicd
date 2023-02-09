package com.esop.schema

import com.esop.constant.MAX_WALLET_CAPACITY
import com.esop.exceptions.InsufficientFundsException
import com.esop.exceptions.WalletLimitExceededException

class Wallet {
    private var freeMoney: Long = 0
    private var lockedMoney: Long = 0

    private fun totalMoneyInWallet(): Long {
        return freeMoney + lockedMoney
    }

    fun willWalletOverflowOnAdding(amount: Long) {
        if (amount + totalMoneyInWallet() > MAX_WALLET_CAPACITY) throw WalletLimitExceededException()
    }


    fun addMoneyToWallet(amountToBeAdded: Long) {
        willWalletOverflowOnAdding(amountToBeAdded)

        this.freeMoney = this.freeMoney + amountToBeAdded
    }

    fun moveMoneyFromFreeToLockedState(amountToBeLocked: Long) {
        if (this.freeMoney < amountToBeLocked) {
            throw InsufficientFundsException()
        }
        this.freeMoney = this.freeMoney - amountToBeLocked
        this.lockedMoney = this.lockedMoney + amountToBeLocked
    }

    fun getFreeMoney(): Long {
        return freeMoney
    }

    fun getLockedMoney(): Long {
        return lockedMoney
    }

    fun removeMoneyFromLockedState(amountToBeRemoved: Long) {
        this.lockedMoney = this.lockedMoney - amountToBeRemoved
    }

    fun moveMoneyFromLockedToFree(amount: Long) {
        removeMoneyFromLockedState(amount)
        addMoneyToWallet(amount)
    }
}