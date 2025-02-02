package com.AlexandreLoiola.Issuer.rest.controller;


import com.AlexandreLoiola.Issuer.rest.dto.AuthDto;
import com.AlexandreLoiola.Issuer.rest.form.AuthForm;
import com.AlexandreLoiola.Issuer.rest.form.LoginForm;
import com.AlexandreLoiola.Issuer.service.AuthService;
import com.AlexandreLoiola.Issuer.service.TokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final TokenService tokenService;

    public AuthController(AuthService authService, TokenService tokenService) {
        this.authService = authService;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthDto> login(@RequestBody LoginForm form) {
        return ResponseEntity.ok(authService.authenticate(form));
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody AuthForm form) {
        authService.register(form);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthDto> refresh(@RequestParam String refreshToken) {
        AuthDto authDto = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(authDto);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestParam String refreshToken) {
        authService.logout(refreshToken);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/decode-jwt")
    public ResponseEntity<Map<String, Object>> decodeJwtPayload(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "");
        Map<String, Object> jwtPayload = tokenService.decodeJwtPayload(token);
        return ResponseEntity.ok(jwtPayload);
    }

    @GetMapping("/validate-token")
    public ResponseEntity<Boolean> validateToken(@RequestParam String token) {
        Boolean bool = tokenService.validateToken(token);
        return ResponseEntity.ok(bool);
    }
}

// TODO: Usar Redis ou Banco de Dados para armazená-los, em vez de Map<String, String>.
// TODO: Usar Kafka para mensageria assincrona
// TODO: Usar Docker