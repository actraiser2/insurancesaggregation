package com.fpnatools.aggregation.insurances.framework.adapters.input.validators;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CredentialsValidator.class)
public @interface Credentials {

	String message() default "Credentials do not adhere to the specified rule";
	Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

}
