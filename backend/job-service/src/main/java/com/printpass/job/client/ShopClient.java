package com.printpass.job.client;
import com.printpass.job.dto.ShopDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "SHOP-SERVICE")
public interface ShopClient {
    @GetMapping("/api/shops/{id}")
    ShopDto getShopById(@PathVariable("id") Long id);
}