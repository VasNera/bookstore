package com.neratzis.bookstore.service;

import com.neratzis.bookstore.core.exceptions.EntityAlreadyExistsException;
import com.neratzis.bookstore.core.exceptions.EntityNotFoundException;
import com.neratzis.bookstore.core.exceptions.InvalidArgumentException;
import com.neratzis.bookstore.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IProductService {

    Page<ProductSummaryDTO> getAllProducts(ProductFilterDTO productFilterDTO, Pageable pageable);

    ProductDetailDTO getProductById(Long id) throws EntityNotFoundException;

    BookReadOnlyDTO createBook(BookInsertDTO bookInsertDTO) throws EntityAlreadyExistsException,
            EntityNotFoundException, InvalidArgumentException;

    MusicReadOnlyDTO createMusic(MusicInsertDTO musicInsertDTO) throws EntityAlreadyExistsException,
            EntityNotFoundException,InvalidArgumentException;

    ToyReadOnlyDTO createToy(ToyInsertDTO toyInsertDTO) throws EntityAlreadyExistsException,
            EntityNotFoundException,InvalidArgumentException;

    StationeryReadOnlyDTO createStationery(StationeryInsertDTO stationeryInsertDTO) throws EntityAlreadyExistsException,
            EntityNotFoundException,InvalidArgumentException;

    BookReadOnlyDTO updateBook(Long id, BookInsertDTO bookInsertDTO) throws EntityAlreadyExistsException,
            EntityNotFoundException,InvalidArgumentException;

    MusicReadOnlyDTO updateMusic(Long id, MusicInsertDTO musicInsertDTO) throws EntityAlreadyExistsException,
            EntityNotFoundException,InvalidArgumentException;

    ToyReadOnlyDTO updateToy(Long id, ToyInsertDTO toyInsertDTO) throws EntityAlreadyExistsException,
            EntityNotFoundException,InvalidArgumentException;

    StationeryReadOnlyDTO updateStationery(Long id, StationeryInsertDTO stationeryInsertDTO) throws EntityAlreadyExistsException,
            EntityNotFoundException,InvalidArgumentException;

    void deleteProduct(Long id) throws EntityNotFoundException;

    void toggleFeatured(Long id) throws EntityNotFoundException;
}
