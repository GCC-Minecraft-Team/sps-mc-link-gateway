package gccminecraftteam.spsmclinkgateway;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class Link extends Command {
    static final MediaType mediaType = MediaType.get("application/text; charset=utf-8");
    static OkHttpClient client = new OkHttpClient();

    public Link() {
        super("link");
    }

    public void execute(CommandSender s, String[] args){
        if(!(s instanceof ProxiedPlayer)) return;

        ProxiedPlayer sender = (ProxiedPlayer) s;

        if(args.length < 1){
            sender.sendMessage(new TextComponent("You can get a token by running the !link command in Discord. Usage: /link <token>"));
            return;
        }

        if(args[0].length() != 32){
            sender.sendMessage(new TextComponent("All tokens should be exactly 32 characters long. Try copying the token again."));
        }

        RequestBody body = RequestBody.create(args[0]+sender.getUniqueId().toString(), mediaType);

        Request request = new Request.Builder().url("http://"+SPSGateway.config().getBotHost()+":"+SPSGateway.config().getBotPort()+"/link").post(body).build();
        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String data = response.body().string();

                switch(data){
                    case "S":
                        sender.sendMessage(new TextComponent("Your SPS account has been successfully linked to your Discord."));
                        break;
                    case "E1":
                        sender.sendMessage(new TextComponent("The token is invalid."));
                        break;
                    case "E2":
                        sender.sendMessage(new TextComponent("There was an error updating your account. Please try again with a new token."));
                        break;
                    case "E":
                        sender.sendMessage(new TextComponent("There was an error linking your SPS and Discord account. Please try again with a new token."));
                        break;
                    default:
                        sender.sendMessage(new TextComponent("The server sent an invalid response. Please try again with a new token."));
                        System.out.println("Invalid data sent from bot during linking process: "+data);
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }
        });
    }
}
