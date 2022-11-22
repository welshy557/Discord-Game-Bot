package org.GameBot;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.GameBot.commands.QuizCommands;

import javax.security.auth.login.LoginException;

public class GameBot {
    private final ShardManager shardManager;
    private final Dotenv config;

    public GameBot() throws LoginException {
        config = Dotenv.configure().ignoreIfMissing().load();

        String token;
        if (config == null) {
            token = System.getenv("TOKEN");
        } else {
            token = config.get("TOKEN");
        }
        DefaultShardManagerBuilder builder = net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder.createDefault(token);
        builder.setStatus(OnlineStatus.ONLINE);
        builder.setActivity(Activity.playing("Games!"));
        builder.build();
        shardManager = builder.build();

        // Commands
        shardManager.addEventListener(new QuizCommands());
    }

    public ShardManager getShardManager() {
        return shardManager;
    }
}
