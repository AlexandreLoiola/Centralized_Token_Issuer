package com.AlexandreLoiola.Issuer.service;

import com.AlexandreLoiola.Issuer.model.UserModel;
import com.AlexandreLoiola.Issuer.repository.UserRepository;
import com.AlexandreLoiola.Issuer.rest.dto.AuthDto;
import com.AlexandreLoiola.Issuer.rest.dto.UserInfoDto;
import com.AlexandreLoiola.Issuer.rest.form.AuthForm;
import com.AlexandreLoiola.Issuer.rest.form.LoginForm;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, TokenService tokenService, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthDto authenticate(LoginForm form) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(form.getUsername(), form.getPassword())
        );
        UserModel userModel = userRepository.findByUsername(form.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário já existe"));
        UserInfoDto userInfoDto = new UserInfoDto(userModel.getUsername(), userModel.getRole());
        String accessToken = tokenService.generateAccessToken(userInfoDto);
        String refreshToken = tokenService.generateRefreshToken(userInfoDto);
        return new AuthDto(accessToken, refreshToken);
    }

    public void register(AuthForm form) {
        if (userRepository.findByUsername(form.getUsername()).isPresent()) {
            throw new RuntimeException("Usuário já existe");
        }

        UserModel user = new UserModel();
        user.setUsername(form.getUsername());
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setRole(form.getRole());
        userRepository.save(user);
    }

    public AuthDto refreshToken(String refreshToken) {
        String accessToken = tokenService.refreshAccessToken(refreshToken);
        return new AuthDto(accessToken, refreshToken);
    }

    public void logout(String refreshToken) {
        tokenService.revokeRefreshToken(refreshToken);
    }
}