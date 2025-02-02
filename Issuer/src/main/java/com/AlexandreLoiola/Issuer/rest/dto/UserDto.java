package com.AlexandreLoiola.Issuer.rest.dto;

import com.AlexandreLoiola.Issuer.enumeration.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String username;
    private RoleEnum roleEnum;
}