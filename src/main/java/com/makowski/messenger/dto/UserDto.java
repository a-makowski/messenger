package com.makowski.messenger.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public class UserDto {


    private Long userId;
    private String username;

    @NonNull
    @NotBlank(message = "first name cannot be blank")
    private String firstName;

    @NonNull
    @NotBlank(message = "surname cannot be blank")
    private String surname;
}
