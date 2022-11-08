package com.ead.authuser.validations;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UsernameConstraintImpl implements ConstraintValidator<UsernameConstraint, String>{
	

	@Override
	public boolean isValid(String username, ConstraintValidatorContext context) {
		return (username == null || username.trim().isEmpty() || username.contains(" ")) ? false : true;
	}

}