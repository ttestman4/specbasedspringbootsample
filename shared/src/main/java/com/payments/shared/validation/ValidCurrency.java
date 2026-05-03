package com.payments.shared.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
@Constraint(validatedBy = ValidCurrencyValidator.class)
@Documented
public @interface ValidCurrency {
    String message() default "Invalid ISO 4217 currency";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
