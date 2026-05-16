package com.trippzo.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordResetDTO {

    @NotBlank(message = "Паролата не може да бъде празна")
    @Size(min = 6, message = "Паролата трябва да бъде поне 6 символа")
    private String password;

    @NotBlank(message = "Потвърждението на паролата не може да бъде празно")
    private String confirmPassword;
}
