package com.yourcompany.ecommerce.search.model;

import lombok.Data;

import java.util.List;

@Data
public class ProductIndex {
    private String id;
    private String name;
    private String description;
    private Double price;
    private String brand;
    private List<String> categories;
    private Integer stock;
}
