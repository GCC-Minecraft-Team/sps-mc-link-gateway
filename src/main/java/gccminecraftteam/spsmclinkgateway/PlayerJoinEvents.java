package gccminecraftteam.spsmclinkgateway;

import gccminecraftteam.spsmclinkgateway.database.DatabaseLink;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PlayerJoinEvents implements Listener {

    /**
     * Fired when the player logs into the proxy
     * @param event
     */
    @EventHandler
    public void postPlayerLogin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();

        if (!DatabaseLink.isRegistered(player.getUniqueId())) {
            // send a welcome message to the player and ask them to register.
            TextComponent title = new TextComponent("Welcome to SPS MC!");
            TextComponent subtitle = new TextComponent("Please click the link in chat to register!");
            Title registerTitle = ProxyServer.getInstance().createTitle();
            registerTitle.title(title).subTitle(subtitle).fadeIn(20).fadeOut(20).stay(800);
            player.sendTitle(registerTitle);
        } else {
            String spsName = DatabaseLink.getSPSName(player.getUniqueId());
            player.sendMessage(new TextComponent(ChatColor.AQUA + "Welcome to SPSMC, " + spsName + "!"));
        }
    }

}
