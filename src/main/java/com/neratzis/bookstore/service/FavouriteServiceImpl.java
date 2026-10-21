package com.neratzis.bookstore.service;


import com.neratzis.bookstore.core.exceptions.AppGenericException;
import com.neratzis.bookstore.core.exceptions.EntityNotFoundException;
import com.neratzis.bookstore.dto.ProductSummaryDTO;
import com.neratzis.bookstore.mapper.ProductMapper;
import com.neratzis.bookstore.model.Product;
import com.neratzis.bookstore.model.User;
import com.neratzis.bookstore.repository.ProductRepository;
import com.neratzis.bookstore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FavouriteServiceImpl implements IFavouriteService{

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ProductSummaryDTO> getFavourites(String username) throws EntityNotFoundException {
        return findUserWithFavourites(username).getFavourites().stream()
                .filter(product -> !product.isDeleted())
                .sorted(Comparator.comparing(Product::getTitle))
                .map(productMapper::toSummaryDTO)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = AppGenericException.class)
    public void addFavourite(String username, Long productId) throws EntityNotFoundException {

        User user = findUserWithFavourites(username);
        Product product = productRepository.findByIdAndDeletedFalse(productId)
                .orElseThrow(() -> new EntityNotFoundException("PRODUCT",
                        "Product with id: " + productId + " not found"));
        user.addFavourite(product);
        log.info("Product sku={} added to favourites of user={}", product.getSku(), username);

    }

    @Override
    @Transactional(rollbackFor = AppGenericException.class)
    public void removeFavourite(String username, Long productId) throws EntityNotFoundException {

        User user = findUserWithFavourites(username);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("PRODUCT",
                        "Product with id: " + productId + " not found"));
        user.removeFavourite(product);

    }

    private User findUserWithFavourites(String username) throws EntityNotFoundException {
        return userRepository.findWithFavouritesByUsernameAndDeletedFalse(username)
                .orElseThrow(() -> new EntityNotFoundException("USER",
                        "User " + username + " not found"));
    }
}
