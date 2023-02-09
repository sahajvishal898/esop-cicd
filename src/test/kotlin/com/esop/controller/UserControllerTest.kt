package com.esop.controller

import com.esop.dto.AddInventoryDTO
import com.esop.dto.AddWalletDTO
import com.esop.dto.CreateOrderDTO
import com.esop.dto.UserCreationDTO
import com.esop.schema.ESOPType.*
import com.esop.service.UserService
import io.micronaut.http.HttpStatus
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import jakarta.inject.Inject
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.hasItem
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@MicronautTest(rebuildContext = true)
class UserControllerTest {
    @Inject
    lateinit var userService: UserService

    @BeforeEach
    fun `it should create two users`() {
        val user1 = UserCreationDTO("Sankar", "M", "+917550276216", "sankar@sahaj.ai", "sankar06")
        val user2 = UserCreationDTO("Kajal", "Test", "+919182838479", "kajal@sahaj.ai", "kajal")

        userService.registerUser(user1)
        userService.registerUser(user2)
    }

    @Test
    fun `it should place buy order`(spec: RequestSpecification) {
        // Arrange
        val quantity = 10L
        val price = 10L
        val type = "BUY"
        val order = CreateOrderDTO(type, quantity, price)
        userService.addingMoney(AddWalletDTO(price * quantity), "sankar06")

        // Act
        spec
            .given()
            .body(
                order
            ).contentType(ContentType.JSON)
            .`when`()
            .pathParam("username", "sankar06")
            .post("/user/{username}/order")
            .then()
            .statusCode(HttpStatus.OK.code)
            .body(
                "orderId", equalTo(1),
                "quantity", equalTo(quantity.toInt()),
                "price", equalTo(price.toInt()),
                "type", equalTo(type)
            )
    }

    @Test
    fun `it should place sell order`(spec: RequestSpecification) {
        // Arrange
        val quantity = 10L
        val price = 10L
        val type = "SELL"
        val order = CreateOrderDTO(type, quantity, price)
        userService.addingInventory(AddInventoryDTO(quantity, NON_PERFORMANCE.toString()), "sankar06")

        // Act
        spec
            .given()
            .body(
                order
            ).contentType(ContentType.JSON)
            .`when`()
            .pathParam("username", "sankar06")
            .post("/user/{username}/order")
            .then()
            .statusCode(HttpStatus.OK.code)
            .body(
                "orderId", equalTo(1),
                "quantity", equalTo(quantity.toInt()),
                "price", equalTo(price.toInt()),
                "type", equalTo(type)
            )
    }

    @Test
    fun `it should throw user does not exist`(spec: RequestSpecification) {
        // Arrange
        val quantity = 10L
        val price = 10L
        val type = "SELL"
        val order = CreateOrderDTO(type, quantity, price)

        // Act
        spec
            .given()
            .body(
                order
            ).contentType(ContentType.JSON)
            .`when`()
            .pathParam("username", "randomName")
            .post("/user/{username}/order")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.code)
            .body(
                "error", hasItem("User not found"),
            )
    }

    @Test
    fun `it should throw insufficient inventory`(spec: RequestSpecification) {
        // Arrange
        val quantity = 10L
        val price = 10L
        val type = "SELL"
        val order = CreateOrderDTO(type, quantity, price)

        // Act
        spec
            .given()
            .body(
                order
            ).contentType(ContentType.JSON)
            .`when`()
            .pathParam("username", "kajal")
            .post("/user/{username}/order")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.code)
            .body(
                "error", hasItem("Insufficient NON_PERFORMANCE inventory"),
            )
    }

    @Test
    fun `it should throw insufficient funds`(spec: RequestSpecification) {
        // Arrange
        val quantity = 10L
        val price = 10L
        val type = "BUY"
        val order = CreateOrderDTO(type, quantity, price)

        // Act
        spec
            .given()
            .body(
                order
            ).contentType(ContentType.JSON)
            .`when`()
            .pathParam("username", "kajal")
            .post("/user/{username}/order")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.code)
            .body(
                "error", hasItem("Insufficient Funds"),
            )
    }
}