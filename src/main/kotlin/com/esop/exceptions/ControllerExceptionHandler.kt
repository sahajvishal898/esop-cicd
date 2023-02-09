package com.esop.exceptions

import com.fasterxml.jackson.core.JsonProcessingException
import io.micronaut.core.convert.exceptions.ConversionErrorException
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Error
import io.micronaut.web.router.exceptions.UnsatisfiedBodyRouteException
import javax.validation.ConstraintViolationException

@Controller
class ControllerExceptionHandler {

    @Error(exception = HttpException::class, global = true)
    fun onHttpException(exception: HttpException): HttpResponse<Map<String, ArrayList<String?>>>? {
        return HttpResponse.status<Map<String, ArrayList<String>>>(exception.status)
            .body(mapOf("errors" to arrayListOf(exception.message)))
    }

    @Error(exception = JsonProcessingException::class, global = true)
    fun onJSONProcessingException(): HttpResponse<Map<String, ArrayList<String>>> {
        return HttpResponse.badRequest(mapOf("errors" to arrayListOf("Invalid JSON format")))
    }

    @Error(exception = UnsatisfiedBodyRouteException::class, global = true)
    fun onUnsatisfiedBodyRouteException(): HttpResponse<Map<String, List<String?>>> {
        return HttpResponse.badRequest(mapOf("errors" to arrayListOf("Request body missing")))
    }

    @Error(status = HttpStatus.NOT_FOUND, global = true)
    fun onRouteNotFoundException(): HttpResponse<Map<String, List<String?>>> {
        return HttpResponse.notFound(mapOf("errors" to arrayListOf("Route not found")))
    }

    @Error(exception = ConversionErrorException::class, global = true)
    fun onConversionErrorException(ex: ConversionErrorException): HttpResponse<Map<String, ArrayList<String?>>>? {
        return HttpResponse.badRequest(mapOf("errors" to arrayListOf(ex.message)))
    }

    @Error(exception = ConstraintViolationException::class, global = true)
    fun onConstraintViolationException(ex: ConstraintViolationException): HttpResponse<Map<String, List<String>>> {
        return HttpResponse.badRequest(mapOf("errors" to ex.constraintViolations.map { it.message }))
    }

    @Error(exception = RuntimeException::class, global = true)
    fun onRuntimeException(ex: RuntimeException): HttpResponse<Map<String, ArrayList<String?>>>? {
        return HttpResponse.serverError(mapOf("errors" to arrayListOf(ex.message)))
    }

    @Error(global = true)
    fun userDoesNotExistException(exception: UserDoesNotExistException): HttpResponse<Map<String, List<String?>>>? {
        return HttpResponse.badRequest(mapOf("error" to listOf(exception.message)))
    }
    @Error(global = true)
    fun invalidPreOrderPlaceException(exception: InvalidPreOrderPlaceException): HttpResponse<Map<String, List<String>>>? {
        return HttpResponse.badRequest(mapOf("error" to exception.errorList))
    }
    @Error(global = true)
    fun insufficientFundsException(exception: InsufficientFundsException): HttpResponse<Map<String, List<String?>>>?{
        return HttpResponse.badRequest(mapOf("error" to listOf(exception.message)))
    }

    @Error(global = true)
    fun insufficientInventoryException(exception: InsufficientInventoryException): HttpResponse<Map<String, List<String?>>>?{
        return HttpResponse.badRequest(mapOf("error" to listOf(exception.message)))
    }

    @Error(global = true)
    fun globalError(e: Throwable): Map<String,List<String?>> {
        return mapOf("error" to listOf(e.message))
    }

}