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
package org.enginehub.squirrelid;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A pairing of a user's UUID and his or her username.
 *
 * <p>Two profile objects are equal if they have the same UUID.</p>
 */
public record Profile(UUID uniqueId, String name, long timeCached) {

    /**
     * Create a new instance.
     *
     * @param uniqueId the user's UUID
     * @param name     the user's username
     */
    public Profile {
        checkNotNull(uniqueId);
        checkNotNull(name);
    }

    /**
     * Get the user's UUID.
     *
     * @return the user's UUID
     */
    @Override
    public UUID uniqueId() {
        return uniqueId;
    }

    /**
     * Create a copy of this profile but with a new UUID.
     *
     * @param uniqueId the new UUID
     * @return a new profile
     */
    public Profile setUniqueId(UUID uniqueId) {
        return new Profile(uniqueId, name, System.currentTimeMillis());
    }

    /**
     * Get the user's name.
     *
     * @return the user's name
     */
    @Override
    public String name() {
        return name;
    }

    /**
     * Create a copy of this profile but with a new name.
     *
     * @param name the new name
     * @return a new profile
     */
    public Profile setName(String name) {
        return new Profile(uniqueId, name, System.currentTimeMillis());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Profile profile = (Profile) o;

        return uniqueId.equals(profile.uniqueId);
    }

    @Override
    public int hashCode() {
        return uniqueId.hashCode();
    }

    @Override
    public String toString() {
        return "Profile{"
                + "uniqueId=" + uniqueId
                + ", name='" + name + '\''
                + '}';
    }
}
