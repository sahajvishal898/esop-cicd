package com.esop.schema

import com.esop.InventoryLimitExceededException
import com.esop.constant.MAX_INVENTORY_CAPACITY
import java.util.*


enum class ESOPType{
    PERFORMANCE,
    NON_PERFORMANCE
}

class Inventory(
    private var freeInventory: Long = 0L,
    private var lockedInventory: Long = 0L,
    private var type: ESOPType
) {

    private fun totalESOPQuantity(): Long {
        return freeInventory + lockedInventory
    }

    fun willInventoryOverflowOnAdding(quantity: Long) {
        if (quantity + totalESOPQuantity() > MAX_INVENTORY_CAPACITY) throw InventoryLimitExceededException()
    }

    fun addESOPsToInventory(esopsToBeAdded: Long) {
        willInventoryOverflowOnAdding(esopsToBeAdded)

        this.freeInventory = this.freeInventory + esopsToBeAdded
    }

    fun moveESOPsFromFreeToLockedState(esopsToBeLocked: Long): String {
        if (this.freeInventory < esopsToBeLocked) {
            return "Insufficient $type inventory."
        }
        this.freeInventory = this.freeInventory - esopsToBeLocked
        this.lockedInventory = this.lockedInventory + esopsToBeLocked
        return "SUCCESS"
    }

    fun getFreeInventory(): Long {
        return freeInventory
    }

    fun getLockedInventory(): Long {
        return lockedInventory
    }

    fun removeESOPsFromLockedState(esopsToBeRemoved: Long) {
        this.lockedInventory = this.lockedInventory - esopsToBeRemoved
    }
}