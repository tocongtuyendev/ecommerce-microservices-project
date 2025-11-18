package com.yourcompany.ecommerce.seller.mapper;

import com.yourcompany.ecommerce.seller.dto.SellerResponse;
import com.yourcompany.ecommerce.seller.model.Seller;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SellerMapper {
    SellerResponse toSellerResponse(Seller seller);
}
