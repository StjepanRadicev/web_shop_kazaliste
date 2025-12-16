package com.ecommerce.project.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "demo.users")
@Component
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemoUsersProperties {

    private User admin;
    private User user;
    private User seller;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class User {
        private String username;
        private String password;
    }
}
