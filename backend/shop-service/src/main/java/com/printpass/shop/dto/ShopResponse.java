package com.printpass.shop.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class ShopResponse {
    private Long id;
    private Long vendorId;
    private String shopName;
    private String location;
    private BigDecimal perPageCost;
    private boolean isOpen;
}