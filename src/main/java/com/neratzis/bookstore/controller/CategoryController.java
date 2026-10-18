package com.neratzis.bookstore.controller;


import com.neratzis.bookstore.dto.CategoryReadOnlyDTO;
import com.neratzis.bookstore.service.ICategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final ICategoryService categoryService;


    @GetMapping
    public ResponseEntity<List<CategoryReadOnlyDTO>> getCategoryTree(){
        return new ResponseEntity<>(categoryService.getCategoryTree(), HttpStatus.OK);

    }
}
