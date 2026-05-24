package com.mycompany.fitnesstracker.Models.Nutrition;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NutritionLogDTO {
    private Long id;
    private String foodName;
    private Double quantity;
    private String unit;
    private Double calories;
    private Double protein;
    private Double carbohydrates;
    private Double fat;

    /* Дата у форматі YYYY-MM-DD (так само як надсилає фронтенд) */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate date;
}
