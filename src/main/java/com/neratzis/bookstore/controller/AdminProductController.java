package com.neratzis.bookstore.controller;

import com.neratzis.bookstore.core.exceptions.EntityAlreadyExistsException;
import com.neratzis.bookstore.core.exceptions.EntityNotFoundException;
import com.neratzis.bookstore.core.exceptions.InvalidArgumentException;
import com.neratzis.bookstore.core.exceptions.ValidationException;
import com.neratzis.bookstore.dto.*;
import com.neratzis.bookstore.service.IProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasAuthority('MANAGE_PRODUCTS')")
@RequiredArgsConstructor
public class AdminProductController {

    private final IProductService productService;

    @PostMapping("/books")
    public ResponseEntity<BookReadOnlyDTO> createBook(
            @Valid @RequestBody
            BookInsertDTO insertDTO,
            BindingResult bindingResult
            ) throws ValidationException, EntityAlreadyExistsException,
            InvalidArgumentException, EntityNotFoundException{
        if (bindingResult.hasErrors()){
            throw new ValidationException("BOOK", "Invalid book data", bindingResult);
        }
        return new ResponseEntity<>(productService.createBook(insertDTO), HttpStatus.CREATED);
    }

    @PutMapping("/books/{id}")
    public ResponseEntity<BookReadOnlyDTO> updateBook(
            @PathVariable long id,
            @Valid @RequestBody
            BookInsertDTO insertDTO,
            BindingResult bindingResult
    ) throws ValidationException,EntityAlreadyExistsException,
            InvalidArgumentException,EntityNotFoundException{
        if (bindingResult.hasErrors()){
            throw new ValidationException("BOOK", "Invalid book data", bindingResult);
        }

        return new ResponseEntity<>(productService.updateBook(id,insertDTO),HttpStatus.OK);
    }

    @PostMapping("/music")
    public ResponseEntity<MusicReadOnlyDTO> createMusic(
            @Valid @RequestBody
            MusicInsertDTO insertDTO,
            BindingResult bindingResult
    ) throws ValidationException, EntityAlreadyExistsException,
    InvalidArgumentException,EntityNotFoundException{
        if (bindingResult.hasErrors()){
            throw new ValidationException("MUSIC", "Invalid music data", bindingResult);
        }
        return new ResponseEntity<>(productService.createMusic(insertDTO),HttpStatus.CREATED);
    }

    @PutMapping("/music/{id}")
    public ResponseEntity<MusicReadOnlyDTO> updateMusic(
            @PathVariable long id,
            @Valid @RequestBody
            MusicInsertDTO insertDTO,
            BindingResult bindingResult
    )throws ValidationException, EntityAlreadyExistsException,
            InvalidArgumentException,EntityNotFoundException{
        if (bindingResult.hasErrors()) {
            throw new ValidationException("MUSIC", "Invalid music data", bindingResult);
        }
            return new ResponseEntity<>(productService.updateMusic(id,insertDTO),HttpStatus.OK);
    }

    @PostMapping("/toys")
    public ResponseEntity<ToyReadOnlyDTO> createToy(
            @Valid @RequestBody
            ToyInsertDTO insertDTO,
            BindingResult bindingResult
    )throws ValidationException,EntityAlreadyExistsException,
            InvalidArgumentException,EntityNotFoundException{

        if (bindingResult.hasErrors()){
            throw new ValidationException("TOY", "Invalid toy data", bindingResult);
        }
        return new ResponseEntity<>(productService.createToy(insertDTO),HttpStatus.CREATED);
    }

    @PutMapping("/toys/{id}")
    public ResponseEntity<ToyReadOnlyDTO> updateToy(
            @PathVariable long id,
            @Valid @RequestBody
            ToyInsertDTO insertDTO,
            BindingResult bindingResult
    ) throws ValidationException,EntityAlreadyExistsException,
            InvalidArgumentException,EntityNotFoundException{
        if (bindingResult.hasErrors()){
            throw new ValidationException("TOY","Invalid toy data", bindingResult);
        }
        return new ResponseEntity<>(productService.updateToy(id,insertDTO),HttpStatus.OK);
    }

    @PostMapping("/stationery")
    public ResponseEntity<StationeryReadOnlyDTO> createStationery(
            @Valid @RequestBody
            StationeryInsertDTO insertDTO,
            BindingResult bindingResult
    ) throws ValidationException, EntityAlreadyExistsException,
            InvalidArgumentException,EntityNotFoundException{
        if (bindingResult.hasErrors()){
            throw new ValidationException("STATIONERY", "Invalid stationery data", bindingResult);
        }
        return new ResponseEntity<>(productService.createStationery(insertDTO),HttpStatus.CREATED);
    }

    @PutMapping("/stationery/{id}")
    public ResponseEntity<StationeryReadOnlyDTO> updateStationery(
            @PathVariable long id,
            @Valid @RequestBody
            StationeryInsertDTO insertDTO,
            BindingResult bindingResult
    ) throws ValidationException, EntityAlreadyExistsException,
            InvalidArgumentException, EntityNotFoundException{
        if (bindingResult.hasErrors()){
            throw new ValidationException("STATIONERY", "Invalid stationery data", bindingResult);
        }
        return new ResponseEntity<>(productService.updateStationery(id,insertDTO),HttpStatus.OK);
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable long id) throws EntityNotFoundException{
        productService.deleteProduct(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PatchMapping("/products/{id}/featured")
    public ResponseEntity<Void> toggleFeatured(@PathVariable long id)throws EntityNotFoundException{
        productService.toggleFeatured(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
