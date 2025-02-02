package com.AlexandreLoiola.Issuer.rest.form;

import com.AlexandreLoiola.Issuer.enumeration.RoleEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AuthForm {
    private String username;
    private String password;
    private RoleEnum role;
}