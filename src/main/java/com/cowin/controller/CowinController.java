package com.cowin.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cowin.service.CowinService;

@RestController
public class CowinController {
	
	@Autowired
	private CowinService service;
	//For testing
	@GetMapping("/")
	public List<String> test(){
		return service.getData();
	}
	
}
