package com.neratzis.bookstore.controller;

import com.neratzis.bookstore.core.exceptions.EntityNotFoundException;
import com.neratzis.bookstore.dto.ProductSummaryDTO;
import com.neratzis.bookstore.service.IFavouriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/favourites")
@RequiredArgsConstructor
public class FavouriteController {

    private final IFavouriteService favouriteService;

    @GetMapping
    public ResponseEntity<List<ProductSummaryDTO>> getFavourites(Authentication authentication)
            throws EntityNotFoundException {
        return new ResponseEntity<>(favouriteService.getFavourites(authentication.getName()), HttpStatus.OK);
    }

    @PostMapping("/{productId}")
    public ResponseEntity<Void> addFavourite(Authentication authentication, @PathVariable long productId)
            throws EntityNotFoundException {
        favouriteService.addFavourite(authentication.getName(), productId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> removeFavourite(Authentication authentication, @PathVariable long productId)
            throws EntityNotFoundException {
        favouriteService.removeFavourite(authentication.getName(), productId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

