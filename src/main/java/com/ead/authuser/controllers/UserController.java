package com.ead.authuser.controllers;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ead.authuser.configs.security.AuthenticationCurrentUserService;
import com.ead.authuser.configs.security.UserDetailsImpl;
import com.ead.authuser.dtos.UserDto;
import com.ead.authuser.models.UserModel;
import com.ead.authuser.services.UserService;
import com.ead.authuser.specifications.SpecificationTemplate;
import com.fasterxml.jackson.annotation.JsonView;

import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/users")
public class UserController {
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private AuthenticationCurrentUserService authenticationCurrentUserService;
	
	@PreAuthorize("hasAnyRole('STUDENT','ADMIN')")
	@GetMapping
	public ResponseEntity<Page<UserModel>> getAllUsers(
			SpecificationTemplate.UserSpec spec,
			@PageableDefault(page = 0, size = 10, sort = "userId", direction = Sort.Direction.ASC) Pageable pageable,
			Authentication authentication) {
		UserDetails userDetails = (UserDetailsImpl) authentication.getPrincipal();
		log.info("Authentication{}", userDetails.getUsername());
		
		Page<UserModel> userModelPage = userService.findAll(spec, pageable);
		
		if(!userModelPage.isEmpty()) {
			for(UserModel userModel : userModelPage.toList()) {
				userModel.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class)
						.getOneUser(userModel.getUserId())).withSelfRel());
			}
		}
		return ResponseEntity.status(HttpStatus.OK).body(userModelPage);
	}
	
	@PreAuthorize("hasAnyRole('STUDENT')")
	@GetMapping("/{userId}")
	public ResponseEntity<Object> getOneUser(@PathVariable(value = "userId") UUID userId) {
		UUID currentUserId = authenticationCurrentUserService.getCurrentUser().getUserId();
		if(currentUserId.equals(userId)) {
			Optional<UserModel> userModelOptional = userService.findById(userId);
			if(!userModelOptional.isPresent()) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
			} else {
				return ResponseEntity.ok(userModelOptional.get());
			}			
		} else {
			throw new AccessDeniedException("Forbiden");
		}
	}
	
	@DeleteMapping("/{userId}")
	public ResponseEntity<Object> deleteUser(@PathVariable(value = "userId") UUID userId) {
		log.debug("DELETE deleteUser userId received {} ", userId);
		Optional<UserModel> userModelOptional = userService.findById(userId);
		if(!userModelOptional.isPresent()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
		} else {
			userService.deleteUser(userModelOptional.get());
			log.debug("DELETE deleteUser userId deleted {} ", userId);
            log.info("User deleted successfully userId {} ", userId);
			return ResponseEntity.status(HttpStatus.OK).body("User deleted successfully.");			
		}
	}
	
	
	@PutMapping("/{userId}")
	public ResponseEntity<Object> updateUser(@PathVariable(value = "userId") UUID userId,
											 @JsonView({UserDto.UserView.UserPut.class}) 
											 @Validated({UserDto.UserView.UserPut.class}) 
											 @RequestBody UserDto userDto) {
		log.debug("PUT updatePassword userDto received {} ", userDto.toString());
		Optional<UserModel> userModelOptional = userService.findById(userId);
		if(!userModelOptional.isPresent()) {
			log.warn("Mismatched old password userId {} ", userDto.getUserId());
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
		} else {
			var userModel = userModelOptional.get();
			userModel.setFullName(userDto.getFullName());
			userModel.setPhoneNumber(userDto.getPhoneNumber());
			userModel.setCpf(userDto.getCpf());
			userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
			userModel = userService.updateUser(userModel);
			log.debug("PUT updatePassword userModel userId {} ", userModel.getUserId());
            log.info("Password updated successfully userId {} ", userModel.getUserId());
			return ResponseEntity.status(HttpStatus.OK).body(userModel);			
		}
	}
	
	
	@PutMapping("/{userId}/password")
	public ResponseEntity<Object> updatePassword(@PathVariable(value = "userId") UUID userId,
											 	 @JsonView({UserDto.UserView.PasswordPut.class}) 
												 @Validated({UserDto.UserView.PasswordPut.class})
											 	 @RequestBody UserDto userDto) {
		log.debug("PUT updateImage userDto received {} ", userDto.toString());
		Optional<UserModel> userModelOptional = userService.findById(userId);
		if(!userModelOptional.isPresent()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
		} 
		if(!userModelOptional.get().getPassword().equals(userDto.getOldPassword())) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("Erro: oldPassword incorreta.");
		} else {
			var userModel = userModelOptional.get();
			userModel.setPassword(userDto.getPassword());
			userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
			userModel = userService.updatePassword(userModel);
			log.debug("PUT updateImage userModel userId {} ", userModel.getUserId());
            log.info("Image updated successfully userId {} ", userModel.getUserId());
			return ResponseEntity.status(HttpStatus.OK).body("Senha atualizada com sucesso.");			
		}
	}
	
	
	@PutMapping("/{userId}/image")
	public ResponseEntity<Object> updateImage(@PathVariable(value = "userId") UUID userId,
											  @JsonView({UserDto.UserView.ImagePut.class}) 
											  @Validated({UserDto.UserView.ImagePut.class})
											  @RequestBody UserDto userDto) {
		Optional<UserModel> userModelOptional = userService.findById(userId);
		if(!userModelOptional.isPresent()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
		} else {
			var userModel = userModelOptional.get();
			userModel.setImageUrl(userDto.getImageUrl());
			userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
			userService.updateUser(userModel);
			return ResponseEntity.status(HttpStatus.OK).body(userModel);			
		}
	}

}
