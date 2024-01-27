package dev.mizule.parrotid.bukkit;

import org.bukkit.plugin.java.JavaPlugin;
import org.enginehub.squirrelid.cache.MongoCache;
import org.enginehub.squirrelid.resolver.CacheForwardingService;
import org.enginehub.squirrelid.resolver.CraftHeadHttpRepositoryService;

import java.util.UUID;

public class ParrotID extends JavaPlugin {

    @Override
    public void onEnable() {
        var thingy = new CacheForwardingService(CraftHeadHttpRepositoryService.forMinecraft(), new MongoCache(null, "parrotid"));
        System.out.println(thingy.findByUuid(UUID.fromString("ea3ff55d-f4bc-4fab-a6ae-d809d0849f95")));
    }
}
