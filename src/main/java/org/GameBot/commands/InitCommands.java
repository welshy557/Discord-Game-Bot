package org.GameBot.commands;

import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class InitCommands extends ListenerAdapter {
    List<CommandData> commandData = new ArrayList<>();
    public InitCommands() {
        super();
        // quiz commands
        commandData.add(Commands.slash("quiz", "Play a quiz game").addOption(OptionType.STRING, "questions", "The number of questions"));
        commandData.add(Commands.slash("close-quiz", "Close your current quiz"));

        // guess commands
        commandData.add(Commands.slash("guess", "Play a guessing game").addOption(OptionType.STRING, "guesses", "The number of guesses allowed"));
        commandData.add(Commands.slash("close-guess", "Close your current guess game"));

        // ConnectFour commands
        commandData.add(Commands.slash("connect-four", "Play a game of Connect 4 with a friend").addOption(OptionType.MENTIONABLE, "opponent", "Opponent to play against"));
        commandData.add(Commands.slash("close-connect-four", "Close your current Connect 4 game").addOption(OptionType.MENTIONABLE, "opponent", "Opponent you played against"));

        // Tic Tac Toe commands
        commandData.add(Commands.slash("tic-tac-toe", "Play a game of Tic Tac Toe with a friend").addOption(OptionType.MENTIONABLE, "opponent", "Opponent to play against"));
        commandData.add(Commands.slash("close-tic-tac-toe", "Close your current Tic Tac Toe game").addOption(OptionType.MENTIONABLE, "opponent", "Opponent you played against"));

        // Rock Paper Scissors Command
        commandData.add(Commands.slash("rock-paper-scissors", "Play a game of Rock Paper Scissors with a friend").addOption(OptionType.MENTIONABLE, "opponent", "Opponent to play against"));

        // Help command
        commandData.add(Commands.slash("help", "Link to README"));
    }
    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        // If in dev env
        if (System.getenv().get("TOKEN") == null) {
            System.out.println("DEVELOPMENT");
            // Sending commands to guild
            event.getGuild().updateCommands().addCommands(commandData).queue();
        }
    }

    @Override
    public void onReady(ReadyEvent event) {
        // If in production env
        if (System.getenv().get("TOKEN") != null) {
            System.out.println("PRODUCTION");
            // Sending commands to guild
            event.getJDA().updateCommands().addCommands(commandData).queue();
        }

    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("help")) {
            event.reply("https://github.com/welshy557/Discord-Game-Bot/blob/main/README.md").setEphemeral(true).queue();
        }
    }
}
