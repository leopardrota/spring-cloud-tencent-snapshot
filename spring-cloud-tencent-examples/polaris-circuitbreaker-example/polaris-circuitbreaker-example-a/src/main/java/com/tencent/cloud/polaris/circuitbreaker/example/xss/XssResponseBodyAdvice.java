/*
 * Tencent is pleased to support the open source community by making Spring Cloud Tencent available.
 *
 *  Copyright (C) 2019 THL A29 Limited, a Tencent company. All rights reserved.
 *
 *  Licensed under the BSD 3-Clause License (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://opensource.org/licenses/BSD-3-Clause
 *
 *  Unless required by applicable law or agreed to in writing, software distributed
 *  under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 *  CONDITIONS OF ANY KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations under the License.
 *
 */

package com.tencent.cloud.polaris.circuitbreaker.example.xss;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.owasp.esapi.ESAPI;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Escape String in ResponseBody before write it into HttpResponse
 *
 * @author Daifu Wu
 */
@ControllerAdvice
public class XssResponseBodyAdvice implements ResponseBodyAdvice {

	@Override
	public boolean supports(MethodParameter methodParameter, Class aClass) {
		return methodParameter.hasMethodAnnotation(ResponseBody.class) || methodParameter.getDeclaringClass().getAnnotation(ResponseBody.class) != null || methodParameter.getDeclaringClass().getAnnotation(RestController.class) != null;
	}

	@Override
	public Object beforeBodyWrite(Object body, MethodParameter methodParameter, MediaType mediaType, Class aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
		if (body instanceof String) {
			body = ESAPI.encoder().encodeForHTML((String) body);
			body = ESAPI.encoder().encodeForJavaScript((String) body);
			body = ESAPI.encoder().encodeForXML((String) body);
			return body;
		}
		try {
			if (!((Class) body.getClass().getField("TYPE").get(null)).isPrimitive()) {
				Map<String, Object> map = new HashMap<>();
				Field[] fields = body.getClass().getDeclaredFields();
				for (Field field: fields) {
					field.setAccessible(true);
					Object value = field.get(body);
					if (value instanceof String) {
						value = StringEscapeUtils.escapeHtml((String) value);
					}
					map.put(field.getName(), value);
				}
				return map;
			}
		}
		catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return body;
	}
}
