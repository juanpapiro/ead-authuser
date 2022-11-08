package com.ead.authuser.configs.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.ead.authuser.enums.RoleType;

@SuppressWarnings("deprecation")
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	
	@Autowired
	private UserDetailsService userDetailsService;
	
	@Autowired
	private AuthenticationEntryPoint authenticationEntryPoint;

	private static final String[] AUTH_WHITELIST = {
			"/auth/**"
	};
	
	@Bean
	public AuthenticationJwtFilter authenticationJwtFilter() {
		return new AuthenticationJwtFilter();
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
//			.httpBasic()
			.exceptionHandling()
			.authenticationEntryPoint(authenticationEntryPoint)
			.and()
			.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			.and()
			.authorizeRequests()
			.antMatchers(AUTH_WHITELIST).permitAll()
//			.antMatchers(HttpMethod.GET, "/users/**").hasRole("STUDENT")
			.anyRequest().authenticated()
			.and()
			.csrf().disable();
		http.addFilterBefore(authenticationJwtFilter(), UsernamePasswordAuthenticationFilter.class);
	}
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService)
			.passwordEncoder(passwordEncoder());
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Override
	@Bean
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}
	
	@Bean
	public RoleHierarchy roleHierarchy() {
		RoleHierarchyImpl roleHierarchyImpl = new RoleHierarchyImpl();
		StringBuilder sb = new StringBuilder();
		sb.append(RoleType.ROLE_ADMIN.toString())
		  .append(" > ")
		  .append(RoleType.ROLE_INSTRUCTOR.toString())
		  .append(" \n")
		  .append(RoleType.ROLE_INSTRUCTOR.toString())
		  .append(" > ")
		  .append(RoleType.ROLE_STUDENT.toString())
		  .append(" \n")
		  .append(RoleType.ROLE_STUDENT.toString())
		  .append(" > ")
		  .append(RoleType.ROLE_USER.toString());
		roleHierarchyImpl.setHierarchy(sb.toString());
		return roleHierarchyImpl;
	}
}
