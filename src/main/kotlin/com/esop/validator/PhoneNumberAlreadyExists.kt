package com.esop.validator

import javax.validation.Constraint

@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [])
annotation class PhoneNumberAlreadyExists(
    val message: String = "Phone number already exists"
)


