package com.ead.authuser.services.impl;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.ead.authuser.services.UtilsService;

@Service
public class UtilsServiceImpl implements UtilsService {
	
	@Value("${ead.api.url.course}")
	private String urlCourse;
	
	public String createUrl(UUID userId, Pageable pageable) {
		return urlCourse + "/courses?userId=" + userId + "&page=" + pageable.getPageNumber() + "&size="
				+ pageable.getPageSize() + "&sort=" + pageable.getSort().toString().replaceAll(": ", ",");
	}
	
	

}
