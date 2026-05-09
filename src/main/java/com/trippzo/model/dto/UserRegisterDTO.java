package com.trippzo.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegisterDTO {

    @NotBlank(message = "Моля, въведете пълното си име")
    @Size(min = 2, max = 50)
    private String fullName;

    @NotBlank(message = "Потребителското име е задължително")
    @Size(min = 3, max = 20)
    private String username;

    @NotBlank(message = "Имейлът е задължителен")
    @Email(message = "Въведете валиден имейл адрес")
    private String email;

    @NotBlank(message = "Паролата е задължителна")
    @Size(min = 6, message = "Паролата трябва да е поне 6 символа")
    private String password;

    @NotBlank(message = "Моля, потвърдете паролата")
    private String confirmPassword;
}
