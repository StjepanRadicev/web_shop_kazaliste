package com.ecommerce.project.service;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.helper.SortValidator;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.Show;
import com.ecommerce.project.payload.ShowDTO;
import com.ecommerce.project.payload.ShowResponse;
import com.ecommerce.project.repositories.CategoryRepository;
import com.ecommerce.project.repositories.PerformanceRepository;
import com.ecommerce.project.repositories.ShowRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShowServiceImpl implements ShowService{

    @Autowired
    private PerformanceRepository performanceRepository;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public ShowDTO createShow(ShowDTO showDTO, Long categoryId) {

        Show show = modelMapper.map(showDTO, Show.class);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category", "categoryId", categoryId));

        Show showFromDb = showRepository.findByShowNameIgnoreCase(show.getShowName());

        if (showFromDb != null) {
            throw new APIException("Show with the name " + showDTO.getShowName() + " already exists !!!");
        }

        show.setCategory(category);
        Show savedShow = showRepository.save(show);

        return modelMapper.map(savedShow, ShowDTO.class);
    }

    @Override
    public ShowResponse getAllShow(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {

        SortValidator.validateSort(sortBy, sortOrder);

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable padeDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Show> showPage = showRepository.findAll(padeDetails);

        List<Show> showList = showPage.getContent();

//        if (showList.isEmpty()) {
//            throw new APIException("\"No show created till now.\"");
//        }

        List<ShowDTO> showDTOList= showList.stream()
                .map(show -> {
                    ShowDTO dto = modelMapper.map(show, ShowDTO.class);
//                    dto.setCategoryName(
//                            show.getCategory().getCategoryName()
//                    );
                    return dto;
                }).toList();

        ShowResponse showResponse = new ShowResponse();
        showResponse.setContent(showDTOList);
        showResponse.setPageNumber(showPage.getNumber());
        showResponse.setPageSize(showPage.getSize());
        showResponse.setTotalElements(showPage.getTotalElements());
        showResponse.setTotalPages(showPage.getTotalPages());
        showResponse.setLastPage(showPage.isLast());

        return showResponse;
    }

    @Override
    public ShowDTO deleteShow(Long showId) {

        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new ResourceNotFoundException("Show", "showId", showId));
        showRepository.delete(show);

        return modelMapper.map(show, ShowDTO.class);
    }

    @Override
    public ShowDTO updateShow(ShowDTO showDTO, Long showId) {
        // Get the existing show from DB
        Show showFromDb = showRepository.findById(showId)
                .orElseThrow(()-> new ResourceNotFoundException("Show", "showId", showId));

        Show show = modelMapper.map(showDTO, Show.class);

        Show savedShowByName = showRepository.findByShowNameIgnoreCase(show.getShowName());

        if (savedShowByName != null) {
            if (!savedShowByName.getShowId().equals(showId)) {
                throw new APIException("Show with the name " + show.getShowName() + " already exists !");
            }
        }


        // Update the product info with the one in request body
        showFromDb.setShowName(show.getShowName());
        showFromDb.setDuration(show.getDuration());
        showFromDb.setDescription(show.getDescription());

        Show updatedShow = showRepository.save(showFromDb);

        return  modelMapper.map(updatedShow, ShowDTO.class);
    }
}





































