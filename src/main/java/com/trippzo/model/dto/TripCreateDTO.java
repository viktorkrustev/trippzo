package com.trippzo.model.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TripCreateDTO {

    @NotBlank(message = "Началната точка е задължителна")
    private String origin;

    @NotNull(message = "Моля, въведете цена")
    @DecimalMin(value = "0.0", inclusive = true, message = "Цената не може да бъде отрицателна")
    private BigDecimal pricePerSeat;

    @NotBlank(message = "Крайната точка е задължителна")
    private String destination;

    @NotBlank(message = "Моля, въведете дата")
    @Pattern(regexp = "\\d{2}-\\d{2}-\\d{4}", message = "Форматът трябва да е дд-мм-гггг")
    private String departureDate;

    @NotBlank(message = "Моля, въведете час")
    @Pattern(regexp = "\\d{2}:\\d{2}", message = "Форматът трябва да е чч:мм")
    private String departureTime;

    @NotNull(message = "Моля, въведете брой места")
    @Min(value = 1, message = "Трябва да има поне 1 място")
    private Integer seatsTotal;

    @NotBlank(message = "Моля, въведете автомобил")
    private String car;

    @Size(max = 500, message = "Описанието е твърде дълго")
    private String description;

    private String stops;
}
