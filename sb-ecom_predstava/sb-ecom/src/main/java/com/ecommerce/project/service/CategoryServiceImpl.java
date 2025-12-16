package com.ecommerce.project.service;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.helper.SortValidator;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.payload.CategoryDTO;
import com.ecommerce.project.payload.CategoryResponse;
import com.ecommerce.project.repositories.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService{

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;


    @Override
    public CategoryResponse getAllCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortDir ) {

        SortValidator.validateSort(sortBy, sortDir);
        if (sortBy.equalsIgnoreCase("naziv kategorije")) {
            sortBy = "categoryName";
        }

        Sort sortByAndOrder = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Category> categoryPage = categoryRepository.findAll(pageDetails);

        List<Category> categories = categoryPage.getContent();

//        if(categories.isEmpty()) {
//            throw  new APIException("No category created till now.");
//        }

        // convert to categoryDTOS
        List<CategoryDTO> categoryDTOS = categories.stream()
                .map(category -> modelMapper.map(category, CategoryDTO.class))
                .toList();

        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setContent(categoryDTOS);
        categoryResponse.setPageNumber(categoryPage.getNumber());
        categoryResponse.setPageSize(categoryPage.getSize());
        categoryResponse.setTotalElements(categoryPage.getTotalElements());
        categoryResponse.setTotalPages(categoryPage.getTotalPages());
        categoryResponse.setLastPage(categoryPage.isLast());

        return categoryResponse;
    }

    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {

        Category category = modelMapper.map(categoryDTO, Category.class);

        Category categoryFromDb = categoryRepository.findByCategoryName(category.getCategoryName());
        if (categoryFromDb != null) {
            throw new APIException("Category with the name " + categoryDTO.getCategoryName() + " already exists !!!");
        }

        Category savedCategory = categoryRepository.save(category);

        return modelMapper.map(savedCategory, CategoryDTO.class);
    }

    @Override
    public CategoryDTO deleteCategory(Long categoryId) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        categoryRepository.delete(category);

        return modelMapper.map(category, CategoryDTO.class);




//        List<Category> categories = categoryRepository.findAll();
//
//        Category category = categories.stream()
//                .filter(c -> c.getCategoryId().equals(categoryId))
//                .findFirst()
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found"));
//
//        categoryRepository.delete(category);
//
//        return "Category with categoryId: " + categoryId + " deleted successfully !";
    }

    @Override
    public CategoryDTO updateCategory(CategoryDTO categoryDTO, Long categoryId) {

        //Optional<Category> savedCategoryOptional = categoryRepository.findById(categoryId);

        Category category = modelMapper.map(categoryDTO, Category.class);


        Category savedCategory = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        Category savedCategoryByName = categoryRepository.findByCategoryName(category.getCategoryName());
        if (savedCategoryByName != null) {
            if (!savedCategoryByName.getCategoryId().equals(categoryId)) {
                throw new APIException("Category with the name " + category.getCategoryName() + " already exists !!!");
            }
        }

        //category.setCategoryId(categoryId);

        // => Proba
        savedCategory.setCategoryName(category.getCategoryName());

        categoryRepository.save(savedCategory);

        return modelMapper.map(savedCategory, CategoryDTO.class);


//  -> Prva šema
//        List<Category> categories = categoryRepository.findAll();
//
//        Optional<Category> optionalCategory = categories.stream()
//                .filter(c -> c.getCategoryId().equals(categoryId))
//                .findFirst();
//
//        if(optionalCategory.isPresent()) {
//            Category existingCategory = optionalCategory.get();
//            existingCategory.setCategoryName(category.getCategoryName());
//            Category savedCategory = categoryRepository.save(existingCategory);
//
//            return savedCategory;
//        } else {
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found");
//        }
    }












































//    @Override
//    public Category patchCategory(Map<String, Object> patchPayLoad, Long categoryId) {
//
//        Optional<Category> optionalCategory = categories.stream()
//                .filter(c -> c.getCategoryId().equals(categoryId))
//                .findFirst();
//
//        // throw exception if request body contains "id" key
//        if (patchPayLoad.containsKey("categoryId")) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad Request");
//        }
//
//        if(optionalCategory.isPresent()) {
//            Category existingCategory = optionalCategory.get();
//            Category patchedCategory = apply(patchPayLoad, existingCategory);
//
//
//            existingCategory.setCategoryName(patchedCategory.getCategoryName());
//            return existingCategory;
//        } else {
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found");
//        }
//
//
//    }
//    //
//    public Category apply(Map<String,Object> patchPayload, Category tempCategory) {
//
//        // Convert employee object to a JSON object node
//
//        ObjectNode employeeNode = objectMapper.convertValue(tempCategory, ObjectNode.class);
//
//        // Convert the patchPayload map to a JSON object node
//        ObjectNode patchNode = objectMapper.convertValue(patchPayload, ObjectNode.class);
//
//        // Merge the patch updates into the employee node
//        employeeNode.setAll(patchNode);
//
//        return objectMapper.convertValue(employeeNode, Category.class);
//    }
}






























