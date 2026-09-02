package net.dysky.planner.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimiterService {

    private final Cache<String, Bucket> cache;

    public RateLimiterService() {
        this.cache = Caffeine.newBuilder()
                .maximumSize(50_000)
                .expireAfterAccess(Duration.ofMinutes(20))
                .build();
    }

    public Bucket getBucket(String key) {
        return cache.get(key, k -> createNewBucker());
    }

    private Bucket createNewBucker() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(50)
                .refillIntervally(50, Duration.ofMinutes(1))
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
