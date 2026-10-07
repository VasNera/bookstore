package com.neratzis.bookstore.service;


import com.neratzis.bookstore.core.exceptions.AppGenericException;
import com.neratzis.bookstore.core.exceptions.EntityAlreadyExistsException;
import com.neratzis.bookstore.core.exceptions.EntityNotFoundException;
import com.neratzis.bookstore.core.exceptions.InvalidArgumentException;
import com.neratzis.bookstore.dto.CategoryInsertDTO;
import com.neratzis.bookstore.dto.CategoryReadOnlyDTO;
import com.neratzis.bookstore.dto.CategoryUpdateDTO;
import com.neratzis.bookstore.mapper.CategoryMapper;
import com.neratzis.bookstore.model.Category;
import com.neratzis.bookstore.repository.CategoryRepository;
import com.neratzis.bookstore.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements ICategoryService{

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional(rollbackFor = AppGenericException.class)
    public CategoryReadOnlyDTO createCategory(CategoryInsertDTO categoryInsertDTO)
            throws EntityAlreadyExistsException, EntityNotFoundException {
        if (categoryRepository.existsByName(categoryInsertDTO.name())){
            throw new EntityAlreadyExistsException("CATEGORY", "Category with name: "
                    + categoryInsertDTO.name() + " already exists");
        }

        Category parent = null;
        if (categoryInsertDTO.parentCategoryId() != null){
            parent = categoryRepository.findById(categoryInsertDTO.parentCategoryId())
                    .orElseThrow(()-> new EntityNotFoundException("CATEGORY", "Parent category with id: "
                    + categoryInsertDTO.parentCategoryId() + " not found"));
        }

        Category saved = categoryRepository.save(categoryMapper.mapToCategoryEntity(categoryInsertDTO,parent));

        log.info("Created category ={}" , saved.getName());
        return categoryMapper.toCategoryDTO(saved);
    }

    @Override
    @Transactional(rollbackFor = AppGenericException.class)
    public CategoryReadOnlyDTO updateCategory(Long id, CategoryUpdateDTO categoryUpdateDTO)
            throws EntityNotFoundException, EntityAlreadyExistsException {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("CATEGORY", "Category with id:"
                        + id + " not found"));
        if (!category.getName().equals(categoryUpdateDTO.name())
                && categoryRepository.existsByName(categoryUpdateDTO.name())) {
            throw new EntityAlreadyExistsException("CATEGORY", "Category with name:"
                    + categoryUpdateDTO.name() + " already exists");
        }

        Category parent = null;
        if (categoryUpdateDTO.parentCategoryId() != null) {
            parent = categoryRepository.findById(categoryUpdateDTO.parentCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("CATEGORY", "Parent category with id: "
                            + categoryUpdateDTO.parentCategoryId() + " not found"));
        }

            categoryMapper.updateCategoryFromDTO(category, categoryUpdateDTO, parent);
            Category updated = categoryRepository.save(category);
            log.info("Updated category ={} ", updated.getName());
            return categoryMapper.toCategoryDTO(updated);
        }

    @Override
    @Transactional(rollbackFor = AppGenericException.class)
    public void deleteCategory(Long id) throws EntityNotFoundException, InvalidArgumentException {

        Category category = categoryRepository.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("CATEGORY", "Category with id:"
                + id + " not found"));
        if (!categoryRepository.findAllByParentCategory(category).isEmpty()){
            throw new InvalidArgumentException("CATEGORY", "Category has subcategories and cannot delete");
        }
        if (productRepository.existsByCategoryAndDeletedFalse(category)){
            throw new InvalidArgumentException("CATEGORY", "Category has got products. It can't delete");
        }
        categoryRepository.delete(category);
        log.info("Category ={} deleted" , category.getName());

    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryReadOnlyDTO> getCategoryTree() {
        return categoryRepository.findAllByParentCategoryIsNullOrderByNameAsc()
                .stream()
                .map(categoryMapper::toCategoryDTO)
                .toList();
    }
}
