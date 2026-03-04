package com.example.payment.controller;

import com.example.payment.config.GmoPgProperties;
import com.example.payment.model.Product;
import com.example.payment.repository.ProductRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ProductController {

    private final ProductRepository productRepository;
    private final GmoPgProperties gmoPgProperties;

    public ProductController(ProductRepository productRepository, GmoPgProperties gmoPgProperties) {
        this.productRepository = productRepository;
        this.gmoPgProperties = gmoPgProperties;
    }

    @GetMapping("/")
    public String products(Model model) {
        model.addAttribute("products", productRepository.findAll());
        return "products";
    }

    @GetMapping("/checkout/{productId}")
    public String checkout(@PathVariable Long productId, Model model) {
        return productRepository.findById(productId)
                .map(product -> {
                    model.addAttribute("product", product);
                    model.addAttribute("shopId", gmoPgProperties.shopId());
                    model.addAttribute("tokenJsUrl", gmoPgProperties.tokenJsUrl());
                    return "checkout";
                })
                .orElse("redirect:/");
    }
}
