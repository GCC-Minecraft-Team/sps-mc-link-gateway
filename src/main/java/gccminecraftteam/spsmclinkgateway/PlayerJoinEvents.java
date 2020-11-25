package gccminecraftteam.spsmclinkgateway;

import gccminecraftteam.spsmclinkgateway.database.DatabaseLink;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PlayerJoinEvents implements Listener {

    /**
     * Handles players logging in who aren't registered
     * @param player
     */
    public static void joinUnregistered(ProxiedPlayer player) {

        // send a message to other players on the server
        ProxyServer.getInstance().broadcast(new TextComponent(ChatColor.GOLD + "A new player is joining the server!"));

        // Create a token for the player
        String jwt = WebInterfaceLink.CreateJWT(player.getUniqueId().toString(), "SPS MC", "Register Token", 1000000);

        TextComponent message = new TextComponent(ChatColor.BOLD + "" + ChatColor.AQUA + ">> CLICK HERE <<");
        message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, SPSGateway.config().getWebAppURL() + "/register?token=" + jwt));

        player.sendMessage(new TextComponent(ChatColor.BOLD.toString() + ChatColor.GOLD.toString() + "Connect to your SPS profile to play!"));
        player.sendMessage(message);

        // send a welcome message to the player and ask them to register.
        TextComponent title = new TextComponent("Welcome to " + ChatColor.AQUA +" SPS MC" + ChatColor.WHITE + "!");
        TextComponent subtitle = new TextComponent(ChatColor.BOLD + "Please click the link in chat to register!");
        Title registerTitle = ProxyServer.getInstance().createTitle();
        registerTitle.title(title).subTitle(subtitle).fadeIn(20).fadeOut(20).stay(800);
        player.sendTitle(registerTitle);
    }

    /**
     * Handles logging the player into the main server (world)
     * @param player the player to connect
     */
    public static void joinRegistered(ProxiedPlayer player) {
        if (DatabaseLink.isBanned(player.getUniqueId())) {
            player.disconnect(new TextComponent("The SPS account you linked has been banned!"));
        }

        String spsName = DatabaseLink.getSPSName(player.getUniqueId());
        player.sendMessage(new TextComponent(ChatColor.AQUA + "Welcome to SPSMC, " + spsName + "! Type" + ChatColor.WHITE + " /help " + ChatColor.AQUA + " for a list of commands."));
    }

    @EventHandler
    public void onServerJoin(ServerConnectEvent event) {
        ProxiedPlayer player = event.getPlayer();

        if (DatabaseLink.isRegistered(player.getUniqueId())) {
            joinRegistered(player);
            if (event.getTarget().getName().equals("limbo")) {
                event.setTarget(ProxyServer.getInstance().getServerInfo(SPSGateway.config().getMainServer()));
            } else if (player.getServer() != null && player.getServer().getInfo().getName().equals("limbo")) {
                event.setTarget(ProxyServer.getInstance().getServerInfo(SPSGateway.config().getMainServer()));
            }
        } else {
            joinUnregistered(player);
            event.setTarget(ProxyServer.getInstance().getServerInfo(SPSGateway.config().getLimboServer()));
        }
    }

}
