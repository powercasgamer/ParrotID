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

import org.junit.Test;

import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

public class ProfileTest {

    final long time = System.currentTimeMillis();

    @Test
    public void testGetUniqueId() throws Exception {
        UUID uniqueId = UUID.randomUUID();
        assertThat(new Profile(uniqueId, "test", time).uniqueId(), equalTo(uniqueId));
    }

    @Test
    public void testSetUniqueId() throws Exception {
        UUID uniqueId1 = UUID.randomUUID();
        Profile profile1 = new Profile(uniqueId1, "test", time);
        UUID uniqueId2 = UUID.randomUUID();

        assertThat(profile1.uniqueId(), equalTo(uniqueId1));
        assertThat(profile1.setUniqueId(uniqueId2).uniqueId(), equalTo(uniqueId2));
        assertThat(profile1.uniqueId(), equalTo(uniqueId1));
    }

    @Test
    public void testGetName() throws Exception {
        assertThat(new Profile(UUID.randomUUID(), "test", time).name(), equalTo("test"));
        assertThat(new Profile(UUID.randomUUID(), "test2", time).name(), equalTo("test2"));
    }

    @Test
    public void testSetName() throws Exception {
        String name1 = "test";
        Profile profile1 = new Profile(UUID.randomUUID(), name1, time);
        String name2 = "test2";

        assertThat(profile1.name(), equalTo(name1));
        assertThat(profile1.setName(name2).name(), equalTo(name2));
        assertThat(profile1.name(), equalTo(name1));
    }

    @Test
    public void testEquals() throws Exception {
        UUID uniqueId1 = UUID.randomUUID();
        UUID uniqueId2 = UUID.randomUUID();

        assertThat(new Profile(uniqueId1, "test", time), equalTo(new Profile(uniqueId1, "test", time)));
        assertThat(new Profile(uniqueId1, "test", time), equalTo(new Profile(uniqueId1, "other", time)));
        assertThat(new Profile(uniqueId1, "test", time), not(equalTo(new Profile(uniqueId2, "test", time))));
        assertThat(new Profile(uniqueId1, "test", time), not(equalTo(new Profile(uniqueId2, "other", time))));
    }

    @Test
    public void testHashCode() throws Exception {
        UUID uniqueId1 = UUID.randomUUID();
        UUID uniqueId2 = UUID.randomUUID();

        assertThat(new Profile(uniqueId1, "test", time).hashCode(), equalTo(new Profile(uniqueId1, "test", time).hashCode()));
        assertThat(new Profile(uniqueId1, "test", time).hashCode(), equalTo(new Profile(uniqueId1, "other", time).hashCode()));
        assertThat(new Profile(uniqueId1, "test", time).hashCode(), not(equalTo(new Profile(uniqueId2, "test", time).hashCode())));
        assertThat(new Profile(uniqueId1, "test", time).hashCode(), not(equalTo(new Profile(uniqueId2, "other", time).hashCode())));
    }
}
