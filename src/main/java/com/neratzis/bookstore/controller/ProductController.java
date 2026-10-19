package com.neratzis.bookstore.controller;


import com.neratzis.bookstore.core.exceptions.EntityNotFoundException;
import com.neratzis.bookstore.dto.ProductDetailDTO;
import com.neratzis.bookstore.dto.ProductFilterDTO;
import com.neratzis.bookstore.dto.ProductSummaryDTO;
import com.neratzis.bookstore.service.IProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final IProductService productService;

    @GetMapping
    public ResponseEntity<Page<ProductSummaryDTO>> getProducts(
            @ModelAttribute ProductFilterDTO filterDTO,
            @PageableDefault(size = 20)Pageable pageable){

        return new ResponseEntity<>(productService.getAllProducts(filterDTO,pageable), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDetailDTO> getProductById(@PathVariable long id) throws EntityNotFoundException{
        return new ResponseEntity<>(productService.getProductById(id),HttpStatus.OK);

    }

}
