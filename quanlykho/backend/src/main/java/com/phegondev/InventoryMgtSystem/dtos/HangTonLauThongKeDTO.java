package com.phegondev.InventoryMgtSystem.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HangTonLauThongKeDTO {
    private Integer tongLo;
    private Integer tren30Ngay;
    private Integer tren60Ngay;
    private Integer tren90Ngay;
    private Integer tren180Ngay;
}
