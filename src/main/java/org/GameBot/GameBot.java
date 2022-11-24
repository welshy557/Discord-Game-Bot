package org.GameBot;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.GameBot.commands.ConnectFour.ConnectFourCommands;
import org.GameBot.commands.ConnectFour.ConnectFourGame;
import org.GameBot.commands.Guess.GuessCommands;
import org.GameBot.commands.InitCommands;
import org.GameBot.commands.Quiz.QuizCommands;
import org.GameBot.commands.TicTacToe.TicTacToeCommands;

import javax.security.auth.login.LoginException;

public class GameBot {
    private final ShardManager shardManager;
    private final Dotenv config;

    public GameBot() throws LoginException {
        config = Dotenv.configure().ignoreIfMissing().load();

        String token;
        if (config == null) {
            token = System.getenv().get("TOKEN");
        } else {
            token = config.get("TOKEN");
        }
        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(token);
        builder.setStatus(OnlineStatus.ONLINE);
        builder.setActivity(Activity.playing("Games!"));
        builder.enableIntents(GatewayIntent.MESSAGE_CONTENT);
        builder.build();
        shardManager = builder.build();

        // Commands
        shardManager.addEventListener(
                new InitCommands(),
                new QuizCommands(),
                new GuessCommands(),
                new ConnectFourCommands(),
                new TicTacToeCommands()
        );

    }

    public ShardManager getShardManager() {
        return shardManager;
    }
}
