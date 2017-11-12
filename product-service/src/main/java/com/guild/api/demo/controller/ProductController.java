package com.guild.api.demo.controller;

import com.guild.api.demo.model.Product;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductController {

    @GetMapping(value = "/products/{productId}")
    public @ResponseBody Product getProduct(@PathVariable String productId) throws InterruptedException {
        Thread.sleep(1000);
        return new Product(productId, "Mac Book 15");
    }

}
