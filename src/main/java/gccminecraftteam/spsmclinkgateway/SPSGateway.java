package gccminecraftteam.spsmclinkgateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import gccminecraftteam.spsmclinkgateway.database.DatabaseLink;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class SPSGateway extends Plugin {

    public Set<UUID> mutedPlayers;
    private static SPSGateway plugin;
    private static GatewayConfig config;

    public static final String CONFIG_FILE = "gatewayConfig.yml";

    public static OkHttpClient client = new OkHttpClient();
    public static final MediaType textMedia = MediaType.get("application/text; charset=utf-8");

    public SPSGateway() {
        mutedPlayers = new HashSet<>();
        plugin = this;
    }

    @Override
    public void onEnable() {

        // Load config
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        File file = new File("gatewayConfig.yaml");

        // Instantiating a new ObjectMapper as a YAMLFactory
        ObjectMapper om = new ObjectMapper(new YAMLFactory());

        // set default values
        config = new GatewayConfig(
                "mydb_uri",
                "mydb_db",
                "", "",
                "main",
                "limbo",
                "end",
                "2788",
                "localhost"
        );

        try {
            // Mapping the gateway config from the YAML file to the GatewayConfig class
            config = om.readValue(file, GatewayConfig.class);
        } catch (IOException e) {
            e.printStackTrace();
            try {
                om.writeValue(file, config);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

        // Start listen server
        WebInterfaceLink.Listen();

        // Set up database connection (mongodb)
        DatabaseLink.SetupDatabase();

        // Event listeners
        ProxyServer.getInstance().getPluginManager().registerListener(this, new PlayerChatEvents());
        ProxyServer.getInstance().getPluginManager().registerListener(this, new PlayerJoinEvents());

        // Commands
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new Link());

        // Update bot's player count
        ProxyServer.getInstance().getScheduler().schedule(this, new Runnable() {
            @Override
            public void run() {
                RequestBody body = RequestBody.create(Integer.toString(ProxyServer.getInstance().getPlayers().size()), SPSGateway.textMedia);

                Request request = new Request.Builder().url("http://localhost:"+SPSGateway.config().getBotPort()+"/postcount").post(body).build();
                SPSGateway.client.newCall(request).enqueue(new Callback() {

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) {

                    }

                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        }, 0l, 10l, TimeUnit.MINUTES);

        getLogger().info("[SPSMC Gateway] System Initialized");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    /**
     * gets a reference to an instance of this plugin class {@link SPSGateway}
     * @return
     */
    public static SPSGateway plugin() {
        return plugin;
    }

    /**
     * gets a reference to the {@link GatewayConfig} this plugin class has
     * @return
     */
    public static GatewayConfig config() { return config; }

}
