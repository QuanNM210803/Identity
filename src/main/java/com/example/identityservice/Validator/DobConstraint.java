package com.example.identityservice.Validator;

import java.lang.annotation.*;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {DobValidator.class})
public @interface DobConstraint {
    String message() default "Invalid data of birth";

    int min();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
