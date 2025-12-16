package com.ecommerce.project.controller;

import com.ecommerce.project.config.AppConstants;
import com.ecommerce.project.payload.ShowDTO;
import com.ecommerce.project.payload.ShowResponse;
import com.ecommerce.project.service.ShowService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Validated
public class ShowController {

    @Autowired
    private ShowService showService;

    @PostMapping("/public/categories/{categoryId}/show")
    public ResponseEntity<ShowDTO> createShow(@Valid @RequestBody ShowDTO showDTO,
                                              @PathVariable Long categoryId) {

        ShowDTO savedShowDTO = showService.createShow(showDTO, categoryId);

        return new ResponseEntity<>(savedShowDTO, HttpStatus.CREATED);
    }

    @GetMapping("/public/show")
    public ResponseEntity<ShowResponse> getAllShow(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) @Min(0) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) @Min(1) @Max(50) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_SHOW_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder
    ){

        ShowResponse showResponse = showService.getAllShow(pageNumber, pageSize, sortBy, sortOrder);

        return new ResponseEntity<>(showResponse, HttpStatus.OK);
    }

    @DeleteMapping("/admin/show/{showId}")
    public ResponseEntity<ShowDTO> deleteShow(@PathVariable Long showId) {

        ShowDTO deleteShow = showService.deleteShow(showId);

        return new ResponseEntity<>(deleteShow, HttpStatus.OK);
    }

    @PutMapping("/public/show/{showId}")
    public ResponseEntity<ShowDTO> updateShow(@Valid @RequestBody ShowDTO showDTO, @PathVariable Long showId) {

        ShowDTO updatedShowDTO = showService.updateShow(showDTO, showId);

        return new ResponseEntity<>(updatedShowDTO, HttpStatus.OK);
    }
}




































