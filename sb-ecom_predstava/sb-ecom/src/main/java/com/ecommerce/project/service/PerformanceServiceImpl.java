package com.ecommerce.project.service;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.helper.JsonPatchUtils;
import com.ecommerce.project.helper.SortHelper;
import com.ecommerce.project.helper.SortValidator;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.Performance;
import com.ecommerce.project.model.Show;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.PerformanceDTO;
import com.ecommerce.project.payload.PerformanceResponse;
import com.ecommerce.project.repositories.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.criteria.Join;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.persistence.criteria.Predicate;


@Service
public class PerformanceServiceImpl implements PerformanceService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private  CartService cartService;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private PerformanceRepository performanceRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ObjectMapper objectMapper;


    @Override
    public PerformanceDTO addPerformance(Long showId, PerformanceDTO performanceDTO) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new ResourceNotFoundException("Show", "showId", showId));

        Performance performance = modelMapper.map(performanceDTO, Performance.class);

        Performance savedPerformanceByName = performanceRepository.findByPerformanceNameIgnoreCase(performance.getPerformanceName());

        if (savedPerformanceByName != null  ) {
            throw new APIException("Product with the name " + performance.getPerformanceName() + " already exists !!!");
        }


        performance.setImage("default.png");
        performance.setShow(show);
        double specialPrice = performance.getPrice() - ((performance.getDiscount() * 0.01) * performance.getPrice());
        performance.setSpecialPrice(specialPrice);
        Performance savedPerformance = performanceRepository.save(performance);


        PerformanceDTO dto = modelMapper.map(savedPerformance, PerformanceDTO.class);
        System.out.println("-> -> Ime predstave " + dto.getShowName());
        dto.setCategoryName(savedPerformance.getShow().getCategory().getCategoryName());

        return dto;
    }

    @Override
    public PerformanceResponse getAllPerformances() {
        List<Performance> performances = performanceRepository.findAll();

        Map<Long, Integer> totals = getTotalReservedQuantities();

        List<PerformanceDTO> performanceDTOS = performances.stream()
                .map(performance -> {
                    PerformanceDTO perDTO = modelMapper.map(performance, PerformanceDTO.class);
                    perDTO.setCategoryName(
                            performance.getShow().getCategory().getCategoryName()
                    );
                    perDTO.setTotalInCarts(
                            totals.getOrDefault(performance.getPerformanceId(),0)
                    );
                    return perDTO;
                })
                .collect(Collectors.toList());


        PerformanceResponse performanceResponse = new PerformanceResponse();
        performanceResponse.setContent(performanceDTOS);
        return performanceResponse;
    }



    @Override
    public PerformanceResponse searchPerformanceByKeyword(String keyword) {
        List<Performance> performances = performanceRepository.findByPerformanceNameContainingIgnoreCase(keyword);

        if(performances.isEmpty()) {
            throw new APIException("No Result");
        }

        List<PerformanceDTO> performanceDTOS = performances.stream()
                .map(product -> modelMapper.map(product, PerformanceDTO.class)).toList();

        PerformanceResponse performanceResponse = new PerformanceResponse();
        performanceResponse.setContent(performanceDTOS);
        return performanceResponse;
    }


    @Override
    public PerformanceResponse searchByCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(() ->
                new ResourceNotFoundException("Category", "categoryId", categoryId));

        List<Performance> performances = performanceRepository.findByShow_Category_CategoryId(categoryId);

        List<Performance> products1 = performanceRepository.findByShow_Category_CategoryNameOrderByPriceAsc(category.getCategoryName());

//        List<ProductDTO> productDTOS = products.stream()
//                .map(product -> modelMapper.map(product, ProductDTO.class)).toList();

        List<PerformanceDTO> performanceDTOS = products1.stream()
                .map(product -> {
                    PerformanceDTO dto = modelMapper.map(product, PerformanceDTO.class);
                    dto.setCategoryName(
                            product.getShow().getCategory().getCategoryName()
                    );
                    return dto;
                })
                .toList();


        PerformanceResponse performanceResponse = new PerformanceResponse();
        performanceResponse.setContent(performanceDTOS);
        return performanceResponse;

    }

    @Override
    public PerformanceResponse getPerformancesByAll(Integer pageNumber, Integer pageSize, Long categoryId, Long showId, String sortBy, String sortDir) {

        SortValidator.validateSort(sortBy,sortDir);

        Specification<Performance> specification = (root, query, cb) ->{
            List<Predicate> predicates = new ArrayList<>();

            Join<Performance,Show> showJoin = root.join("show");
            Join<Show,Category> categoryJoin = showJoin.join("category");

            if (categoryId != null) {
                predicates.add( cb.equal(categoryJoin.get("categoryId"), categoryId));
            }

            if(showId != null) {
                predicates.add( cb.equal(showJoin.get("showId"), showId));
            }

            // sort manual
            SortHelper.applySort(query, cb, root, sortBy, sortDir);

            return predicates.isEmpty()
                    ? cb.conjunction() // "true" predicate
                    : cb.and((jakarta.persistence.criteria.Predicate[]) predicates.toArray(new Predicate[0]));

        };

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize);

        Page<Performance> pageProducts = performanceRepository.findAll(specification, pageDetails);

        List<Performance> performanceList = pageProducts.getContent();


        //List<Product> productList = productRepository.findAll( specification);

        List<PerformanceDTO> performanceDTOS = performanceList.stream()
                .map(product -> {
                    PerformanceDTO dto = modelMapper.map(product, PerformanceDTO.class);
                    dto.setCategoryName(
                            product.getShow().getCategory().getCategoryName()
                    );
                    return dto;
                })
                .toList();


        PerformanceResponse performanceResponse = new PerformanceResponse();
        performanceResponse.setContent(performanceDTOS);
        performanceResponse.setPageNumber(pageProducts.getNumber());
        performanceResponse.setPageSize(pageProducts.getSize());
        performanceResponse.setTotalElements(pageProducts.getTotalElements());
        performanceResponse.setTotalPages(pageProducts.getTotalPages());
        performanceResponse.setLastPage(pageProducts.isLast());
        return performanceResponse;

    }


    @Override
    public PerformanceDTO updatePerformance(PerformanceDTO performanceDTO, Long performanceId) {
        // Get the existing product from DB
        Performance performanceFromDb = performanceRepository.findById(performanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", performanceId ));

        Performance performance = modelMapper.map(performanceDTO, Performance.class);

        Performance savedPerformanceByName = performanceRepository.findByPerformanceNameIgnoreCase(performance.getPerformanceName());

        if (savedPerformanceByName != null) {
            if (!savedPerformanceByName.getPerformanceId().equals(performanceId)) {
                throw new APIException("Product with the name " + performance.getPerformanceName() + " already exists !!!");
            }
        }

        if(performanceDTO.getShowId() != null) {
            Show show = showRepository.findById(performanceDTO.getShowId())
                    .orElseThrow(() -> new ResourceNotFoundException("Show", "showId", performanceDTO.getShowId()));

            performanceFromDb.setShow(show);
        }
        // Update the product info with the one in request body

        performanceFromDb.setPerformanceName(performance.getPerformanceName());
        performanceFromDb.setDescription(performance.getDescription());
        performanceFromDb.setQuantity(performanceFromDb.getQuantity() + performance.getQuantity());
        performanceFromDb.setDiscount(performance.getDiscount());
        performanceFromDb.setPrice(performance.getPrice());
        double specialPrice = performance.getPrice() - ((performance.getDiscount() * 0.01) * performance.getPrice());
        performanceFromDb.setSpecialPrice(specialPrice);

        // Save to database
        Performance savedPerformance = performanceRepository.save(performanceFromDb);

        List<Cart> carts = cartRepository.findCartsByPerformanceId(performanceId);

        List<CartDTO> cartDTOs = carts.stream().map(cart -> {
            CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

            List<PerformanceDTO> performances = cart.getCartItems().stream()
                    .map(item -> {
                        PerformanceDTO performanceDTOs = modelMapper.map(item.getPerformance(), PerformanceDTO.class);
                        performanceDTO.setQuantityInCart(item.getQuantity()); // količina u korpi
                        return performanceDTOs;
                    })
                    .collect(Collectors.toList());

            cartDTO.setPerformances(performances);
            return cartDTO;
        }).collect(Collectors.toList());

        cartDTOs.forEach(cart -> cartService.updatePerformanceInCarts(cart.getCartId(), performanceId));


        PerformanceDTO dto = modelMapper.map(savedPerformance, PerformanceDTO.class);
        dto.setCategoryName(savedPerformance.getShow().getCategory().getCategoryName());
        return dto;
    }

    @Override
    public PerformanceDTO patchedUpdatePerformance(Long performanceId, Map<String, Object> patchPayLoad) {

        Performance performanceFromDb = performanceRepository.findById(performanceId)
                .orElseThrow(()-> new ResourceNotFoundException("Performance", "performanceId", performanceId));

        // if exist product with same name
        String newPerformanceName = patchPayLoad.get("performanceName").toString();

        if (newPerformanceName != null) {
            Performance savedPerformanceByName = performanceRepository.findByPerformanceNameIgnoreCase(newPerformanceName);
            if (savedPerformanceByName != null && !savedPerformanceByName.getPerformanceId().equals(performanceId)) {
                throw new APIException("Performance with the name " + newPerformanceName + " already exists !");
            }
        }

        // if exists Id in JSON request body
        if(JsonPatchUtils.containsAnyIdKey(patchPayLoad)) {
            throw new APIException("Performance id not allowed in request body ");
        }

        Performance patchedPerformance = apply(patchPayLoad, performanceFromDb);

        Performance patchedSavedPerformance = performanceRepository.save(patchedPerformance);

        return modelMapper.map(patchedSavedPerformance, PerformanceDTO.class);
    }

    @Override
    public PerformanceDTO deletePerformance(Long performanceId) {
        Performance performanceFromDb = performanceRepository.findById(performanceId)
                .orElseThrow(()-> new ResourceNotFoundException("Performance", "performanceId", performanceId));

        List<Cart> carts = cartRepository.findCartsByPerformanceId(performanceId);
        carts.forEach(cart -> cartService.deletePerformanceFromCart(cart.getCartId(), performanceId));

        performanceRepository.delete(performanceFromDb);

        return modelMapper.map(performanceFromDb, PerformanceDTO.class);
    }

    //method for patch update
    public Performance apply(Map<String, Object> patchPayLoad, Performance tempPerformance) {

        ObjectNode performanceNode = objectMapper.convertValue(tempPerformance, ObjectNode.class);

        ObjectNode patchNode = objectMapper.convertValue(patchPayLoad, ObjectNode.class);

        performanceNode.setAll(patchNode);

        return objectMapper.convertValue(performanceNode, Performance.class);
    }

    public Map<Long, Integer> getTotalReservedQuantities() {
        List<Object[]> rows = cartItemRepository.findTotalQuantities();

        Map<Long, Integer> map = new HashMap<>();

        for (Object[] row : rows) {
            Long performanceId = (Long) row[0];
            Integer total = ((Number) row[1]).intValue();
            map.put(performanceId, total);
        }

        return map;
    }
}




























