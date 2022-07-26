package me.lara.bungeeskywarsffa.utils;

import me.lara.bungeeskywarsffa.BungeeSkywarsFFA;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.Configuration;

import java.util.Objects;

public class LocationUtils {

    public static Location spawnLocation() {
        final Configuration config = BungeeSkywarsFFA.getInstance().getConfig();

        if(!config.isSet("Spawn.world")) {
            return null;
        }
        return new Location(Bukkit.getWorld(Objects.requireNonNull(config.getString("Spawn.world"))), config.getDouble("Spawn.X"), config.getDouble("Spawn.Y"), config.getDouble("Spawn.Z"), (float) config.getDouble("Spawn.YAW"), (float) config.getDouble("Spawn.PITCH"));
    }

    public static double buildHeight() {
        return BungeeSkywarsFFA.getInstance().getConfig().getDouble("Build-height.Y");
    }

}
