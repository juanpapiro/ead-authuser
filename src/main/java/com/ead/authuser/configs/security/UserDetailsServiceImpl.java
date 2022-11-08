package com.ead.authuser.configs.security;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.ead.authuser.models.UserModel;
import com.ead.authuser.repositories.UserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	@Autowired
	private UserRepository userRepository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		UserModel userModel = userRepository.findByUsername(username)
				.orElseThrow(() -> new RuntimeException("User not found with username " + username));
		return UserDetailsImpl.build(userModel);
	}
	
	public UserDetails loadUserById(UUID userId) {
		UserModel userModel = userRepository.findByUserId(userId)
				.orElseThrow(() -> new RuntimeException("User not found with userId " + userId));
		return UserDetailsImpl.build(userModel);
	}

}
