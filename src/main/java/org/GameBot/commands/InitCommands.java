package org.GameBot.commands;

import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class InitCommands extends ListenerAdapter {
    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        List<CommandData> commandData = new ArrayList<>();

        // quiz commands
        commandData.add(Commands.slash("quiz", "Play a quiz game").addOption(OptionType.STRING, "questions", "The number of questions"));
        commandData.add(Commands.slash("close-quiz", "Close your current quiz"));

        // guess commands
        commandData.add(Commands.slash("guess", "Play a guessing game").addOption(OptionType.STRING, "guesses", "The number of guesses allowed"));
        commandData.add(Commands.slash("close-guess", "Close your current guess game"));

        // ConnectFour commands
        commandData.add(Commands.slash("connect-four", "Play a game of Connect 4 with a friend").addOption(OptionType.MENTIONABLE, "opponent", "Opponent to play against"));
        commandData.add(Commands.slash("close-connect-four", "Close your current guess game").addOption(OptionType.MENTIONABLE, "opponent", "Opponent you played against"));
        // Sending commands to guild
        event.getGuild().updateCommands().addCommands(commandData).queue();
    }
}
