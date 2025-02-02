package com.AlexandreLoiola.Issuer.rest.controller;

import com.AlexandreLoiola.Issuer.rest.dto.UserDto;
import com.AlexandreLoiola.Issuer.service.UserService;
import lombok.Getter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getAll() {
        List<UserDto> dto = userService.getAll();
        return ResponseEntity.ok(dto);
    }
}
