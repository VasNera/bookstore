package com.neratzis.bookstore.controller;


import com.neratzis.bookstore.core.exceptions.EntityAlreadyExistsException;
import com.neratzis.bookstore.core.exceptions.EntityNotFoundException;
import com.neratzis.bookstore.core.exceptions.InvalidArgumentException;
import com.neratzis.bookstore.core.exceptions.ValidationException;
import com.neratzis.bookstore.dto.CategoryInsertDTO;
import com.neratzis.bookstore.dto.CategoryReadOnlyDTO;
import com.neratzis.bookstore.dto.CategoryUpdateDTO;
import com.neratzis.bookstore.service.ICategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final ICategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryReadOnlyDTO> createCategory(
            @Valid @RequestBody
            CategoryInsertDTO categoryInsertDTO,
            BindingResult bindingResult
            ) throws ValidationException, EntityAlreadyExistsException, EntityNotFoundException{
        if (bindingResult.hasErrors()){
            throw new ValidationException("CATEGORY", "Invalid category data", bindingResult);
        }
        return new ResponseEntity<>(categoryService.createCategory(categoryInsertDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryReadOnlyDTO> updateCategory(
            @PathVariable long id,
            @Valid @RequestBody
            CategoryUpdateDTO categoryUpdateDTO,
            BindingResult bindingResult
    )throws ValidationException,EntityNotFoundException,EntityAlreadyExistsException, InvalidArgumentException {

        if (bindingResult.hasErrors()){
            throw new ValidationException("CATEGORY", "Invalid category data", bindingResult);
        }
        return new ResponseEntity<>(categoryService.updateCategory(id, categoryUpdateDTO),HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable long id) throws EntityNotFoundException,
            InvalidArgumentException{
        categoryService.deleteCategory(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
