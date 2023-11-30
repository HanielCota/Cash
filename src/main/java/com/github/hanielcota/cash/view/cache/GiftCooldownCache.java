package com.github.hanielcota.cash.view.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.Getter;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class GiftCooldownCache {

    public static final int COOLDOWN_SECONDS = 10;
    @Getter
    private static final GiftCooldownCache instance = new GiftCooldownCache();
    private final Cache<String, Long> cache;

    private GiftCooldownCache() {
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(COOLDOWN_SECONDS, TimeUnit.SECONDS)
                .build();
    }

    public Optional<Long> getLastCollectedTime(String playerName) {
        return Optional.ofNullable(cache.getIfPresent(playerName));
    }

    public void updateLastCollectedTime(String playerName) {
        cache.asMap().compute(playerName, (key, value) -> System.currentTimeMillis());
    }
}
