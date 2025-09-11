package com.yourcompany.ecommerce.seller.service;

import com.yourcompany.ecommerce.seller.dto.SellerApprovalRequest;
import com.yourcompany.ecommerce.seller.dto.SellerResponse;
import com.yourcompany.ecommerce.seller.mapper.SellerMapper;
import com.yourcompany.ecommerce.seller.repository.SellerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class SellerService {

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private SellerMapper sellerMapper;

    public Flux<SellerResponse> getAllSellersByStatus(String status) {
        return sellerRepository.findAllByStatus(status)
                .map(sellerMapper::toSellerResponse);
    }

    public Mono<SellerResponse> getSellerById(String id) {
        return sellerRepository.findById(id)
                .map(sellerMapper::toSellerResponse);
    }

    public Mono<SellerResponse> processSellerApproval(String sellerId, SellerApprovalRequest approvalRequest) {
        return sellerRepository.findById(sellerId)
                .flatMap(seller -> {
                    seller.setStatus(approvalRequest.getStatus().toUpperCase());
                    if ("REJECTED".equals(seller.getStatus())) {
                        seller.setReasonForRejection(approvalRequest.getReason());
                    }
                    return sellerRepository.save(seller);
                })
                .map(sellerMapper::toSellerResponse);
        // TODO: Gửi sự kiện SellerApprovedEvent hoặc SellerRejectedEvent sau khi lưu thành công
    }
}