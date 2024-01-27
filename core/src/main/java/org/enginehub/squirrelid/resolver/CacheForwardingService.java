/*
 * SquirrelID, a UUID library for Minecraft
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) SquirrelID team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.enginehub.squirrelid.resolver;

import com.google.common.collect.ImmutableList;
import org.enginehub.squirrelid.Profile;
import org.enginehub.squirrelid.cache.ProfileCache;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Resolves UUIDs using another resolver and stores results to a cache.
 */
public class CacheForwardingService implements ProfileService {

    private final ProfileService resolver;
    private final ProfileCache cache;

    private final Duration maxCacheTime;

    /**
     * Create a new instance.
     *
     * @param resolver the resolver to use
     * @param cache    the cache to use
     */
    public CacheForwardingService(ProfileService resolver, ProfileCache cache, final Duration maxCacheTime) {
        checkNotNull(resolver);
        checkNotNull(cache);

        this.resolver = resolver;
        this.cache = cache;
        this.maxCacheTime = maxCacheTime;
    }

    /**
     * Create a new instance.
     *
     * @param resolver the resolver to use
     * @param cache    the cache to use
     */
    public CacheForwardingService(ProfileService resolver, ProfileCache cache) {
        this(resolver, cache, Duration.ofHours(6));
    }

    @Override
    public int getIdealRequestLimit() {
        return resolver.getIdealRequestLimit();
    }

    @Nullable @Override
    public Profile findByName(String name) {
        // check cache first
        Profile profile = cache.getIfPresent(name);

        if (profile != null && isProfileValid(profile)) {
            // Cache is valid, return the profile
            return profile;
        }

        // Cache is invalid or profile is not in the cache, fetch from resolver
        Profile resolverProfile = resolver.findByName(name);

        if (resolverProfile != null) {
            // Update the cache with the new profile
            cache.put(resolverProfile);
        }

        return resolverProfile;
    }

    @Override
    public ImmutableList<Profile> findAllByName(Iterable<String> names) {
        ImmutableList<Profile> profiles = resolver.findAllByName(names);
        for (Profile profile : profiles) {
            cache.put(profile);
        }
        return profiles;
    }

    @Override
    public void findAllByName(Iterable<String> names, final Predicate<Profile> consumer) {
        resolver.findAllByName(names, input -> {
            cache.put(input);
            return consumer.test(input);
        });
    }

    @Nullable @Override
    public Profile findByUuid(UUID uuid) {
        // check cache first
        Profile profile = cache.getIfPresent(uuid);

        if (profile != null && isProfileValid(profile)) {
            // Cache is valid, return the profile
            return profile;
        }

        // Cache is invalid or profile is not in the cache, fetch from resolver
        Profile resolverProfile = resolver.findByUuid(uuid);

        if (resolverProfile != null) {
            // Update the cache with the new profile
            cache.put(resolverProfile);
        }

        return resolverProfile;
    }

    private boolean isProfileValid(Profile profile) {
        Instant cachedTime = Instant.ofEpochMilli(profile.timeCached());
        Instant expirationTime = cachedTime.plus(this.maxCacheTime);

        return expirationTime.isAfter(Instant.now());
    }

    @Override
    public ImmutableList<Profile> findAllByUuid(Iterable<UUID> uuids) {
        ImmutableList<Profile> profiles = resolver.findAllByUuid(uuids);
        for (Profile profile : profiles) {
            cache.put(profile);
        }
        return profiles;
    }

    @Override
    public void findAllByUuid(Iterable<UUID> uuids, Predicate<Profile> consumer) {
        resolver.findAllByUuid(uuids, input -> {
            cache.put(input);
            return consumer.test(input);
        });
    }
}
