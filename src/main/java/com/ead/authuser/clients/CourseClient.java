package com.ead.authuser.clients;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.ead.authuser.dtos.CourseDto;
import com.ead.authuser.dtos.ResponsePageDto;
import com.ead.authuser.services.UtilsService;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class CourseClient {
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private UtilsService utilsService;
		
//	@Retry(name = "retryInstance", fallbackMethod = "retryFallback")
	@CircuitBreaker(name = "circuitbreakerInstance", fallbackMethod = "circuitbreakerFallback")
//	@CircuitBreaker(name = "circuitbreakerInstance")
	public Page<CourseDto> getAllCoursesByUser(UUID userId, Pageable pageable, String token) {
		List<CourseDto> searchResult = null;
		ResponseEntity<ResponsePageDto<CourseDto>> response = null;

		String url = utilsService.createUrl(userId, pageable);
		
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, token);
		HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
		
		log.debug("Request URL {}", url);
		log.info("Request URL {}", url);
		
		ParameterizedTypeReference<ResponsePageDto<CourseDto>> responseType = new ParameterizedTypeReference<ResponsePageDto<CourseDto>>(){};
		response = restTemplate.exchange(url, HttpMethod.GET, entity, responseType);
		searchResult = response.getBody().getContent();
		log.info("Response number of elements {}", searchResult.size());			
			
		log.info("Ending request /courses userId {}", userId);
		
		return new PageImpl<>(searchResult);
	}

	
	public Page<CourseDto> circuitbreakerFallback(UUID userId, Pageable pageable, String token, Throwable t) {
		log.error("Inside circuit Breaker fallback, cause {}", t.toString());
		List<CourseDto> searchResult = new ArrayList<>();
		return new PageImpl<>(searchResult);
	}
	
	public Page<CourseDto> retryFallback(UUID userId, Pageable pageable, String token, Throwable t) {
		log.error("Inside retry retryfallback, cause {}", t.toString());
		List<CourseDto> searchResult = new ArrayList<>();
		return new PageImpl<>(searchResult);
	}
}
