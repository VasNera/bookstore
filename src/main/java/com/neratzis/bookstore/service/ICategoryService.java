package com.neratzis.bookstore.service;

import com.neratzis.bookstore.core.exceptions.EntityAlreadyExistsException;
import com.neratzis.bookstore.core.exceptions.EntityNotFoundException;
import com.neratzis.bookstore.core.exceptions.InvalidArgumentException;
import com.neratzis.bookstore.dto.CategoryInsertDTO;
import com.neratzis.bookstore.dto.CategoryReadOnlyDTO;
import com.neratzis.bookstore.dto.CategoryUpdateDTO;

import java.util.List;

public interface ICategoryService {

    CategoryReadOnlyDTO createCategory(CategoryInsertDTO categoryInsertDTO)
            throws EntityAlreadyExistsException, EntityNotFoundException;

    CategoryReadOnlyDTO updateCategory(Long id, CategoryUpdateDTO categoryUpdateDTO)
            throws EntityNotFoundException, EntityAlreadyExistsException;

    public void deleteCategory(Long id) throws EntityNotFoundException, InvalidArgumentException;

    List<CategoryReadOnlyDTO> getCategoryTree();

}
