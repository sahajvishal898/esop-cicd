package com.esop.controller


import com.esop.dto.AddInventoryDTO
import com.esop.dto.AddWalletDTO
import com.esop.dto.CreateOrderDTO
import com.esop.dto.UserCreationDTO
import com.esop.exceptions.*
import com.esop.schema.ESOPType
import com.esop.schema.ESOPType.*
import com.esop.schema.Order
import com.esop.schema.OrderType
import com.esop.service.*
import io.micronaut.http.*
import io.micronaut.http.annotation.*
import io.micronaut.validation.Validated
import jakarta.inject.Inject
import javax.validation.Valid


@Validated
@Controller("/user")
class UserController {

    @Inject
    lateinit var userService: UserService

    @Inject
    lateinit var orderService: OrderService


    @Post(uri = "/register", consumes = [MediaType.APPLICATION_JSON], produces = [MediaType.APPLICATION_JSON])
    fun register(@Body @Valid userData: UserCreationDTO): HttpResponse<*> {
        val newUser = this.userService.registerUser(userData)
        if (newUser["error"] != null) {
            return HttpResponse.badRequest(newUser)
        }
        return HttpResponse.ok(newUser)
    }

    @Post(uri = "/{userName}/order", consumes = [MediaType.APPLICATION_JSON], produces = [MediaType.APPLICATION_JSON])
    fun order(userName: String, @Body @Valid orderData: CreateOrderDTO): Any? {
        var esopType = NON_PERFORMANCE
        orderData.esopType?.let{esopType = ESOPType.valueOf(orderData.esopType!!)}

        val order = Order(orderData.quantity!!, OrderType.valueOf(orderData.type!!.uppercase()), orderData.price!!, userName, esopType)

        val placedOrderId = orderService.placeOrder(order)
        return HttpResponse.ok(
            mapOf(
                "orderId" to placedOrderId,
                "quantity" to orderData.quantity,
                "type" to orderData.type,
                "price" to orderData.price
            )
        )
    }

    @Get(uri = "/{userName}/accountInformation", produces = [MediaType.APPLICATION_JSON])
    fun getAccountInformation(userName: String): HttpResponse<*> {
        val userData = this.userService.accountInformation(userName)

        if (userData["error"] != null) {
            return HttpResponse.badRequest(userData)
        }

        return HttpResponse.ok(userData)
    }

    @Post(
        uri = "{userName}/inventory",
        consumes = [MediaType.APPLICATION_JSON],
        produces = [MediaType.APPLICATION_JSON]
    )
    fun addInventory(userName: String, @Body @Valid body: AddInventoryDTO): HttpResponse<*> {
        val newInventory = this.userService.addingInventory(body, userName)

        if (newInventory["error"] != null) {
            return HttpResponse.badRequest(newInventory)
        }
        return HttpResponse.ok(newInventory)
    }


    @Post(uri = "{userName}/wallet", consumes = [MediaType.APPLICATION_JSON], produces = [MediaType.APPLICATION_JSON])
    fun addWallet(userName: String, @Body @Valid body: AddWalletDTO): HttpResponse<*> {
        val addedMoney = this.userService.addingMoney(body, userName)

        if (addedMoney["error"] != null) {
            return HttpResponse.badRequest(addedMoney)
        }
        return HttpResponse.ok(addedMoney)

    }

    @Get(uri = "/{userName}/order", produces = [MediaType.APPLICATION_JSON])
    fun orderHistory(userName: String): HttpResponse<*> {
        val orderHistoryData = orderService.orderHistory(userName)
        if (orderHistoryData is Map<*, *>) {
            return HttpResponse.badRequest(orderHistoryData)
        }
        return HttpResponse.ok(orderHistoryData)
    }
}