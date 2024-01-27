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
package org.enginehub.squirrelid.cache;

import com.google.common.collect.ImmutableMap;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.UuidRepresentation;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.enginehub.squirrelid.Profile;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Iterator;
import java.util.Objects;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class MongoCache extends AbstractProfileCache {

    private final String databaseName;
    private final MongoCollection<Profile> collection;
    private final MongoDatabase database;
    private final MongoClient client;

    public MongoCache(final @Nullable MongoClient mongoClient, final @NonNull String databaseName) {
        checkNotNull(databaseName, "databaseName cannot be null.");

        this.databaseName = databaseName;

        this.client = Objects.requireNonNullElseGet(mongoClient, () -> MongoClients.create(MongoClientSettings.builder()
                .uuidRepresentation(UuidRepresentation.STANDARD)
                .build()));

        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));

        this.database = this.client.getDatabase(this.databaseName).withCodecRegistry(pojoCodecRegistry);

        this.collection = this.database.getCollection("profiles", Profile.class);
    }

    @Override
    public void putAll(@NonNull Iterable<Profile> profiles) {
        Iterator<Profile> it = profiles.iterator();
        // It was an empty collection
        if (!it.hasNext()) {
            return;
        }

        for (final Profile profile : profiles) {
            this.collection.replaceOne(Filters.eq("uniqueId", profile.uniqueId()), profile, new ReplaceOptions().upsert(true));
        }
    }

    @Override
    public ImmutableMap<UUID, Profile> getAllPresent(Iterable<UUID> ids) {
        Iterator<UUID> it = ids.iterator();
        // It was an empty collection
        if (!it.hasNext()) {
            return ImmutableMap.of();
        }

        final ImmutableMap.Builder<UUID, Profile> mapBuilder = ImmutableMap.builder();

        for (final UUID uuid : ids) {
            final var first = this.collection.find(Filters.eq("uniqueId", uuid)).first();
            if (first != null) {
                mapBuilder.put(first.uniqueId(), first);
            }
        }

        return mapBuilder.build();
    }

    @Override
    public ImmutableMap<String, Profile> allPresent(Iterable<String> names) {
        Iterator<String> it = names.iterator();
        // It was an empty collection
        if (!it.hasNext()) {
            return ImmutableMap.of();
        }

        final var foundings = this.collection.find(Filters.eq("name", it));

        final ImmutableMap.Builder<String, Profile> mapBuilder = ImmutableMap.builder();
        for (var found : foundings) {
            mapBuilder.put(found.name(), found);
        }

        return mapBuilder.build();
    }
}
