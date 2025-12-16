package com.ecommerce.project.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// klasa za odgovor greške
@Data
@NoArgsConstructor
@AllArgsConstructor
public class APIResponse {

    public String message;
    private boolean status;
}
