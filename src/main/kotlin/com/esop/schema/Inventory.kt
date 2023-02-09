package com.esop.schema

import com.esop.constant.MAX_INVENTORY_CAPACITY
import com.esop.exceptions.InsufficientInventoryException
import com.esop.exceptions.InventoryLimitExceededException


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

    fun moveESOPsFromFreeToLockedState(esopsToBeLocked: Long) {
        if (this.freeInventory < esopsToBeLocked) {
            throw InsufficientInventoryException(type)
        }
        this.freeInventory = this.freeInventory - esopsToBeLocked
        this.lockedInventory = this.lockedInventory + esopsToBeLocked
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