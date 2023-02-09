package com.esop.exceptions

import com.fasterxml.jackson.core.JsonProcessingException
import io.micronaut.core.convert.exceptions.ConversionErrorException
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Error
import io.micronaut.web.router.exceptions.UnsatisfiedBodyRouteException
import javax.validation.ConstraintViolationException

class ControllerExceptionHandler {

    @Error(exception = HttpException::class)
    fun onHttpException(exception: HttpException): HttpResponse<Map<String, ArrayList<String?>>>? {
        return HttpResponse.status<Map<String, ArrayList<String>>>(exception.status)
            .body(mapOf("errors" to arrayListOf(exception.message)))
    }

    @Error(exception = JsonProcessingException::class)
    fun onJSONProcessingException(ex: JsonProcessingException): HttpResponse<Map<String, ArrayList<String>>> {
        return HttpResponse.badRequest(mapOf("errors" to arrayListOf("Invalid JSON format")))
    }

    @Error(exception = UnsatisfiedBodyRouteException::class)
    fun onUnsatisfiedBodyRouteException(
        request: HttpRequest<*>,
        ex: UnsatisfiedBodyRouteException
    ): HttpResponse<Map<String, List<String?>>> {
        return HttpResponse.badRequest(mapOf("errors" to arrayListOf("Request body missing")))
    }

    @Error(status = HttpStatus.NOT_FOUND, global = true)
    fun onRouteNotFoundException(): HttpResponse<Map<String, List<String?>>> {
        return HttpResponse.notFound(mapOf("errors" to arrayListOf("Route not found")))
    }

    @Error(exception = ConversionErrorException::class)
    fun onConversionErrorException(ex: ConversionErrorException): HttpResponse<Map<String, ArrayList<String?>>>? {
        return HttpResponse.badRequest(mapOf("errors" to arrayListOf(ex.message)))
    }

    @Error(exception = ConstraintViolationException::class)
    fun onConstraintViolationException(ex: ConstraintViolationException): HttpResponse<Map<String, List<String>>> {
        return HttpResponse.badRequest(mapOf("errors" to ex.constraintViolations.map { it.message }))
    }

    @Error(exception = RuntimeException::class)
    fun onRuntimeException(ex: RuntimeException): HttpResponse<Map<String, ArrayList<String?>>>? {
        return HttpResponse.serverError(mapOf("errors" to arrayListOf(ex.message)))
    }

    @Error
    fun userDoesNotExistException(exception: UserDoesNotExistException): HttpResponse<Map<String, List<String?>>>? {
        return HttpResponse.badRequest(mapOf("error" to listOf(exception.message)))
    }
    @Error
    fun invalidPreOrderPlaceException(exception: InvalidPreOrderPlaceException): HttpResponse<Map<String, List<String>>>? {
        return HttpResponse.badRequest(mapOf("error" to exception.errorList))
    }
    @Error
    fun insufficientFundsException(exception: InsufficientFundsException): HttpResponse<Map<String, List<String?>>>?{
        return HttpResponse.badRequest(mapOf("error" to listOf(exception.message)))
    }

    @Error
    fun insufficientInventoryException(exception: InsufficientInventoryException): HttpResponse<Map<String, List<String?>>>?{
        return HttpResponse.badRequest(mapOf("error" to listOf(exception.message)))
    }

    @Error(global = true)
    fun globalError(e: Throwable): Map<String,List<String?>> {
        return mapOf("error" to listOf(e.message))
    }

}