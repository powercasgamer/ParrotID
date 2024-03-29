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

import com.google.common.collect.Lists;
import org.enginehub.squirrelid.Profile;
import org.enginehub.squirrelid.util.ExtraMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;

public class SQLiteCacheTest {


    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testDatabase() throws Exception {
        UUID testId1 = UUID.randomUUID();
        UUID testId2 = UUID.randomUUID();
        UUID testId3 = UUID.randomUUID();
        UUID testId4 = UUID.randomUUID();
        UUID testId5 = UUID.randomUUID();

        File file = folder.newFile();

        final long time = System.currentTimeMillis();

        SQLiteCache cache = new SQLiteCache(file);

        MatcherAssert.assertThat(
                cache.getAllPresent(Arrays.asList(testId1, testId2, testId3)),
                ExtraMatchers.hasSize(0));

        cache.putAll(Arrays.asList(
                new Profile(testId1, "test1", time),
                new Profile(testId2, "test2", time)));

        assertThat(
                cache.getAllPresent(Lists.newArrayList(testId1)),
                allOf(
                        ExtraMatchers.<UUID, Profile>hasSize(1),
                        hasEntry(testId1, new Profile(testId1, "test1", time))));

        assertThat(
                cache.getAllPresent(Arrays.asList(testId1, testId2, testId3)),
                allOf(
                        ExtraMatchers.<UUID, Profile>hasSize(2),
                        hasEntry(testId1, new Profile(testId1, "test1", time)),
                        hasEntry(testId2, new Profile(testId2, "test2", time))));

        cache.put(new Profile(testId1, "test1_2", time));

        assertThat(
                cache.getAllPresent(Arrays.asList(testId1, testId2, testId3)),
                allOf(
                        ExtraMatchers.<UUID, Profile>hasSize(2),
                        hasEntry(testId1, new Profile(testId1, "test1_2", time)),
                        hasEntry(testId2, new Profile(testId2, "test2", time))));

        cache.put(new Profile(testId3, "test3", time));

        assertThat(
                cache.getAllPresent(Arrays.asList(testId1, testId2, testId3)),
                allOf(
                        ExtraMatchers.<UUID, Profile>hasSize(3),
                        hasEntry(testId1, new Profile(testId1, "test1_2", time)),
                        hasEntry(testId2, new Profile(testId2, "test2", time)),
                        hasEntry(testId3, new Profile(testId3, "test3", time))));

        assertThat(
                cache.getIfPresent(testId1),
                equalTo(new Profile(testId1, "test1_2", time)));

        assertThat(
                cache.getIfPresent(testId3),
                equalTo(new Profile(testId3, "test3", time)));

        assertThat(
                cache.getAllPresent(Arrays.asList(testId1, testId3)),
                allOf(
                        ExtraMatchers.<UUID, Profile>hasSize(2),
                        hasEntry(testId1, new Profile(testId1, "test1_2", time)),
                        hasEntry(testId3, new Profile(testId3, "test3", time))));

        assertThat(
                cache.getIfPresent(UUID.randomUUID()),
                equalTo(null));

        cache = new SQLiteCache(file);

        assertThat(
                cache.getAllPresent(Arrays.asList(testId1, testId2, testId3)),
                allOf(
                        ExtraMatchers.<UUID, Profile>hasSize(3),
                        hasEntry(testId1, new Profile(testId1, "test1_2", time)),
                        hasEntry(testId2, new Profile(testId2, "test2", time)),
                        hasEntry(testId3, new Profile(testId3, "test3", time))));

        assertThat(
                cache.getAllPresent(Arrays.asList(testId1, testId3)),
                allOf(
                        ExtraMatchers.<UUID, Profile>hasSize(2),
                        hasEntry(testId1, new Profile(testId1, "test1_2", time)),
                        hasEntry(testId3, new Profile(testId3, "test3", time))));

        cache.putAll(Lists.newArrayList(new Profile(testId2, "test2_2", time)));

        assertThat(
                cache.getAllPresent(Arrays.asList(testId1, testId2, testId3)),
                allOf(
                        ExtraMatchers.<UUID, Profile>hasSize(3),
                        hasEntry(testId1, new Profile(testId1, "test1_2", time)),
                        hasEntry(testId2, new Profile(testId2, "test2_2", time)),
                        hasEntry(testId3, new Profile(testId3, "test3", time))));

        cache.putAll(Arrays.asList(
                new Profile(testId2, "test2_2", time),
                new Profile(testId4, "test4", time)));

        assertThat(
                cache.getAllPresent(Arrays.asList(testId1, testId2, testId3, testId4)),
                allOf(
                        ExtraMatchers.<UUID, Profile>hasSize(4),
                        hasEntry(testId1, new Profile(testId1, "test1_2", time)),
                        hasEntry(testId2, new Profile(testId2, "test2_2", time)),
                        hasEntry(testId3, new Profile(testId3, "test3", time)),
                        hasEntry(testId4, new Profile(testId4, "test4", time))));

        assertThat(
                cache.getAllPresent(Arrays.asList(testId1, testId2, testId3, testId4, testId5)),
                allOf(
                        ExtraMatchers.<UUID, Profile>hasSize(4),
                        hasEntry(testId1, new Profile(testId1, "test1_2", time)),
                        hasEntry(testId2, new Profile(testId2, "test2_2", time)),
                        hasEntry(testId3, new Profile(testId3, "test3", time)),
                        hasEntry(testId4, new Profile(testId4, "test4", time))));

        assertThat(
                cache.getAllPresent(Arrays.asList(testId2, testId4, testId5)),
                allOf(
                        ExtraMatchers.<UUID, Profile>hasSize(2),
                        hasEntry(testId2, new Profile(testId2, "test2_2", time)),
                        hasEntry(testId4, new Profile(testId4, "test4", time))));

        assertThat(
                cache.getIfPresent(UUID.randomUUID()),
                equalTo(null));
    }

}
