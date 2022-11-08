package com.ead.authuser.controllers;

import java.time.LocalDateTime;
import java.time.ZoneId;

import javax.validation.Valid;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ead.authuser.configs.security.JwtProvider;
import com.ead.authuser.dtos.JwtDto;
import com.ead.authuser.dtos.LoginDto;
import com.ead.authuser.dtos.UserDto;
import com.ead.authuser.enums.RoleType;
import com.ead.authuser.enums.UserStatus;
import com.ead.authuser.enums.UserType;
import com.ead.authuser.models.RoleModel;
import com.ead.authuser.models.UserModel;
import com.ead.authuser.services.RoleService;
import com.ead.authuser.services.UserService;
import com.fasterxml.jackson.annotation.JsonView;

import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/auth")
public class AuthenticationController {
	
//	Logger logging = LogManager.getLogger(AuthenticationController.class);


	@Autowired
	private UserService userService;
	
	@Autowired
	private RoleService roleService;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private JwtProvider jwtProvider;
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	
	@PostMapping("/signup")
	public ResponseEntity<Object> registerUser(@JsonView({UserDto.UserView.ResgistrationPost.class})
											   @Validated({UserDto.UserView.ResgistrationPost.class})
											   @RequestBody UserDto userDto) {
		
		log.debug("POST registerUser userDto received {} ", userDto.toString());
		
		if(userService.existsByUsername(userDto.getUsername())) {
            log.warn("Username {} is Already Taken ", userDto.getUsername());
			return ResponseEntity.status(HttpStatus.CONFLICT).body("Username já cadastrado.");
		}
		if(userService.existsByEmail(userDto.getEmail())) {
            log.warn("Email {} is Already Taken ", userDto.getEmail());
			return ResponseEntity.status(HttpStatus.CONFLICT).body("E-mail já cadastrado.");
		}
		
		RoleModel roleModel = roleService.findByRoleName(RoleType.ROLE_STUDENT)
				.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
		
		userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));
		
		var userModel = new UserModel();
		BeanUtils.copyProperties(userDto, userModel);
		userModel.setUserStatus(UserStatus.ACTIVE);
		userModel.setUserType(UserType.STUDENT);
		userModel.setCreationDate(LocalDateTime.now(ZoneId.of("UTC")));
		userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
		userModel.getRoles().add(roleModel);
		userModel = userService.saveUser(userModel);
	    log.debug("POST registerUser userModel saved {} ", userModel.getUserId());
        log.info("User saved successfully userId {} ", userModel.getUserId());
		return ResponseEntity.status(HttpStatus.CREATED).body(userModel);
		
	}
	
	@PostMapping("/login")
	public ResponseEntity<JwtDto> authenticateUser(@Valid @RequestBody LoginDto loginDto) {
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword()));
		SecurityContextHolder.getContext().setAuthentication(authentication);
		String jwt = jwtProvider.generateJwt(authentication);
		return ResponseEntity.ok(new JwtDto(jwt));
	}
	
	@PostMapping("/signup/admin/usr")
	public ResponseEntity<Object> registerUserAdmin(@JsonView({UserDto.UserView.ResgistrationPost.class})
											   @Validated({UserDto.UserView.ResgistrationPost.class})
											   @RequestBody UserDto userDto) {
		
		log.debug("POST registerUser userDto received {} ", userDto.toString());
		
		if(userService.existsByUsername(userDto.getUsername())) {
            log.warn("Username {} is Already Taken ", userDto.getUsername());
			return ResponseEntity.status(HttpStatus.CONFLICT).body("Username já cadastrado.");
		}
		if(userService.existsByEmail(userDto.getEmail())) {
            log.warn("Email {} is Already Taken ", userDto.getEmail());
			return ResponseEntity.status(HttpStatus.CONFLICT).body("E-mail já cadastrado.");
		}
		
		RoleModel roleModel = roleService.findByRoleName(RoleType.ROLE_ADMIN)
				.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
		
		userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));
		
		var userModel = new UserModel();
		BeanUtils.copyProperties(userDto, userModel);
		userModel.setUserStatus(UserStatus.ACTIVE);
		userModel.setUserType(UserType.ADMIN);
		userModel.setCreationDate(LocalDateTime.now(ZoneId.of("UTC")));
		userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
		userModel.getRoles().add(roleModel);
		userModel = userService.saveUser(userModel);
	    log.debug("POST registerUser userModel saved {} ", userModel.getUserId());
        log.info("User saved successfully userId {} ", userModel.getUserId());
		return ResponseEntity.status(HttpStatus.CREATED).body(userModel);
		
	}
	
}
