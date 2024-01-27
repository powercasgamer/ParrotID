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

import com.google.common.collect.ImmutableList;
import org.enginehub.squirrelid.Profile;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * An abstract implementation.
 */
abstract class AbstractProfileCache implements ProfileCache {

    @Override
    public void put(@NonNull Profile profile) {
        putAll(ImmutableList.of(profile));
    }

    @Override
    public @Nullable Profile getIfPresent(UUID uuid) {
        return getAllPresent(ImmutableList.of(uuid)).get(uuid);
    }

    @Override
    public @Nullable Profile getIfPresent(String name) {
        return allPresent(ImmutableList.of(name)).get(name);
    }

}
