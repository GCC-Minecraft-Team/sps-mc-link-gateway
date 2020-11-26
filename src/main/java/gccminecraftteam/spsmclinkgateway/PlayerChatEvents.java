package gccminecraftteam.spsmclinkgateway;

import gccminecraftteam.spsmclinkgateway.database.DatabaseLink;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.ArrayList;
import java.util.List;

public class PlayerChatEvents implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerChat(ChatEvent e) {
        if (e.isCancelled()) return;
        if (!(e.getSender() instanceof ProxiedPlayer)) return;

        ProxiedPlayer sender = (ProxiedPlayer) e.getSender();
        Server senderServer = sender.getServer();

        for (ProxiedPlayer player : SPSGateway.plugin().getProxy().getPlayers()) {
            if (!player.equals(sender) && !sender.getServer().equals(player.getServer())) {
                // TODO: replicate onChat from spigot plugin here.
                //String msg = e.getMessage();

                //player.sendMessage(new TextComponent(msg));
            }
        }

    }
}