package com.AlexandreLoiola.Downstream.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/downstream")
public class DownstreamController {
    @GetMapping("/ping")
    public String poing() {
        return "poing";
    }
}