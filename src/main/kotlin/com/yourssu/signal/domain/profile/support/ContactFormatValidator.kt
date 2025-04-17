package com.yourssu.signal.domain.profile.support

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class ContactFormatValidator : ConstraintValidator<ContactFormat, String> {
    private val phoneRegex = Regex("^010\\d{8}$")
    private val instagramRegex = Regex("^@[a-zA-Z0-9._]{1,30}$")

    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        if (value == null) {
            return false
        }
        return phoneRegex.matches(value) || instagramRegex.matches(value)
    }
}
