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
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Iterables;
import dev.mizule.squirrelid.core.util.HttpUtil;
import org.enginehub.squirrelid.Profile;
import org.enginehub.squirrelid.util.HttpRequests;
import org.enginehub.squirrelid.util.UUIDs;
import org.json.simple.JSONObject;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Resolves names in bulk to UUIDs using Mojang's profile HTTP API.
 */
public class CraftHeadHttpRepositoryService implements ProfileService {

    public static final String MINECRAFT_AGENT = "powercasgamer/squirrelid";

    public static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    private static final Logger log = Logger.getLogger(CraftHeadHttpRepositoryService.class.getCanonicalName());
    private static final int MAX_NAMES_PER_REQUEST = 100;

    private final URI profilesURL;
    private final Function<UUID, URL> nameHistoryUrlCreator;
    private int maxRetries = 5;
    private long retryDelay = 50;

    /**
     * Create a new resolver.
     *
     * <p>For Minecraft, use the {@link #MINECRAFT_AGENT} constant. The UUID
     * to name mapping is only available if a user owns the game for the
     * provided "agent," so an incorrect agent may return zero results or
     * incorrect results.</p>
     *
     * @param agent the agent (i.e. the game)
     */
    public CraftHeadHttpRepositoryService(String agent) {
        checkNotNull(agent);
        profilesURL = HttpRequests.uri("https://crafthead.net/profile/");
        nameHistoryUrlCreator = (uuid) -> {
            try {
                return HttpRequests.uri("https://playerdb.co/api/player/minecraft/" + uuid.toString()).toURL();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        };
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private static Profile decodeProfileResult(Object entry) {
        try {
            if (entry instanceof Map) {
                Map<Object, Object> mapEntry = (Map<Object, Object>) entry;
                Object rawUuid = mapEntry.get("id");
                Object rawName = mapEntry.get("name");

                if (rawUuid != null && rawName != null) {
                    UUID uuid = UUID.fromString(UUIDs.addDashes(String.valueOf(rawUuid)));
                    String name = String.valueOf(rawName);
                    return new Profile(uuid, name, System.currentTimeMillis());
                }
            } else if (entry instanceof JSONObject jsonObject) {
                Object rawName = jsonObject.get("name");
                UUID uuid = UUID.fromString(UUIDs.addDashes(String.valueOf(jsonObject.get("id"))));
                if (rawName != null) {
                    return new Profile(uuid, String.valueOf(rawName), System.currentTimeMillis());
                }
            }
        } catch (ClassCastException | IllegalArgumentException e) {
            log.log(Level.WARNING, "Got invalid value from UUID lookup service", e);
        }

        return null;
    }

    /**
     * Create a resolver for Minecraft.
     *
     * @return a UUID resolver
     */
    public static ProfileService forMinecraft() {
        return new CraftHeadHttpRepositoryService(MINECRAFT_AGENT);
    }

    /**
     * Get the maximum number of HTTP request retries.
     *
     * @return the maximum number of retries
     */
    public int getMaxRetries() {
        return maxRetries;
    }

    /**
     * Set the maximum number of HTTP request retries.
     *
     * @param maxRetries the maximum number of retries
     */
    public void setMaxRetries(int maxRetries) {
        checkArgument(maxRetries > 0, "maxRetries must be >= 0");
        this.maxRetries = maxRetries;
    }

    /**
     * Get the number of milliseconds to delay after each failed HTTP request,
     * doubling each time until success or total failure.
     *
     * @return delay in milliseconds
     */
    public long getRetryDelay() {
        return retryDelay;
    }

    /**
     * Set the number of milliseconds to delay after each failed HTTP request,
     * doubling each time until success or total failure.
     *
     * @param retryDelay delay in milliseconds
     */
    public void setRetryDelay(long retryDelay) {
        this.retryDelay = retryDelay;
    }

    @Override
    public int getIdealRequestLimit() {
        return MAX_NAMES_PER_REQUEST;
    }

    @Nullable
    @Override
    public Profile findByName(String name) {
        ImmutableList<Profile> profiles = findAllByName(ImmutableList.of(name));
        if (!profiles.isEmpty()) {
            return profiles.get(0);
        } else {
            return null;
        }
    }

    @Override
    public ImmutableList<Profile> findAllByName(Iterable<String> names) {
        Builder<Profile> builder = ImmutableList.builder();
        for (List<String> partition : Iterables.partition(names, MAX_NAMES_PER_REQUEST)) {
            builder.addAll(queryByName(partition));
        }
        return builder.build();
    }

    @Override
    public void findAllByName(Iterable<String> names, Predicate<Profile> consumer) {
        for (List<String> partition : Iterables.partition(names, MAX_NAMES_PER_REQUEST)) {
            for (Profile profile : queryByName(partition)) {
                consumer.test(profile);
            }
        }
    }

    @Nullable
    @Override
    public Profile findByUuid(UUID uuid) {
        ImmutableList<Profile> profiles = findAllByUuid(ImmutableList.of(uuid));
        if (!profiles.isEmpty()) {
            return profiles.get(0);
        } else {
            return null;
        }
    }

    @Override
    public ImmutableList<Profile> findAllByUuid(Iterable<UUID> uuids) {
        return ImmutableList.copyOf(queryByUuid(uuids));
    }

    @Override
    public void findAllByUuid(Iterable<UUID> uuids, Predicate<Profile> consumer) {
        for (Profile profile : queryByUuid(uuids)) {
            consumer.test(profile);
        }
    }

    /**
     * Perform a query for profiles by name without partitioning the queries.
     *
     * @param names an iterable of names
     * @return a list of results
//     * @throws IOException          thrown on I/O error
//     * @throws InterruptedException thrown on interruption
     */
    protected ImmutableList<Profile> queryByName(Iterable<String> names) {
        List<Profile> profiles = new ArrayList<>();

        List<Object> results = new ArrayList<>();

        int retriesLeft = maxRetries;
        long retryDelay = this.retryDelay;

        for (final String name : names) {
            try {
                results.add(HttpUtil.requestAsync(builder -> builder.uri(profilesURL.resolve(name)).build(), HttpResponse.BodyHandlers.ofString())
                        .thenApply(HttpResponse::body)
                        .thenApply(HttpUtil::parseJson)
                        .join());
            } catch (IOException | InterruptedException e) {
                if (retriesLeft == 0) {
                    throw new RuntimeException(e);
                }

                log.log(Level.WARNING, "Failed to query profile service -- retrying...", e);
                try {
                    Thread.sleep(retryDelay);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
            retryDelay *= 2;
            retriesLeft--;
        }

        for (final Object result : results) {
            Profile profile = decodeProfileResult(result);
            if (profile != null) {
                profiles.add(profile);
            }
        }

        return ImmutableList.copyOf(profiles);
    }

    /**
     * Perform a query for profiles by uuid.
     *
     * @param uuids an iterable of uuids
     * @return a list of results
//     * @throws IOException          thrown on I/O error
//     * @throws InterruptedException thrown on interruption
     */
    protected ImmutableList<Profile> queryByUuid(Iterable<UUID> uuids) {
        List<Profile> profiles = new ArrayList<>();

        List<Object> results = new ArrayList<>();

        int retriesLeft = maxRetries;
        long retryDelay = this.retryDelay;

        for (UUID uuid : uuids) {
            try {
                final Object obj = HttpUtil.requestAsync(builder -> builder.uri(profilesURL.resolve(uuid.toString())).build(), HttpResponse.BodyHandlers.ofString())
                        .thenApply(HttpResponse::body)
                        .thenApply(HttpUtil::parseJson)
                        .join();
                results.add(obj);

                Profile profile = decodeProfileResult(obj);
                if (profile != null) {
                    profiles.add(profile);
                }

                break;
            } catch (IOException | InterruptedException e) {
                if (retriesLeft == 0) {
                    throw new RuntimeException(e);
                }

                log.log(Level.WARNING, "Failed to query name history service -- retrying...", e);
                try {
                    Thread.sleep(retryDelay);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }

            retryDelay *= 2;
            retriesLeft--;
        }

        return ImmutableList.copyOf(profiles);
    }

}
