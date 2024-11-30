package com.makowski.messenger.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PasswordDto {

    @NonNull
    @NotBlank(message = "old password cannot be blank")
    private String oldPassword;

    @NonNull
    @NotBlank(message = "new password cannot be blank")
    private String newPassword;

    @NonNull
    @NotBlank(message = "repeat new password cannot be blank")
    private String repeatNewPassword;
    
}
