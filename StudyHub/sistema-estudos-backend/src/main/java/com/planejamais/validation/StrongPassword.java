package com.planejamais.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = StrongPasswordValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface StrongPassword {
    String message() default "Senha deve ter no mínimo 8 caracteres, incluindo letra e número.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
