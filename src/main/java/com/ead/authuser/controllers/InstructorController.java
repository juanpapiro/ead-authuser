package com.ead.authuser.controllers;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ead.authuser.dtos.InstructorDto;
import com.ead.authuser.enums.RoleType;
import com.ead.authuser.enums.UserType;
import com.ead.authuser.models.RoleModel;
import com.ead.authuser.models.UserModel;
import com.ead.authuser.services.RoleService;
import com.ead.authuser.services.UserService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/instructors")
public class InstructorController {
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private RoleService roleService;
	
	@PreAuthorize("hasAnyRole('ADMIN')")
	@PostMapping("/subscription")
	public ResponseEntity<Object> saveSubscriptionInstructor(@RequestBody @Valid InstructorDto instructorDto) {
		log.info("Cadastrando instrutor.");
		Optional<UserModel> userModelOptional = userService.findById(instructorDto.getUserId());
		
		if(!userModelOptional.isPresent()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found!");
		} else {
			RoleModel roleModel = roleService.findByRoleName(RoleType.ROLE_INSTRUCTOR)
					.orElseThrow(() -> new RuntimeException(String.format(
							"Error: %s is not found.", RoleType.ROLE_INSTRUCTOR.toString())));
			UserModel userModel = userModelOptional.get();
			userModel.setUserType(UserType.INSTRUCTOR);
			userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
			userModel.getRoles().add(roleModel);
			userService.updateUser(userModel);
			return ResponseEntity.ok().body(userModel);
		}		
	}

}
