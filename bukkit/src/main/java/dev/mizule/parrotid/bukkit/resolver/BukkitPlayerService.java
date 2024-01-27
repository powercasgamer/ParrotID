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

package dev.mizule.parrotid.bukkit.resolver;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.enginehub.squirrelid.Profile;
import org.enginehub.squirrelid.resolver.SingleRequestService;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Checks the list of online players in Bukkit to find UUIDs.
 */
public final class BukkitPlayerService extends SingleRequestService {

    private static final BukkitPlayerService INSTANCE = new BukkitPlayerService();

    private BukkitPlayerService() {
    }

    public static BukkitPlayerService instance() {
        return INSTANCE;
    }

    @Override
    public int getIdealRequestLimit() {
        return Integer.MAX_VALUE;
    }

    @Nullable
    @Override
    public Profile findByName(String name) {
        Player player = Bukkit.getServer().getPlayerExact(name);
        if (player != null) {
            return new Profile(player.getUniqueId(), player.getName(), System.currentTimeMillis());
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public Profile findByUuid(UUID uuid) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        if (player.getName() != null) {
            return new Profile(player.getUniqueId(), player.getName(), System.currentTimeMillis());
        } else {
            return null;
        }
    }

}
