package org.GameBot;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.GameBot.commands.ConnectFour.ConnectFourCommands;
import org.GameBot.commands.Guess.GuessCommands;
import org.GameBot.commands.InitCommands;
import org.GameBot.commands.Quiz.QuizCommands;
import org.GameBot.commands.RockPaperScissors.RockPaperScissorCommands;
import org.GameBot.commands.TicTacToe.TicTacToeCommands;

import javax.security.auth.login.LoginException;
import java.util.Timer;
import java.util.TimerTask;

public class GameBot {
    public GameBot() throws LoginException {
        Dotenv config = Dotenv.configure().ignoreIfMissing().load();

        String token;
        if (config == null) {
            token = System.getenv().get("TOKEN");
        } else {
            token = config.get("TOKEN");
        }
        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(token);
        builder.setStatus(OnlineStatus.ONLINE);
        builder.setActivity(Activity.playing("Playing Games!"));
        builder.enableIntents(GatewayIntent.MESSAGE_CONTENT);
        builder.build();
        ShardManager shardManager = builder.build();

        // Commands
        shardManager.addEventListener(
                new InitCommands(),
                new QuizCommands(),
                new GuessCommands(),
                new ConnectFourCommands(),
                new TicTacToeCommands(),
                new RockPaperScissorCommands()
        );

        // Update status to the number of servers the bot is in every minute
        int prevGuildCount = shardManager.getGuilds().size();
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                int guildCount = shardManager.getGuilds().size();
                if (guildCount != prevGuildCount) {
                    shardManager.setActivity(Activity.playing(String.format("Playing Games in %d Servers!", guildCount)));
                }
            }
        }, 1000 * 60, 1000 * 60);

    }
}
