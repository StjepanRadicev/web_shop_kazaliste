package com.ecommerce.project.service;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.helper.JsonPatchUtils;
import com.ecommerce.project.helper.SortHelper;
import com.ecommerce.project.helper.SortValidator;
import com.ecommerce.project.model.*;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.PerformanceDTO;
import com.ecommerce.project.payload.PerformanceResponse;
import com.ecommerce.project.repositories.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.criteria.Join;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;


import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import jakarta.persistence.criteria.Predicate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


@Service
@RequiredArgsConstructor
public class PerformanceServiceImpl implements PerformanceService {


    private final CartItemRepository cartItemRepository;


    private final CartService cartService;


    private final CartRepository cartRepository;


    private final PerformanceRepository performanceRepository;


    private final CategoryRepository categoryRepository;


    private final ShowRepository showRepository;


    private final SeatRepository seatRepository;


    private final PerformanceSeatRepository performanceSeatRepository;


    private final HallRepository hallRepository;


    private final  ModelMapper modelMapper;


    private final ObjectMapper objectMapper;


    private final FileService fileService;


    @Value("${project.image}")
    private String path;

    @Value("${image.base.url}")
    private String imageBaseUrl;


    @Transactional
    @Override
    public PerformanceDTO addPerformance(Long showId, Long hallId,  PerformanceDTO performanceDTO) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new ResourceNotFoundException("Show", "showId", showId));

        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new ResourceNotFoundException("Hall", "hallId", hallId));

        Performance performance = modelMapper.map(performanceDTO, Performance.class);

        LocalDateTime newStart = performance.getLocalDateTime();


        if (!newStart.isAfter(LocalDateTime.now())) {
            throw new APIException(
                    "Vrijeme početka izvedbe mora biti u budućnosti."
            );
        }

        LocalDateTime newEnd = newStart.plusMinutes(show.getDuration());

        List<Performance> overlapping = performanceRepository.findOverlappingPerformances(newStart, newEnd, hallId);

        if (!overlapping.isEmpty()) {
            String message = overlapping.stream()
                    .map(p -> "Predstava: " + p.getShow().getShowName() +
                            ", početak: " + p.getLocalDateTime())
                    .collect(Collectors.joining(" | "));

            throw new APIException("Termin se preklapa s postojećim predstavama: " + message);
        }


        // 2)  sva sjedala dvorane
        // Long hallId = savedPerformance.getHall().getHallId();
        List<Seat> seats = seatRepository.findByHall_HallId(hallId);

        if (seats.isEmpty()) {
            //throw new IllegalStateException("Hall has no seats. Create seats first.");
            throw new APIException("Hall has no seats. Create seats first.");
        }

        Integer seatNumber =  seats.size();

        // 1)
        performance.setImage("default.png");
        performance.setShow(show);
        performance.setHall(hall);
        performance.setTotalSeats(seatNumber);
        performance.setAvailableSeats(seatNumber);
        performance.setStatus(PerformanceStatus.DRAFT);

        double specialPrice = performance.getPrice()
                - ((performance.getDiscount() * 0.01) * performance.getPrice());

        specialPrice = BigDecimal.valueOf(specialPrice)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();

        performance.setSpecialPrice(specialPrice);
        Performance savedPerformance = performanceRepository.save(performance);




        // 3) PerformanceSeat za svako sjedalo
        List<PerformanceSeat> inventory = seats.stream().map(seat -> {
            PerformanceSeat ps = new PerformanceSeat();
            ps.setPerformance(savedPerformance);
            ps.setSeat(seat);
            ps.setStatus(PerformanceSeatStatus.AVAILABLE);
            ps.setHeldUntil(null);
            ps.setHeldByCartId(null);
            ps.setPrice(savedPerformance.getSpecialPrice());
            return ps;
        }).toList();

        performanceSeatRepository.saveAll(inventory);


        PerformanceDTO dto = modelMapper.map(savedPerformance, PerformanceDTO.class);
        System.out.println("-> -> Ime predstave " + dto.getShowName());
        dto.setCategoryName(savedPerformance.getShow().getCategory().getCategoryName());

        return dto;
    }

    @Override
    public PerformanceResponse getAllPerformances() {
        List<Performance> performances = performanceRepository.findAll();

        //Map<Long, Integer> totals = getTotalReservedQuantities();

        List<PerformanceDTO> performanceDTOS = performances.stream()
                .map(performance -> {
                    PerformanceDTO perDTO = modelMapper.map(performance, PerformanceDTO.class);
                    perDTO.setCategoryName(
                            performance.getShow().getCategory().getCategoryName()
                    );
//                    perDTO.setTotalInCarts(
//                            totals.getOrDefault(performance.getPerformanceId(),0)
//                    );
                    return perDTO;
                })
                .collect(Collectors.toList());


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
    public PerformanceResponse getPerformancesByAll(Integer pageNumber, Integer pageSize, String category, String showId, String sortBy, String sortDir, String name) {

        SortValidator.validateSort(sortBy,sortDir);



        Specification<Performance> specification = (root, query, cb) ->{
            List<Predicate> predicates = new ArrayList<>();

            Join<Performance,Show> showJoin = root.join("show");
            Join<Show,Category> categoryJoin = showJoin.join("category");

            query.distinct(true);

            if (category != null) {
                predicates.add( cb.equal(categoryJoin.get("categoryName"), category));
            }

            if(showId != null) {
                predicates.add( cb.equal(showJoin.get("showId"), showId));
            }

            predicates.add(
                    cb.greaterThan(root.get("localDateTime"), cb.currentTimestamp())
            );

            predicates.add(
                    cb.equal(root.get("status"), PerformanceStatus.PUBLISHED)
            );

            if (name != null && !name.isBlank()) {
                predicates.add(
                        cb.like(
                                cb.lower(showJoin.get("showName")),
                                "%" + name.toLowerCase() + "%"
                        )
                );
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

        int numberSeat = 0;

        for(Performance performance : performanceList ) {
            numberSeat = performanceSeatRepository.countSeatsByPerformanceIdAndStatus(performance.getPerformanceId(), PerformanceSeatStatus.AVAILABLE);

            performance.setAvailableSeats(numberSeat);

        }


        //List<Product> productList = productRepository.findAll( specification);

        List<PerformanceDTO> performanceDTOS = performanceList.stream()
                .map(performance -> {
                    PerformanceDTO dto = modelMapper.map(performance, PerformanceDTO.class);
                    dto.setCategoryName(performance.getShow().getCategory().getCategoryName());
                    dto.setImage(constructImageUrl(performance.getImage()));
                    dto.setAvailableSeats(performance.getAvailableSeats());
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



    private String constructImageUrl(String imageName) {
        return imageBaseUrl.endsWith("/") ? imageBaseUrl + imageName : imageBaseUrl + "/" + imageName;
    }



    @Transactional
    @Override
    public PerformanceDTO updatePerformance(PerformanceDTO performanceDTO, Long performanceId) {
        // Get the existing product from DB
        Performance performanceFromDb = performanceRepository.findById(performanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", performanceId ));

        // provjera jel izvedba završila
        if (performanceFromDb.getStatus() == PerformanceStatus.FINISHED) {
            throw new APIException(
                    "Završenu izvedbu nije moguće ažurirati."
            );
        }

        Performance performance = modelMapper.map(performanceDTO, Performance.class);


        Show show = performanceFromDb.getShow();

        if (performanceDTO.getShowId() != null) {
            show = showRepository.findById(performanceDTO.getShowId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Show", "showId", performanceDTO.getShowId()
                            )
                    );
        }

        // provjera jel ima prodanih tiketa za izvedbu koju želimo ažurirati
        boolean hasSoldTickets = performanceSeatRepository.existsByPerformance_PerformanceIdAndStatus(
                        performanceId,
                        PerformanceSeatStatus.SOLD
        );
        // provera što se želi ažurirati
        boolean showChanged =
                !performanceFromDb.getShow().getShowId()
                        .equals(show.getShowId());

        boolean dateChanged =
                !performanceFromDb.getLocalDateTime()
                        .equals(performanceDTO.getLocalDateTime());

        //provjera ako  ima prodanma bar jedna ulaznica za sada administrator ne može ništa mjenjati
        boolean hasHeldSeats =
                performanceSeatRepository.existsByPerformance_PerformanceIdAndStatus(
                        performanceId,
                        PerformanceSeatStatus.HELD
                );

        if (hasSoldTickets || hasHeldSeats) {
            throw new APIException(
                    "Izvedbu nije moguće ažurirati jer postoje prodane " +
                            "ulaznice ili rezervirana sjedala."
            );
        }



        Hall hall = performanceFromDb.getHall();
        LocalDateTime newStart = performance.getLocalDateTime();

        if(newStart.isBefore(LocalDateTime.now() ) ) {
            throw new APIException(
                    "Vrijeme početka izvedbe mora biti u budućnosti."
            );
        }

        LocalDateTime newEnd = newStart.plusMinutes(show.getDuration());

        List<Performance> overlapping =
                performanceRepository.findOverlappingPerformances(
                        newStart, newEnd, hall.getHallId()
                );

        overlapping.removeIf(p ->
                p.getPerformanceId().equals(performanceId)
        );


        if (!overlapping.isEmpty()) {
            String message = overlapping.stream()
                    .map(p -> "Predstava: " + p.getShow().getShowName() +
                            ", početak: " + p.getLocalDateTime())
                    .collect(Collectors.joining(" | "));

            throw new APIException("Termin se preklapa s postojećim predstavama: " + message);
        }


        // provjera ststusa izvedbe

        PerformanceStatus newStatus = performanceDTO.getStatus();

        if (newStatus == PerformanceStatus.FINISHED ||
                newStatus == PerformanceStatus.SOLD_OUT) {

            throw new APIException(
                    "Status FINISHED ili SOLD_OUT nije moguće ručno postaviti."
            );
        }

        performanceFromDb.setShow(show);
        performanceFromDb.setDiscount(performance.getDiscount());
        performanceFromDb.setPrice(performance.getPrice());


        double specialPrice = performanceDTO.getPrice()
                - ((performanceDTO.getDiscount() * 0.01)
                * performanceDTO.getPrice());

        specialPrice = BigDecimal.valueOf(specialPrice)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();

        performanceFromDb.setSpecialPrice(specialPrice);

        // Ažuriranje cijene slobodnih sjedala
        List<PerformanceSeat> availableSeats =
                performanceSeatRepository.findByPerformance_PerformanceIdAndStatus(
                        performanceId,
                        PerformanceSeatStatus.AVAILABLE
                );

        for (PerformanceSeat seat : availableSeats) {
            seat.setPrice(specialPrice);
        }

        performanceSeatRepository.saveAll(availableSeats);

        performanceFromDb.setStatus(newStatus);


        performanceFromDb.setLocalDateTime(performanceDTO.getLocalDateTime());
        // Save to database
        Performance savedPerformance = performanceRepository.save(performanceFromDb);


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

//        if (newPerformanceName != null) {
//            Performance savedPerformanceByName = performanceRepository.findByPerformanceNameIgnoreCase(newPerformanceName);
//            if (savedPerformanceByName != null && !savedPerformanceByName.getPerformanceId().equals(performanceId)) {
//                throw new APIException("Performance with the name " + newPerformanceName + " already exists !");
//            }
//        }

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

//        List<Cart> carts = cartRepository.findCartsByPerformanceId(performanceId);
//        carts.forEach(cart -> cartService.deletePerformanceFromCart(cart.getCartId(), performanceId));

        performanceRepository.delete(performanceFromDb);

        return modelMapper.map(performanceFromDb, PerformanceDTO.class);
    }

    @Override
    public PerformanceDTO updatePerformanceImage(Long performanceId, MultipartFile image) throws IOException {
        // Get the performance from DB
        Performance performanceFromDB = performanceRepository.findById(performanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Performance", "performanceId", performanceId));

        // Upload image to server
        // Get the file name of uploaded image
        String fileName = fileService.uploadImage(path, image);

        // Updating the new file name to the performance
        performanceFromDB.setImage(fileName);

        // Save updated performance
        Performance updatedPerformance = performanceRepository.save(performanceFromDB);

        // return DTO after mapping performance to DTO
        return modelMapper.map(updatedPerformance, PerformanceDTO.class);
    }

    @Override
    public PerformanceResponse getAllPerformances(Integer pageNumber, Integer pageSize, String sortBy, String sortDir) {
        SortValidator.validateSort(sortBy,sortDir);



        Specification<Performance> specification = (root, query, cb) ->{
            List<Predicate> predicates = new ArrayList<>();

            Join<Performance,Show> showJoin = root.join("show");
            Join<Show,Category> categoryJoin = showJoin.join("category");

            query.distinct(true);

//            if (category != null) {
//                predicates.add( cb.equal(categoryJoin.get("categoryName"), category));
//            }
//
//            if(showId != null) {
//                predicates.add( cb.equal(showJoin.get("showId"), showId));
//            }

//            predicates.add(
//                    cb.greaterThan(root.get("localDateTime"), cb.currentTimestamp())
//            );

//            predicates.add(
//                    cb.equal(root.get("status"), PerformanceStatus.PUBLISHED)
//            );

//            if (name != null && !name.isBlank()) {
//                predicates.add(
//                        cb.like(
//                                cb.lower(showJoin.get("showName")),
//                                "%" + name.toLowerCase() + "%"
//                        )
//                );
//            }
            // sort manual
            SortHelper.applySort(query, cb, root, sortBy, sortDir);

            return predicates.isEmpty()
                    ? cb.conjunction() // "true" predicate
                    : cb.and((jakarta.persistence.criteria.Predicate[]) predicates.toArray(new Predicate[0]));

        };

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize);

        Page<Performance> pageProducts = performanceRepository.findAll(specification, pageDetails);

        List<Performance> performanceList = pageProducts.getContent();

        int numberSeat = 0;

        for(Performance performance : performanceList ) {
            numberSeat = performanceSeatRepository.countSeatsByPerformanceIdAndStatus(performance.getPerformanceId(), PerformanceSeatStatus.AVAILABLE);

            performance.setAvailableSeats(numberSeat);

        }


        //List<Product> productList = productRepository.findAll( specification);

        List<PerformanceDTO> performanceDTOS = performanceList.stream()
                .map(performance -> {
                    PerformanceDTO dto = modelMapper.map(performance, PerformanceDTO.class);
                    dto.setCategoryName(performance.getShow().getCategory().getCategoryName());
                    dto.setImage(constructImageUrl(performance.getImage()));
                    dto.setAvailableSeats(performance.getAvailableSeats());
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


    //method for patch update
    public Performance apply(Map<String, Object> patchPayLoad, Performance tempPerformance) {

        ObjectNode performanceNode = objectMapper.convertValue(tempPerformance, ObjectNode.class);

        ObjectNode patchNode = objectMapper.convertValue(patchPayLoad, ObjectNode.class);

        performanceNode.setAll(patchNode);

        return objectMapper.convertValue(performanceNode, Performance.class);
    }

//    public Map<Long, Integer> getTotalReservedQuantities() {
//        //List<Object[]> rows = cartItemRepository.findTotalQuantities();
//
//        Map<Long, Integer> map = new HashMap<>();
//
//        for (Object[] row : rows) {
//            Long performanceId = (Long) row[0];
//            Integer total = ((Number) row[1]).intValue();
//            map.put(performanceId, total);
//        }
//
//        return map;
//    }
}




























