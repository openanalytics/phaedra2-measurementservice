/**
 * Phaedra II
 *
 * Copyright (C) 2016-2025 Open Analytics
 *
 * ===========================================================================
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Apache License as published by
 * The Apache Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.phaedra.measservice.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import eu.openanalytics.phaedra.measservice.image.ImageCodestreamAccessor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@EnableCaching
public class CachingConfig {

  @Bean
  public Caffeine caffeineConfig(Environment environment) {
    long maxBytes = Long.parseLong(
        environment.getProperty("PHAEDRA2_MEASUREMENT_CACHE_MAX_BYTES", "500000000")); // 500MB

    return Caffeine.newBuilder()
        .weigher((key, value) -> estimateSizeInBytes(value))
        .expireAfterAccess(
            Integer.parseInt(environment.getProperty("PHAEDRA2_MEASUREMENT_CACHE_TTL", "5")),
            java.util.concurrent.TimeUnit.MINUTES)
        .removalListener((key, value, cause) -> {
          if (cause != null && cause.wasEvicted()) {
            System.out.println("Cache entry removed: " + key + " (" + cause + ")");
          }
        });
  }

  private int estimateSizeInBytes(Object value) {
    if (value instanceof byte[]) {
      return ((byte[]) value).length;
    } else {
      ImageCodestreamAccessor codestream = (ImageCodestreamAccessor) value;
      return (int) codestream.getCodestreamSize();
    }
  }

  @Bean
  public CacheManager cacheManager(Caffeine caffeine) {
    CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
    caffeineCacheManager.setCaffeine(caffeine);
    return caffeineCacheManager;
  }

}
