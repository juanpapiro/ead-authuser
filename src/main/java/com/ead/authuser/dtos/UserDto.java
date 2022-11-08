package com.ead.authuser.dtos;

import java.util.UUID;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.br.CPF;

import com.ead.authuser.validations.UsernameConstraint;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto {
	
	public interface UserView {
		public static interface ResgistrationPost {}
		public static interface UserPut {}
		public static interface PasswordPut {}
		public static interface ImagePut {}
	}
	
	private UUID userId;
	
	@UsernameConstraint(groups = {UserView.ResgistrationPost.class}, message = "erro")
	@Size(min = 4, max = 50, groups = {UserView.ResgistrationPost.class})
	@NotBlank(groups = {UserView.ResgistrationPost.class})
	@JsonView(UserView.ResgistrationPost.class)
	private String username;
	
	@Email(groups = {UserView.ResgistrationPost.class})
	@NotBlank(groups = {UserView.ResgistrationPost.class})
	@JsonView(UserView.ResgistrationPost.class)
	private String email;
	
	@Size(min = 6, max = 20, groups = {UserView.ResgistrationPost.class, UserView.PasswordPut.class})
	@NotBlank(groups = {UserView.ResgistrationPost.class, UserView.PasswordPut.class})
	@JsonView({UserView.ResgistrationPost.class, UserView.PasswordPut.class})
	private String password;
	
	@Size(min = 2, max = 20, groups = {UserView.ResgistrationPost.class, UserView.PasswordPut.class})
	@NotBlank(groups = {UserView.PasswordPut.class})
	@JsonView(UserView.PasswordPut.class)
	private String oldPassword;
	
	@JsonView({UserView.ResgistrationPost.class, UserView.UserPut.class})
	private String fullName;

	@JsonView({UserView.ResgistrationPost.class, UserView.UserPut.class})
	private String phoneNumber;

	@CPF(groups = {UserView.ResgistrationPost.class, UserView.UserPut.class})
	@JsonView({UserView.ResgistrationPost.class, UserView.UserPut.class})
	private String cpf;

	@NotBlank(groups = {UserView.ImagePut.class})
	@JsonView(UserView.ImagePut.class)
	private String imageUrl;

}
