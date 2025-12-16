package com.ecommerce.project.helper;

import com.ecommerce.project.exception.APIException;

import java.util.Set;

public class SortValidator {

    private static final Set<String> allowedFields = Set.of("naziv projekcije", "cijena", "naziv kategorije", "naziv predstave", "productName", "categoryName", "showName");

    public static void validateSort(String sortBy, String sortDir) {

        if(!allowedFields.contains(sortBy))

    {
        throw new APIException("Nevažeće polje za sortiranje: " + sortBy);
    }

        if(!sortDir.equalsIgnoreCase("asc") && !sortDir.equalsIgnoreCase("desc"))

    {
        throw new APIException("Smjer sortiranja mora biti 'asc' ili 'desc");
    }
}

}
