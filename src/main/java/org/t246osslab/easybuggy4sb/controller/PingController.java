package org.t246osslab.easybuggy4sb.controller;

import java.util.Collections;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingController {

	@RequestMapping(value = "/ping")
	public Map<String, String> ping() {
		return Collections.singletonMap("status", "UP");
	}
}