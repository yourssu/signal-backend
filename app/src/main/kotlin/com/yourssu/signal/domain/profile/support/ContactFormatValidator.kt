package com.yourssu.signal.domain.profile.support

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import com.yourssu.signal.domain.profile.implement.ProfileValidationPolicy
import org.springframework.stereotype.Component

@Component
class ContactFormatValidator : ConstraintValidator<ContactFormat, String> {
    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        return value != null && ProfileValidationPolicy.isValidContact(value)
    }
}
