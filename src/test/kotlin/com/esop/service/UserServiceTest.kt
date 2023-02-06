package com.esop.service

import com.esop.dto.UserCreationDTO
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

@MicronautTest
class UserServiceTest{

    @Inject
    private lateinit var userService: UserService

    @Test
    fun `should register a valid user`(){
        val user = UserCreationDTO("Sankar","M","+917550276216","sankar@sahaj.ai","sankar06")
        val expected = mapOf(
            "firstName" to "Sankar",
            "lastName" to "M",
            "phoneNumber" to "+917550276216",
            "email" to "sankar@sahaj.ai",
            "username" to "sankar06"
        )

        //Action
        val response = userService.registerUser(user)

        //Assert
        assertEquals(response,expected)
    }
}