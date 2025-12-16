package com.ecommerce.project.helper;

import com.ecommerce.project.model.Performance;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;

public class SortHelper {

    public static void applySort(CriteriaQuery<?> query, CriteriaBuilder cb, Root<Performance> root, String sortBy, String sortDir) {
        Path<?> sortPath;

        if("naziv predstave".equals(sortBy)) {
            sortPath =  root.join("show").get("showName");
        }
        else if ("naziv kategorije".equals(sortBy)) {
            sortPath = root.join("show").join("category").get("categoryName");
        }
        else if ("naziv projekcije".equals(sortBy)) {
            sortPath = root.get("productName");
        }
        else if ("cijena".equals(sortBy)) {
            sortPath = root.get("price");
        }
        else {
            sortPath =  root.get(sortBy);
        }

        if ("desc".equalsIgnoreCase(sortDir)) {
            query.orderBy(cb.desc(sortPath));
        }
        else {
            query.orderBy(cb.asc(sortPath));
        }
    }
}
