package com.printpass.shop.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ShopRequest {
    private Long vendorId;
    private String shopName;
    private String location;
    private BigDecimal perPageCost;
}