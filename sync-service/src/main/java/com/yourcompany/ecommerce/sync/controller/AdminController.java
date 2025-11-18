package com.yourcompany.ecommerce.sync.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/internal")
public class AdminController {


    // A simple health / admin endpoint. You can add reindex trigger here if desired.
    @PostMapping("/health/reindex")
    public String reindex() {
// In production, trigger a background job to scan DB and publish events or call Search-service bulk index
        return "reindex-started";
    }
}