package com.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simple REST Controller to demonstrate basic HTTP GET handling in Spring Boot.
 * 
 * @RestController: Combines @Controller and @ResponseBody.
 * @GetMapping: Maps HTTP GET requests onto specific handler methods.
 */
@RestController
public class HelloController {

    @GetMapping("/")
    public String hello() {
        return "Hello, Spring Boot!";
    }
}
