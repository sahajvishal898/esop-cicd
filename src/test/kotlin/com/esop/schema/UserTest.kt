package com.esop.schema

import com.esop.schema.ESOPType.PERFORMANCE
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class UserTest {

    @Test
    fun `it should transfer Performance ESOPs to the other user`() {
        val userOne = User("Sankaranarayanan", "M", "7550276216", "sankaranarayananm@sahaj.ai", "sankar")
        val userTwo = User("Aditya", "Tiwari", "", "aditya@sahaj.ai", "aditya")
        userOne.userPerformanceInventory.addESOPsToInventory(20)
        userOne.userPerformanceInventory.moveESOPsFromFreeToLockedState(10)

        userOne.transferLockedESOPsTo(userTwo, PERFORMANCE, 10)

        Assertions.assertEquals(10, userOne.userPerformanceInventory.getFreeInventory())
        Assertions.assertEquals(10, userTwo.userNonPerfInventory.getFreeInventory())
    }
}