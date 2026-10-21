package com.neratzis.bookstore.service;

import com.neratzis.bookstore.core.exceptions.EntityNotFoundException;
import com.neratzis.bookstore.dto.ProductSummaryDTO;

import java.util.List;

public interface IFavouriteService {

    List<ProductSummaryDTO> getFavourites(String username) throws EntityNotFoundException;

    void addFavourite(String username, Long productId) throws EntityNotFoundException;

    void removeFavourite(String username, Long productId) throws EntityNotFoundException;
}
