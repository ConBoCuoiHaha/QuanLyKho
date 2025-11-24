package com.phegondev.InventoryMgtSystem.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class NhaCungCapDTO {

    private Long id;

    @NotBlank(message = "Ten nha cung cap la bat buoc")
    private String name;

    @NotBlank(message = "Thong tin lien he la bat buoc")
    private String contactInfo;
    private String phone;
    private String email;

    private String address;
}
