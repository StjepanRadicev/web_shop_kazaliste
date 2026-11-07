
package com.ecommerce.project.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private final ConcurrentHashMap<String, Bucket> buckets =
            new ConcurrentHashMap<>();

    public boolean allowRequest(String ipAddress) {

        Bucket bucket = buckets.computeIfAbsent(
                ipAddress,
                ip -> createBucket()
        );

        return bucket.tryConsume(1);
    }

    private Bucket createBucket() {

        Bandwidth limit = Bandwidth.classic(
                5,
                Refill.intervally(5, Duration.ofMinutes(10))
        );

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
