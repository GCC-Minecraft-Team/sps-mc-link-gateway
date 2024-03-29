package gccminecraftteam.spsmclinkgateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gccminecraftteam.spsmclinkgateway.database.DatabaseLink;
import io.javalin.Javalin;
import io.jsonwebtoken.*;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;

public class WebInterfaceLink {

    private static final SecureRandom secureRandom = new SecureRandom(); //threadsafe
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder(); //threadsafe

    /**
     * Starts server.
     */
    public static void Listen() {
        Javalin app = null;

        /*
         * Below is an extremely stupid fix for Javalin. You have to do this whenever you're using it with Spigot
         */

        // Get the current class loader.
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        // Temporarily set this thread's class loader to the plugin's class loader.
        // Replace JavalinTestPlugin.class with your own plugin's class.
        Thread.currentThread().setContextClassLoader(SPSGateway.class.getClassLoader());

        // Instantiate the web server (which will now load using the plugin's class loader).
        app = Javalin.create().start(8000);
        app.get("/", context -> context.result("Yay Javalin works!"));

        // Put the original class loader back where it was.
        Thread.currentThread().setContextClassLoader(classLoader);
        ProxyServer.getInstance().getLogger().info("Listening for web-app requests on port 8000!");

        // listen for post request from web app
        app.post("/registerPlayer", ctx -> {
            ProxyServer.getInstance().getLogger().info("request " + ctx.body());
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode newUser = objectMapper.readTree(ctx.body());
            ProxyServer.getInstance().getLogger().info("request " + newUser.toString());

            UUID newUUID = null;

            // try to decode JWT
            try {
                newUUID = UUID.fromString(DecodeJWT(newUser.get("token").asText()).getId());
            } catch (JwtException exception) {
                ProxyServer.getInstance().getLogger().severe("Something went wrong decoding a JSON web token");
            }

            // send response and load com.github.gcc_minecraft_team.sps_mc_link_spigot.database data
            if (newUUID != null) {
                DatabaseLink.registerPlayer(newUUID, newUser.get("id").asText(), newUser.get("nick").asText(), newUser.get("email") != null ? newUser.get("email").asText() : newUser.get("nick").asText(), newUser.get("name").asText());
                // success
                ctx.status(200);
            } else {
                // internal server error
                ctx.status(500);
            }
        });

        // I really should be using websockets but it'll be a bit more complicated setting up the whole ping system and managing clients
        app.post("/broadcast", ctx -> {
            String message = ctx.body();
            for (ProxiedPlayer player : SPSGateway.plugin().getProxy().getPlayers()) {
                player.sendMessage(new TextComponent(message));
            }

            ctx.status(200);
        });
    }

    public static String CreateJWT(@NotNull String id, @NotNull String issuer, @NotNull String subject, long ttlMillis) {

        //The JWT signature algorithm we will be using to sign the token
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        //We will sign our JWT with our ApiKey secret
        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(SPSGateway.config().getjwtsecret());
        Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

        //Let's set the JWT Claims
        JwtBuilder builder = Jwts.builder().setId(id)
                .setIssuedAt(now)
                .setSubject(subject)
                .setIssuer(issuer)
                .signWith(signatureAlgorithm, signingKey);

        //if it has been specified, let's add the expiration
        if (ttlMillis > 0) {
            long expMillis = nowMillis + ttlMillis;
            Date exp = new Date(expMillis);
            builder.setExpiration(exp);
        }

        //Builds the JWT and serializes it to a compact, URL-safe string
        return builder.compact();
    }

    public static Claims DecodeJWT(@NotNull String jwt) {
        //This line will throw an exception if it is not a signed JWS (as expected)
        return Jwts.parser()
                .setSigningKey(DatatypeConverter.parseBase64Binary(SPSGateway.config().getjwtsecret()))
                .parseClaimsJws(jwt).getBody();
    }

}
