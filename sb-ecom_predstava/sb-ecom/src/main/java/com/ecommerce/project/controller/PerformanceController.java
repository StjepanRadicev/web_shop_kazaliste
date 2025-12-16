package com.ecommerce.project.controller;

import com.ecommerce.project.config.AppConstants;
import com.ecommerce.project.payload.PerformanceDTO;
import com.ecommerce.project.payload.PerformanceResponse;
import com.ecommerce.project.service.PerformanceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@Validated
public class PerformanceController {

    @Autowired
    PerformanceService performanceService;

    @PostMapping("/admin/show/{showId}/performance")
    public ResponseEntity<PerformanceDTO> addPerformance(@Valid @RequestBody PerformanceDTO performanceDTO,
                                                     @PathVariable Long showId) {
        PerformanceDTO savedPerformanceDTO = performanceService.addPerformance(showId, performanceDTO);

        return  new ResponseEntity<>(savedPerformanceDTO, HttpStatus.CREATED);
    }



    @GetMapping("/public/performances")
    public ResponseEntity<PerformanceResponse> getAllPerformance() {
        PerformanceResponse performanceResponse = performanceService.getAllPerformances();
        return new ResponseEntity<>(performanceResponse, HttpStatus.OK);
    }

    @GetMapping("/public/performances/keyword/{keyword}")
    public ResponseEntity<PerformanceResponse> getPerformanceByKeyword(@PathVariable String keyword) {
        PerformanceResponse performanceResponse = performanceService.searchPerformanceByKeyword(keyword);
        return new ResponseEntity<>(performanceResponse, HttpStatus.FOUND);
    }

    @GetMapping("/public/categories/{categoryId}/performances")
    public ResponseEntity<PerformanceResponse> getPerformanceByCategory(@PathVariable Long categoryId) {
        PerformanceResponse performanceResponse = performanceService.searchByCategory(categoryId);

        return new ResponseEntity<>(performanceResponse, HttpStatus.OK);
    }


    @GetMapping("/public/performances/all")
    public ResponseEntity<PerformanceResponse> getPerformanceByAll(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER,required = false)@Min(0) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false)@Min(1) @Max(100) Integer pageSize,
            @RequestParam(required = false)@Min(1) Long categoryId,
            @RequestParam(required = false)@Min(1) Long showId,
            @RequestParam(defaultValue = AppConstants.SORT_PERFORMANCE_BY, required = false) String sortBy,
            @RequestParam(defaultValue = AppConstants.SORT_DIR, required = false) String sortDir) {

        PerformanceResponse performanceResponse = performanceService.getPerformancesByAll(pageNumber, pageSize, categoryId, showId, sortBy, sortDir);

        return  new ResponseEntity<>(performanceResponse, HttpStatus.OK);
    }

    @PutMapping("/admin/performance/{performanceId}")
    public ResponseEntity<PerformanceDTO> updatePerformance(@RequestBody PerformanceDTO performanceDTO,
                                                        @PathVariable Long performanceId) {

        PerformanceDTO updatedPerformanceDTO = performanceService.updatePerformance(performanceDTO, performanceId);

        return new ResponseEntity<>(updatedPerformanceDTO, HttpStatus.OK);
    }

    // PatchMapping updateMethod
    @PatchMapping("/admin/performance/{performanceId}")
    public ResponseEntity<PerformanceDTO> updatePerformance(@RequestBody Map<String, Object> patchPayLoad,
                                                            @PathVariable Long performanceId) {

        PerformanceDTO updatedPerformanceDTO = performanceService.patchedUpdatePerformance(performanceId, patchPayLoad);

        return new ResponseEntity<>(updatedPerformanceDTO, HttpStatus.OK);
    }

    @DeleteMapping("/admin/performance/{performanceId}")
    public ResponseEntity<PerformanceDTO> deletePerformance(@PathVariable Long performanceId) {

        PerformanceDTO performanceDTO = performanceService.deletePerformance(performanceId);

        return new ResponseEntity<PerformanceDTO>(performanceDTO, HttpStatus.OK);
    }
}






































