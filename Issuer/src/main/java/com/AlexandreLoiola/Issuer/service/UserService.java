package com.AlexandreLoiola.Issuer.service;

import com.AlexandreLoiola.Issuer.exceptions.UserNotFoundException;
import com.AlexandreLoiola.Issuer.model.UserModel;
import com.AlexandreLoiola.Issuer.repository.UserRepository;
import com.AlexandreLoiola.Issuer.rest.dto.UserDto;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j2
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info(username);
        UserModel user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }

    public List<UserDto> getAll() {
        return userRepository.findAll()
                .stream()
                .map( model -> new UserDto(model.getUsername(), model.getRole()))
                .collect(Collectors.toList());
    }
}