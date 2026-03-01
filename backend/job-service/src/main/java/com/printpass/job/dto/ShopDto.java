package com.printpass.job.dto;
import lombok.Data;
import java.math.BigDecimal;
@Data
public class ShopDto {
    private Long id;
    private BigDecimal perPageCost;
    private boolean isOpen;
}