package gccminecraftteam.spsmclinkgateway.database;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.mongodb.*;
import com.mongodb.client.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.model.UpdateOptions;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import gccminecraftteam.spsmclinkgateway.GatewayConfig;
import gccminecraftteam.spsmclinkgateway.SPSGateway;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class DatabaseLink {

    public static final String CONFIG_FILE = "gatewayConfig.yml";

    // mongo variables
    private static MongoClient mongoClient;
    private static MongoDatabase mongoDatabase;

    public static MongoCollection<Document> userCol;

    /**
     * Creates a connection to the MongoDB database.
     */
    public static void SetupDatabase() {
        // Loading the YAML file
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        File file = new File("gatewayConfig.yaml");

        // Instantiating a new ObjectMapper as a YAMLFactory
        ObjectMapper om = new ObjectMapper(new YAMLFactory());

        // set default values
        GatewayConfig config = new GatewayConfig("mydb_uri", "mydb_db");

        try {
            // Mapping the gateway config from the YAML file to the GatewayConfig class
            config = om.readValue(file, GatewayConfig.class);
        } catch (IOException e) {
            try {
                om.writeValue(new File("gatewayConfig.yaml"), config);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

        // set up info for database
        ConnectionString connectionString = new ConnectionString((String) config.getDbURI());
        String dbName = (String) config.getDbDB();

        CodecRegistry pojoCodecRegistry = fromProviders(PojoCodecProvider.builder().automatic(true).build());
        CodecRegistry codecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoCodecRegistry);


        // set client settings
        MongoClientSettings clientSettings = MongoClientSettings.builder().applyConnectionString(connectionString)
                .codecRegistry(codecRegistry).build();

        // set database and connect
        try {
            mongoClient = MongoClients.create(clientSettings);
            mongoDatabase = mongoClient.getDatabase(dbName);
            userCol = mongoDatabase.getCollection("users");
        } catch(MongoException exception) {
            ProxyServer.getInstance().getLogger().severe("Something went wrong connecting to the MongoDB database, is the config file set up correctly?");
        }
    }

    /**
     * Gets whether a player is registered on the database.
     * @param uuid The {@link UUID} of the player to check.
     * @return {@code true} if the player is registered.
     */
    public static boolean isRegistered(@NotNull UUID uuid) {
        try {
            // check if player is registered
            return userCol.countDocuments(new Document("mcUUID", uuid.toString())) == 1;
        } catch(MongoException exception) {
            ProxyServer.getInstance().getLogger().severe("Couldn't check user from database! Error: " + exception.toString());
            return false;
        }
    }

    /**
     * Gets the Minecraft names of all SPS users registered.
     * @return A {@link List} of all the names.
     */
    @NotNull
    public static List<String> getAllSPSNames() {
        List<String> spsNames = new ArrayList<>();
        for(Document doc : userCol.find()) {
            spsNames.add(doc.getString("mcName"));
        }
        return spsNames;
    }

    /**
     * Gets the {@link UUID}s of all SPS users registered.
     * @return A {@link List} of all players' {@link UUID}s.
     */
    @NotNull
    public static List<UUID> getAllSPSPlayers() {
        List<UUID> spsPlayers = new ArrayList<>();
        for (Document doc : userCol.find()) {
            spsPlayers.add(UUID.fromString(doc.getString("mcUUID")));
        }
        return spsPlayers;
    }

    /**
     * Gets the SPS name for a Minecraft player.
     * @param uuid The {@link UUID} of the Minecraft player.
     * @return The SPS name of the player.
     */
    @NotNull
    public static String getSPSName(@NotNull UUID uuid) {
        if (isRegistered(uuid)) {
            return userCol.find(new Document("mcUUID", uuid.toString())).first().getString("mcName");
        } else {
            return "Unregistered User";
        }
    }

    /**
     * Gets the school a player goes to.
     * @param uuid The {@link UUID} of the player to check.
     * @return The three-character tag of the school, or an empty {@link String} if not found.
     */
    public static String getSchoolTag(@NotNull UUID uuid) {
        if (isRegistered(uuid)) {
            String fullName = userCol.find(new Document("mcUUID", uuid.toString())).first().getString("oAuthName");
            if (fullName != null) {
                return fullName.split(" ")[4].trim();
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    /**
     * Gets the grade level of a player.
     * @param uuid The {@link UUID} of the player to check.
     * @return The one or two-character grade level, or an empty {@link String} if not found.
     */
    public static String getGradeTag(@NotNull UUID uuid) {
        if (isRegistered(uuid)) {
            String fullName = userCol.find(new Document("mcUUID", uuid.toString())).first().getString("oAuthName");
            if (fullName != null) {
                return fullName.split(" ")[5].trim();
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    /**
     * Gets the {@link UUID} of the Minecraft player from their SPS username.
     * @param SPSName The SPS username to check.
     * @return The {@link UUID} of the Minecraft player if they are linked, otherwise {@code null}.
     */
    @Nullable
    public static UUID getSPSUUID(@NotNull String SPSName) {
        Document result = userCol.find(new Document("mcName", SPSName)).first();
        if (result != null)
            return UUID.fromString(result.getString("mcUUID"));
        else
            return null;
    }

    /**
     * Checks if a player is banned.
     * @param uuid The Minecraft {@link UUID} of the player to check.
     * @return {@code true} if the player is banned.
     */
    public static boolean isBanned(@NotNull UUID uuid) {
        try {
            return userCol.find(new Document("mcUUID", uuid.toString())).first().getBoolean("banned");
        } catch (Exception exception) {
            return false;
        }
    }

    /**
     * Bans a player using their SPS ID.
     * @param SPSUser The SPS username to ban without domain (e.g. 1absmith).
     * @return {@code true} if the SPS ID was successfully banned.
     */
    public static boolean banPlayer(@NotNull String SPSUser) {
        String spsEmail = SPSUser + "@seattleschools.org";

        ProxyServer.getInstance().getLogger().info("Banning player with SPS email: " + spsEmail);

        BasicDBObject updateFields = new BasicDBObject();
        updateFields.append("banned", true);
        // set query
        BasicDBObject setQuery = new BasicDBObject();
        setQuery.append("$set", updateFields);

        // Ban the player in the database
        try {
            userCol.updateOne(new Document("oAuthEmail", spsEmail), setQuery);

            // kick them from the server
            ProxiedPlayer oplayer = ProxyServer.getInstance().getPlayer(UUID.fromString(userCol.find(new Document("oAuthEmail", spsEmail)).first().getString("mcUUID")));
            if (oplayer.isConnected()) {
                oplayer.disconnect(new TextComponent("Wooks wike uwu've bewn banned! UwU"));
            }

            return true;
        } catch (MongoException exception) {
            ProxyServer.getInstance().getLogger().severe("Something went wrong banning a player!");
            return false;
        } catch (NullPointerException exception)  {
            ProxyServer.getInstance().getLogger().severe("Something went wrong looking up a user to player!");
            return false;
        }
    }

    /**
     * Checks if a player is muted.
     * @param uuid The Minecraft {@link UUID} of the player to mute.
     * @return {@code true} if the player is muted.
     */
    public static boolean getIsMuted(@NotNull UUID uuid) {
        try {
            return userCol.find(new Document("mcUUID", uuid.toString())).first().getBoolean("muted");
        } catch (Exception exception) {
            return false;
        }
    }

    /**
     * Mutes an SPS user in the database (stops them from talking)
     * @param SPSUser The SPS name of the player to mute.
     * @return {@code true} if no errors occurred.
     */
    public static boolean setMutePlayer(@NotNull String SPSUser, boolean muted) {
        String spsEmail = SPSUser + "@seattleschools.org";

        ProxyServer.getInstance().getLogger().info("Muting/Unmuting player with SPS email: " + spsEmail);

        BasicDBObject updateFields = new BasicDBObject();
        updateFields.append("muted", muted);
        // set query
        BasicDBObject setQuery = new BasicDBObject();
        setQuery.append("$set", updateFields);

        // mute the player in the database
        try {
            userCol.updateOne(new Document("oAuthEmail", spsEmail), setQuery);

            // mute them on the server
            ProxiedPlayer oplayer = ProxyServer.getInstance().getPlayer(UUID.fromString(userCol.find(new Document("oAuthEmail", spsEmail)).first().getString("mcUUID")));
            if (muted) {
                SPSGateway.plugin().mutedPlayers.add(oplayer.getUniqueId());
            } else {
                SPSGateway.plugin().mutedPlayers.remove(oplayer.getUniqueId());
            }

            return true;
        } catch (MongoException exception) {
            ProxyServer.getInstance().getLogger().severe("Something went wrong muting/unmuting a player!");
            return false;
        } catch (NullPointerException exception)  {
            ProxyServer.getInstance().getLogger().severe("Something went wrong looking up a user to player!");
            return false;
        }
    }

    /**
     * Registers a new player in the database.
     * @param uuid The Minecraft {@link UUID} of the player.
     * @param SPSid The SPS ID of the player.
     * @param name The new name of the player.
     */
    public static void registerPlayer(@NotNull UUID uuid, @NotNull String SPSid, @NotNull String name, @NotNull String SPSemail, @NotNull String SPSname) {
        // set UUID and Name
        BasicDBObject updateFields = new BasicDBObject();
        updateFields.append("oAuthId", SPSid);
        updateFields.append("oAuthEmail", SPSemail);
        updateFields.append("oAuthName", SPSname);

        updateFields.append("mcUUID", uuid.toString());
        updateFields.append("mcName", name);
        updateFields.append( "banned", false);
        updateFields.append("muted", false);

        // set query
        BasicDBObject setQuery = new BasicDBObject();
        setQuery.append("$set", updateFields);

        UpdateOptions options = new UpdateOptions().upsert(true);

        // update in the database
        userCol.updateOne(new Document("oAuthId", SPSid), setQuery, options);
        String email = Objects.requireNonNull(userCol.find(new Document("oAuthId", SPSid)).first()).getString("oAuthEmail");

        // get player
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);

        // load permissions
        //SPSSpigot.perms().loadPermissions(player);

        // set nametag
        int maxLength = Math.min(name.length(), 15);

        //NickAPI.setSkin( player, player.getName() );
        //NickAPI.setUniqueId( player, player.getName() );
        player.setDisplayName(name.substring(0, maxLength));
        //NickAPI.nick( player, name.substring(0, maxLength));
        //NickAPI.refreshPlayer( player );

        // send a confirmation message
        player.sendMessage(ChatColor.BOLD.toString() +
                ChatColor.GREEN.toString() + "Successfully linked account " +
                ChatColor.GOLD.toString() + email +
                ChatColor.GREEN.toString() + " to the server! Your new username is: " +
                ChatColor.GOLD.toString() + name);

        if (isBanned(uuid)) {
            player.disconnect(new TextComponent("The SPS account you linked has been banned!"));
        }
        // leave this stuff for the individual servers
        /*
        // give starting boat
        if (player.getInventory().getItemInMainHand().getType() == Material.AIR) {
            SPSSpigot.plugin().giveStartingItems(player);
        }

        ClaimBoard.addBoard(player);

        CompassThread compass = new CompassThread(player, SPSSpigot.getWorldGroup(player.getWorld()));
        SPSSpigot.plugin().compassThreads.put(player.getUniqueId(), compass);
        compass.start();

        player.sendMessage("You've spawned in the lobby, please use the included " + ChatColor.BLUE +"Starting Boat" + ChatColor.WHITE + " to leave the island!");
        */
    }
}
