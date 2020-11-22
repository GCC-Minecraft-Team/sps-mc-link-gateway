package gccminecraftteam.spsmclinkgateway;

import gccminecraftteam.spsmclinkgateway.database.DatabaseLink;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class SPSGateway extends Plugin {

    public Set<UUID> mutedPlayers;
    private static SPSGateway plugin;

    public SPSGateway() {
        mutedPlayers = new HashSet<>();
        plugin = this;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        DatabaseLink.SetupDatabase();
        ProxyServer.getInstance().getPluginManager().registerListener(this, new PlayerJoinEvents());

        getLogger().info("[SPSMC Gateway] System Initialized");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static SPSGateway plugin() {
        return plugin;
    }

}
