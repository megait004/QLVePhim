package com.cinema.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping({"/", "/index", "/movies", "/screenings", "/tickets", "/login"})
    public String handleStaticPages() {
        return "forward:/index.html";
    }

    @GetMapping("/error")
    public String handleError() {
        return "forward:/error.html";
    }
}