package com.esop.service

import com.esop.repository.OrderRecords
import com.esop.repository.UserRecords
import com.esop.schema.ESOPType.PERFORMANCE
import com.esop.schema.Order
import com.esop.schema.OrderStatus.*
import com.esop.schema.OrderType.BUY
import com.esop.schema.OrderType.SELL
import com.esop.schema.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.lang.Thread.sleep

class OrderServiceTest {

    private lateinit var userRecords: UserRecords
    private lateinit var orderService: OrderService
    private lateinit var userService: UserService
    private lateinit var orderRecords: OrderRecords
    private lateinit var platformFeeService: PlatformFeeService

    @BeforeEach
    fun `It should create user`() {
        userRecords = UserRecords()
        platformFeeService = PlatformFeeService()
        userService = UserService(userRecords, platformFeeService)
        orderRecords = OrderRecords()
        orderService = OrderService(userService, orderRecords)

        val buyer1 = User("Sankaranarayanan", "M", "7550276216", "sankaranarayananm@sahaj.ai", "sankar")
        val seller1 = User("Kajal", "Pawar", "", "kajal@sahaj.ai", "kajal")
        val buyer2 = User("Aditya", "Tiwari", "", "aditya@sahaj.ai", "aditya")
        val seller2 = User("Arun", "Murugan", "", "arun@sahaj.ai", "arun")

        userRecords.addUser(buyer1)
        userRecords.addUser(seller1)
        userRecords.addUser(buyer2)
        userRecords.addUser(seller2)
    }

    @Test
    fun `It should place BUY order`() {
        //Arrange
        userRecords.getUser("sankar")!!.userWallet.addMoneyToWallet(100)
        val buyOrder = Order(10, BUY, 10, "sankar")
        //Act
        orderService.placeOrder(buyOrder)
        //Assert
        val actualOrder = orderRecords.getBuyOrderById(buyOrder.orderID)
        assertThat(buyOrder)
            .usingRecursiveComparison()
            .comparingOnlyFields("quantity", "type", "price", "userName")
            .isEqualTo(actualOrder)
    }

    @Test
    fun `It should place SELL order`() {
        //Arrange
        userRecords.getUser("kajal")!!.userNonPerfInventory.addESOPsToInventory(10)
        val sellOrder = Order(10, SELL, 10, "kajal")
        //Act
        orderService.placeOrder(sellOrder)
        //Assert
        val actualOrder = orderRecords.getSellOrderById(sellOrder.orderID)
        assertThat(sellOrder)
            .usingRecursiveComparison()
            .comparingOnlyFields("quantity", "type", "price", "userName")
            .isEqualTo(actualOrder)
    }

    @Test
    fun `It should match BUY order for existing SELL order`() {
        //Arrange
        userRecords.getUser("kajal")!!.userNonPerfInventory.addESOPsToInventory(50)
        val sellOrder = Order(10, SELL, 10, "kajal")
        orderService.placeOrder(sellOrder)

        userRecords.getUser("sankar")!!.userWallet.addMoneyToWallet(100)
        val buyOrder = Order(10, BUY, 10, "sankar")

        //Act
        orderService.placeOrder(buyOrder)

        //Assert
        assertEquals(40, userRecords.getUser("kajal")!!.userNonPerfInventory.getFreeInventory())
        assertEquals(10, userRecords.getUser("sankar")!!.userNonPerfInventory.getFreeInventory())
        assertEquals(98, userRecords.getUser("kajal")!!.userWallet.getFreeMoney())
        assertEquals(0, userRecords.getUser("sankar")!!.userWallet.getFreeMoney())
    }

    @Test
    fun `It should place 2 SELL orders followed by a BUY order where the BUY order is partial`() {
        //Arrange
        userRecords.getUser("kajal")!!.userNonPerfInventory.addESOPsToInventory(50)
        val sellOrderByKajal = Order(10, SELL, 10, "kajal")
        orderService.placeOrder(sellOrderByKajal)

        userRecords.getUser("arun")!!.userNonPerfInventory.addESOPsToInventory(50)
        val sellOrderByArun = Order(10, SELL, 10, "arun")
        orderService.placeOrder(sellOrderByArun)

        userRecords.getUser("sankar")!!.userWallet.addMoneyToWallet(250)
        val buyOrderBySankar = Order(25, BUY, 10, "sankar")

        orderService.placeOrder(buyOrderBySankar)

        //Assert
        assertEquals(40, userRecords.getUser("kajal")!!.userNonPerfInventory.getFreeInventory())
        assertEquals(40, userRecords.getUser("arun")!!.userNonPerfInventory.getFreeInventory())
        assertEquals(20, userRecords.getUser("sankar")!!.userNonPerfInventory.getFreeInventory())
        assertEquals(98, userRecords.getUser("kajal")!!.userWallet.getFreeMoney())
        assertEquals(98, userRecords.getUser("arun")!!.userWallet.getFreeMoney())
        assertEquals(50, userRecords.getUser("sankar")!!.userWallet.getLockedMoney())
        assertEquals(PARTIAL, buyOrderBySankar.orderStatus)
        assertEquals(COMPLETED, sellOrderByArun.orderStatus)
        assertEquals(COMPLETED, sellOrderByArun.orderStatus)
    }

    @Test
    fun `It should place 2 SELL orders followed by a BUY order where the BUY order is complete`() {
        //Arrange
        userRecords.getUser("kajal")!!.userNonPerfInventory.addESOPsToInventory(50)
        val sellOrderByKajal = Order(10, SELL, 10, "kajal")
        orderService.placeOrder(sellOrderByKajal)

        userRecords.getUser("arun")!!.userNonPerfInventory.addESOPsToInventory(50)
        val sellOrderByArun = Order(10, SELL, 10, "arun")
        orderService.placeOrder(sellOrderByArun)

        userRecords.getUser("sankar")!!.userWallet.addMoneyToWallet(250)
        val buyOrderBySankar = Order(20, BUY, 10, "sankar")

        //Act
        orderService.placeOrder(buyOrderBySankar)

        //Assert
        assertEquals(40, userRecords.getUser("kajal")!!.userNonPerfInventory.getFreeInventory())
        assertEquals(40, userRecords.getUser("arun")!!.userNonPerfInventory.getFreeInventory())
        assertEquals(20, userRecords.getUser("sankar")!!.userNonPerfInventory.getFreeInventory())
        assertEquals(98, userRecords.getUser("kajal")!!.userWallet.getFreeMoney())
        assertEquals(98, userRecords.getUser("arun")!!.userWallet.getFreeMoney())
        assertEquals(0, userRecords.getUser("sankar")!!.userWallet.getLockedMoney())
        assertEquals(COMPLETED, buyOrderBySankar.orderStatus)
        assertEquals(COMPLETED, sellOrderByKajal.orderStatus)
        assertEquals(COMPLETED, sellOrderByArun.orderStatus)
    }

    @Test
    fun `It should place 1 SELL orders followed by a BUY order where the BUY order is complete`() {
        //Arrange
        userRecords.getUser("kajal")!!.userNonPerfInventory.addESOPsToInventory(50)
        val sellOrderByKajal = Order(10, SELL, 10, "kajal")
        orderService.placeOrder(sellOrderByKajal)


        userRecords.getUser("sankar")!!.userWallet.addMoneyToWallet(250)
        val buyOrderBySankar = Order(5, BUY, 10, "sankar")

        //Act
        orderService.placeOrder(buyOrderBySankar)

        //Assert
        assertEquals(40, userRecords.getUser("kajal")!!.userNonPerfInventory.getFreeInventory())
        assertEquals(5, userRecords.getUser("sankar")!!.userNonPerfInventory.getFreeInventory())
        assertEquals(49, userRecords.getUser("kajal")!!.userWallet.getFreeMoney())
        assertEquals(0, userRecords.getUser("sankar")!!.userWallet.getLockedMoney())
        assertEquals(COMPLETED, buyOrderBySankar.orderStatus)
        assertEquals(PARTIAL, sellOrderByKajal.orderStatus)
    }

    @Test
    fun `It should place 1 SELL orders followed by a BUY order where the BUY order is partial`() {
        //Arrange
        userRecords.getUser("kajal")!!.userNonPerfInventory.addESOPsToInventory(50)
        val sellOrderByKajal = Order(10, SELL, 10, "kajal")
        orderService.placeOrder(sellOrderByKajal)

        userRecords.getUser("sankar")!!.userWallet.addMoneyToWallet(250)
        val buyOrderBySankar = Order(15, BUY, 10, "sankar")

        //Act
        orderService.placeOrder(buyOrderBySankar)

        //Assert
        assertEquals(40, userRecords.getUser("kajal")!!.userNonPerfInventory.getFreeInventory())
        assertEquals(10, userRecords.getUser("sankar")!!.userNonPerfInventory.getFreeInventory())
        assertEquals(98, userRecords.getUser("kajal")!!.userWallet.getFreeMoney())
        assertEquals(50, userRecords.getUser("sankar")!!.userWallet.getLockedMoney())
        assertEquals(PARTIAL, buyOrderBySankar.orderStatus)
        assertEquals(COMPLETED, sellOrderByKajal.orderStatus)
    }

    @Test
    fun `It should place 2 BUY orders followed by a SELL order where the SELL order is partial`() {
        //Arrange
        userRecords.getUser("sankar")!!.userWallet.addMoneyToWallet(100)
        val buyOrderBySankar = Order(10, BUY, 10, "sankar")
        orderService.placeOrder(buyOrderBySankar)


        userRecords.getUser("aditya")!!.userWallet.addMoneyToWallet(100)
        val buyOrderByAditya = Order(10, BUY, 10, "aditya")
        orderService.placeOrder(buyOrderByAditya)

        userRecords.getUser("kajal")!!.userNonPerfInventory.addESOPsToInventory(50)
        val sellOrderByKajal = Order(25, SELL, 10, "kajal")

        //Act
        orderService.placeOrder(sellOrderByKajal)

        //Assert
        assertEquals(25, userRecords.getUser("kajal")!!.userNonPerfInventory.getFreeInventory())
        assertEquals(10, userRecords.getUser("sankar")!!.userNonPerfInventory.getFreeInventory())
        assertEquals(10, userRecords.getUser("aditya")!!.userNonPerfInventory.getFreeInventory())
        assertEquals(196, userRecords.getUser("kajal")!!.userWallet.getFreeMoney())
        assertEquals(0, userRecords.getUser("sankar")!!.userWallet.getFreeMoney())
        assertEquals(0, userRecords.getUser("sankar")!!.userWallet.getFreeMoney())
        assertEquals(PARTIAL, orderRecords.getSellOrderById(sellOrderByKajal.orderID)!!.orderStatus)
        assertEquals(COMPLETED, buyOrderBySankar.orderStatus)
        assertEquals(COMPLETED, buyOrderByAditya.orderStatus)
    }

    @Test
    fun `It should place 2 BUY orders followed by a SELL order where the SELL order is complete`() {
        //Arrange
        userRecords.getUser("kajal")!!.userWallet.addMoneyToWallet(100)
        val buyOrderByKajal = Order(10, BUY, 10, "kajal")
        orderService.placeOrder(buyOrderByKajal)

        userRecords.getUser("arun")!!.userWallet.addMoneyToWallet(100)
        val buyOrderByArun = Order(10, BUY, 10, "arun")
        orderService.placeOrder(buyOrderByArun)

        userRecords.getUser("sankar")!!.userNonPerfInventory.addESOPsToInventory(30)
        val sellOrderBySankar = Order(20, SELL, 10, "sankar")

        //Act
        orderService.placeOrder(sellOrderBySankar)

        //Assert
        assertEquals(10, userRecords.getUser("kajal")!!.userNonPerfInventory.getFreeInventory())
        assertEquals(0, userRecords.getUser("kajal")!!.userWallet.getFreeMoney())

        assertEquals(10, userRecords.getUser("arun")!!.userNonPerfInventory.getFreeInventory())
        assertEquals(0, userRecords.getUser("arun")!!.userWallet.getFreeMoney())

        assertEquals(10, userRecords.getUser("sankar")!!.userNonPerfInventory.getFreeInventory())
        assertEquals(98 + 98, userRecords.getUser("sankar")!!.userWallet.getFreeMoney())

        assertEquals(COMPLETED, sellOrderBySankar.orderStatus)
        assertEquals(COMPLETED, buyOrderByKajal.orderStatus)
        assertEquals(COMPLETED, buyOrderByArun.orderStatus)
    }

    @Test
    fun `It should match BUY order for existing SELL order for PERFORMANCE esop type`() {
        //Arrange
        userRecords.getUser("kajal")!!.userPerformanceInventory.addESOPsToInventory(50)
        val sellOrder = Order(10, SELL, 10, "kajal", PERFORMANCE)
        orderService.placeOrder(sellOrder)

        userRecords.getUser("sankar")!!.userWallet.addMoneyToWallet(100)
        val buyOrder = Order(10, BUY, 10, "sankar")

        //Act
        orderService.placeOrder(buyOrder)

        //Assert
        assertEquals(40, userRecords.getUser("kajal")!!.userPerformanceInventory.getFreeInventory())
        assertEquals(10, userRecords.getUser("sankar")!!.userNonPerfInventory.getFreeInventory())
        assertEquals(100, userRecords.getUser("kajal")!!.userWallet.getFreeMoney())
        assertEquals(0, userRecords.getUser("sankar")!!.userWallet.getFreeMoney())
    }

    @Test
    fun `It should match SELL order for PERFORMANCE for existing BUY order`() {
        //Arrange
        userRecords.getUser("sankar")!!.userWallet.addMoneyToWallet(100)
        val buyOrder = Order(10, BUY, 10, "sankar")
        orderService.placeOrder(buyOrder)

        userRecords.getUser("kajal")!!.userPerformanceInventory.addESOPsToInventory(50)
        val sellOrder = Order(10, SELL, 10, "kajal", PERFORMANCE)

        //Act
        orderService.placeOrder(sellOrder)

        //Assert
        assertEquals(40, userRecords.getUser("kajal")!!.userPerformanceInventory.getFreeInventory())
        assertEquals(10, userRecords.getUser("sankar")!!.userNonPerfInventory.getFreeInventory())
        assertEquals(100, userRecords.getUser("kajal")!!.userWallet.getFreeMoney())
        assertEquals(0, userRecords.getUser("sankar")!!.userWallet.getFreeMoney())
    }

    @Test
    fun `It should match SELL order for existing BUY order where SELL order is complete`() {
        //Arrange
        userRecords.getUser("sankar")!!.userWallet.addMoneyToWallet(200)
        val buyOrder = Order(20, BUY, 10, "sankar")
        orderService.placeOrder(buyOrder)

        userRecords.getUser("kajal")!!.userNonPerfInventory.addESOPsToInventory(50)
        val sellOrder = Order(10, SELL, 10, "kajal")

        //Act
        orderService.placeOrder(sellOrder)

        //Assert
        assertEquals(40, userRecords.getUser("kajal")!!.userNonPerfInventory.getFreeInventory())
        assertEquals(10, userRecords.getUser("sankar")!!.userNonPerfInventory.getFreeInventory())
        assertEquals(98, userRecords.getUser("kajal")!!.userWallet.getFreeMoney())
        assertEquals(0, userRecords.getUser("sankar")!!.userWallet.getFreeMoney())
    }

    @Test
    fun `It should place 2 SELL orders where one SELL order is of PERFORMANCE esop type and other is of NON-PERFORMANCE esop type followed by a BUY order where the BUY order is complete`() {
        //Arrange
        userRecords.getUser("kajal")!!.userNonPerfInventory.addESOPsToInventory(50)
        val sellOrderByKajal = Order(10, SELL, 10, "kajal")
        orderService.placeOrder(sellOrderByKajal)

        userRecords.getUser("arun")!!.userPerformanceInventory.addESOPsToInventory(50)
        val sellOrderByArun = Order(10, SELL, 10, "arun", PERFORMANCE)
        orderService.placeOrder(sellOrderByArun)

        userRecords.getUser("sankar")!!.userWallet.addMoneyToWallet(250)
        val buyOrderBySankar = Order(20, BUY, 10, "sankar")

        //Act
        orderService.placeOrder(buyOrderBySankar)

        //Assert
        assertEquals(40, userRecords.getUser("kajal")!!.userNonPerfInventory.getFreeInventory())
        assertEquals(40, userRecords.getUser("arun")!!.userPerformanceInventory.getFreeInventory())
        assertEquals(20, userRecords.getUser("sankar")!!.userNonPerfInventory.getFreeInventory())
        assertEquals(98, userRecords.getUser("kajal")!!.userWallet.getFreeMoney())
        assertEquals(100, userRecords.getUser("arun")!!.userWallet.getFreeMoney())
        assertEquals(0, userRecords.getUser("sankar")!!.userWallet.getLockedMoney())
        assertEquals(COMPLETED, buyOrderBySankar.orderStatus)
        assertEquals(COMPLETED, sellOrderByKajal.orderStatus)
        assertEquals(COMPLETED, sellOrderByArun.orderStatus)
    }

    @Test
    fun `It should place 2 SELL orders of PERFORMANCE esop type followed by a BUY order where the BUY order is complete`() {
        //Arrange
        userRecords.getUser("kajal")!!.userPerformanceInventory.addESOPsToInventory(50)
        val sellOrderByKajal = Order(10, SELL, 10, "kajal", PERFORMANCE)
        orderService.placeOrder(sellOrderByKajal)

        userRecords.getUser("arun")!!.userPerformanceInventory.addESOPsToInventory(50)
        val sellOrderByArun = Order(10, SELL, 10, "arun", PERFORMANCE)
        orderService.placeOrder(sellOrderByArun)

        userRecords.getUser("sankar")!!.userWallet.addMoneyToWallet(250)
        val buyOrderBySankar = Order(20, BUY, 10, "sankar")

        //Act
        orderService.placeOrder(buyOrderBySankar)

        //Assert
        assertEquals(40, userRecords.getUser("kajal")!!.userPerformanceInventory.getFreeInventory())
        assertEquals(40, userRecords.getUser("arun")!!.userPerformanceInventory.getFreeInventory())
        assertEquals(20, userRecords.getUser("sankar")!!.userNonPerfInventory.getFreeInventory())
        assertEquals(100, userRecords.getUser("kajal")!!.userWallet.getFreeMoney())
        assertEquals(100, userRecords.getUser("arun")!!.userWallet.getFreeMoney())
        assertEquals(0, userRecords.getUser("sankar")!!.userWallet.getLockedMoney())
        assertEquals(COMPLETED, buyOrderBySankar.orderStatus)
        assertEquals(COMPLETED, sellOrderByKajal.orderStatus)
        assertEquals(COMPLETED, sellOrderByArun.orderStatus)
    }

    @Test
    fun `It should place 2 SELL orders of PERFORMANCE esop type followed by a BUY order where higher timestamp order placed first`() {
        //Arrange
        userRecords.getUser("kajal")!!.userPerformanceInventory.addESOPsToInventory(50)
        val sellOrderByKajal = Order(10, SELL, 10, "kajal", PERFORMANCE)

        sleep(10)
        userRecords.getUser("arun")!!.userPerformanceInventory.addESOPsToInventory(50)
        val sellOrderByArun = Order(10, SELL, 10, "arun", PERFORMANCE)
        orderService.placeOrder(sellOrderByArun)
        orderService.placeOrder(sellOrderByKajal)

        userRecords.getUser("sankar")!!.userWallet.addMoneyToWallet(250)
        val buyOrderBySankar = Order(20, BUY, 10, "sankar")

        //Act
        orderService.placeOrder(buyOrderBySankar)

        //Assert
        assertEquals(40, userRecords.getUser("kajal")!!.userPerformanceInventory.getFreeInventory())
        assertEquals(40, userRecords.getUser("arun")!!.userPerformanceInventory.getFreeInventory())
        assertEquals(20, userRecords.getUser("sankar")!!.userNonPerfInventory.getFreeInventory())
        assertEquals(100, userRecords.getUser("kajal")!!.userWallet.getFreeMoney())
        assertEquals(100, userRecords.getUser("arun")!!.userWallet.getFreeMoney())
        assertEquals(0, userRecords.getUser("sankar")!!.userWallet.getLockedMoney())
        assertEquals(COMPLETED, buyOrderBySankar.orderStatus)
        assertEquals(COMPLETED, sellOrderByKajal.orderStatus)
        assertEquals(COMPLETED, sellOrderByArun.orderStatus)
    }

    @Test
    fun `It should place 2 SELL orders of NON-PERFORMANCE esop type followed by a BUY order where higher timestamp order placed first`() {
        //Arrange
        userRecords.getUser("kajal")!!.userNonPerfInventory.addESOPsToInventory(50)
        val sellOrderByKajal = Order(10, SELL, 10, "kajal")

        sleep(10)
        userRecords.getUser("arun")!!.userNonPerfInventory.addESOPsToInventory(50)
        val sellOrderByArun = Order(10, SELL, 10, "arun")
        orderService.placeOrder(sellOrderByArun)
        orderService.placeOrder(sellOrderByKajal)

        userRecords.getUser("sankar")!!.userWallet.addMoneyToWallet(250)
        val buyOrderBySankar = Order(20, BUY, 10, "sankar")

        //Act
        orderService.placeOrder(buyOrderBySankar)

        //Assert
        assertEquals(40, userRecords.getUser("kajal")!!.userNonPerfInventory.getFreeInventory())
        assertEquals(40, userRecords.getUser("arun")!!.userNonPerfInventory.getFreeInventory())
        assertEquals(20, userRecords.getUser("sankar")!!.userNonPerfInventory.getFreeInventory())
        assertEquals(98, userRecords.getUser("kajal")!!.userWallet.getFreeMoney())
        assertEquals(98, userRecords.getUser("arun")!!.userWallet.getFreeMoney())
        assertEquals(0, userRecords.getUser("sankar")!!.userWallet.getLockedMoney())
        assertEquals(COMPLETED, buyOrderBySankar.orderStatus)
        assertEquals(COMPLETED, sellOrderByKajal.orderStatus)
        assertEquals(COMPLETED, sellOrderByArun.orderStatus)
    }

    @Test
    fun `It should place 2 SELL orders of NON-PERFORMANCE esop type followed by a BUY order where SELL order price is different`() {
        //Arrange
        userRecords.getUser("kajal")!!.userNonPerfInventory.addESOPsToInventory(50)
        val sellOrderByKajal = Order(10, SELL, 20, "kajal")
        orderService.placeOrder(sellOrderByKajal)

        userRecords.getUser("arun")!!.userNonPerfInventory.addESOPsToInventory(50)
        val sellOrderByArun = Order(10, SELL, 10, "arun")
        orderService.placeOrder(sellOrderByArun)


        userRecords.getUser("sankar")!!.userWallet.addMoneyToWallet(400)
        val buyOrderBySankar = Order(20, BUY, 20, "sankar")

        //Act
        orderService.placeOrder(buyOrderBySankar)

        //Assert
        assertEquals(40, userRecords.getUser("kajal")!!.userNonPerfInventory.getFreeInventory())
        assertEquals(40, userRecords.getUser("arun")!!.userNonPerfInventory.getFreeInventory())
        assertEquals(20, userRecords.getUser("sankar")!!.userNonPerfInventory.getFreeInventory())
        assertEquals(196, userRecords.getUser("kajal")!!.userWallet.getFreeMoney())
        assertEquals(98, userRecords.getUser("arun")!!.userWallet.getFreeMoney())
        assertEquals(0, userRecords.getUser("sankar")!!.userWallet.getLockedMoney())
        assertEquals(COMPLETED, buyOrderBySankar.orderStatus)
        assertEquals(COMPLETED, sellOrderByKajal.orderStatus)
        assertEquals(COMPLETED, sellOrderByArun.orderStatus)
    }

    @Test
    fun `It should place 2 SELL orders of NON-PERFORMANCE esop type followed by a BUY order where lower SELL order price is placed first`() {
        //Arrange
        userRecords.getUser("kajal")!!.userNonPerfInventory.addESOPsToInventory(50)
        val sellOrderByKajal = Order(10, SELL, 20, "kajal")

        userRecords.getUser("arun")!!.userNonPerfInventory.addESOPsToInventory(50)
        val sellOrderByArun = Order(10, SELL, 10, "arun")
        orderService.placeOrder(sellOrderByArun)
        orderService.placeOrder(sellOrderByKajal)

        userRecords.getUser("sankar")!!.userWallet.addMoneyToWallet(400)
        val buyOrderBySankar = Order(20, BUY, 20, "sankar")

        //Act
        orderService.placeOrder(buyOrderBySankar)

        //Assert
        assertEquals(40, userRecords.getUser("kajal")!!.userNonPerfInventory.getFreeInventory())
        assertEquals(40, userRecords.getUser("arun")!!.userNonPerfInventory.getFreeInventory())
        assertEquals(20, userRecords.getUser("sankar")!!.userNonPerfInventory.getFreeInventory())
        assertEquals(196, userRecords.getUser("kajal")!!.userWallet.getFreeMoney())
        assertEquals(98, userRecords.getUser("arun")!!.userWallet.getFreeMoney())
        assertEquals(0, userRecords.getUser("sankar")!!.userWallet.getLockedMoney())
        assertEquals(COMPLETED, buyOrderBySankar.orderStatus)
        assertEquals(COMPLETED, sellOrderByKajal.orderStatus)
        assertEquals(COMPLETED, sellOrderByArun.orderStatus)
    }

    @Test
    fun `It should place 2 SELL orders and 2 BUY order where one BUY order is partial and one is pending`() {
        //Arrange
        userRecords.getUser("sankar")!!.userWallet.addMoneyToWallet(1000)
        val firstBuyOrderBySankar = Order(10, BUY, 10, "sankar")
        orderService.placeOrder(firstBuyOrderBySankar)

        userRecords.getUser("kajal")!!.userNonPerfInventory.addESOPsToInventory(5)
        val firstSellOrderByKajal = Order(5, SELL, 25, "kajal")
        orderService.placeOrder(firstSellOrderByKajal)

        userRecords.getUser("kajal")!!.userPerformanceInventory.addESOPsToInventory(5)
        val secondSellOrderByKajal = Order(5, SELL, 25, "kajal", PERFORMANCE)
        orderService.placeOrder(secondSellOrderByKajal)

        val secondBuyOrderBySankar = Order(15, BUY, 25, "sankar")


        orderService.placeOrder(secondBuyOrderBySankar)


        //Assert
        assertEquals(0, userRecords.getUser("kajal")!!.userNonPerfInventory.getFreeInventory())
        assertEquals(10, userRecords.getUser("sankar")!!.userNonPerfInventory.getFreeInventory())
        assertEquals(248, userRecords.getUser("kajal")!!.userWallet.getFreeMoney())
        assertEquals(225, userRecords.getUser("sankar")!!.userWallet.getLockedMoney())
        assertEquals(525, userRecords.getUser("sankar")!!.userWallet.getFreeMoney())
        assertEquals(PENDING, firstBuyOrderBySankar.orderStatus)
        assertEquals(PARTIAL, secondBuyOrderBySankar.orderStatus)
        assertEquals(COMPLETED, firstSellOrderByKajal.orderStatus)
        assertEquals(COMPLETED, secondSellOrderByKajal.orderStatus)
    }

}

