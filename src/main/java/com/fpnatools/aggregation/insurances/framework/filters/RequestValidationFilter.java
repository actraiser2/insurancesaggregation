package com.fpnatools.aggregation.insurances.framework.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("requestValidationFilter")
public class RequestValidationFilter implements Filter {

	private Logger logger = LoggerFactory.getLogger(RequestValidationFilter.class);
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		// TODO Auto-generated method stub
		var httpRequest = (HttpServletRequest) request;
		var httpResponse = (HttpServletResponse) response;
		
		
		String xRequestId = httpRequest.getHeader("X-Request-Id");
		String requestUrl = httpRequest.getRequestURL().toString();
		logger.info(requestUrl);
		if (requestUrl.contains("v3") || requestUrl.contains("swagger-ui") || 
				requestUrl.contains("error") || StringUtils.isNotEmpty(xRequestId)) {
			chain.doFilter(request, response);
		}
		else {
			httpResponse.setStatus(400);
		}
	}

}
