package com.AlexandreLoiola.Issuer.rest.dto;

import com.AlexandreLoiola.Issuer.enumeration.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoDto {
    private String username;
    private RoleEnum role;
}