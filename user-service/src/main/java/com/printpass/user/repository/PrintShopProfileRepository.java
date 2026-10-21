package com.printpass.user.repository;

import com.printpass.user.entity.PrintShopProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PrintShopProfileRepository extends JpaRepository<PrintShopProfile, UUID> {

    //Ek user ki saari shops (Underscore lagana zaroori tha)
    List<PrintShopProfile> findByUser_Id(UUID userId);


    //Admin - verified shops dhundhna
    List<PrintShopProfile> findByIsVerified(boolean isVerified);

    // Job Service - Shop exist aur verified he?
    Optional<PrintShopProfile> findByIdAndIsVerified(UUID shopId, boolean isVerified);

    // City ke hisaab se shops dhundhna
    List<PrintShopProfile> findByCity(String city);

    // Shop Name already exist karta he?
    boolean existsByShopName(String shopName);

}
