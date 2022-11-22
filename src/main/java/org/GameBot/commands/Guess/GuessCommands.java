package org.GameBot.commands.Guess;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.GameBot.commands.Quiz.QuizGame;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class GuessCommands extends ListenerAdapter {
    ArrayList<GuessGame> guessGames = new ArrayList<>();

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String command = event.getName();
        if (command.equals("guess")) {
            if (guessGames.contains(new GuessGame(event.getUser().getId()))) {
                event.reply("You already have a guessing game in progress!").setEphemeral(true).queue();
            } else {
                List<Category> quizCategories = event.getGuild().getCategoriesByName("Guess Games", true);
                if (quizCategories.size() == 0) {
                    event.getGuild().createCategory("Guess Games").queue(c -> createGuessGame(event, c));
                } else {
                    createGuessGame(event, quizCategories.get(0));
                }
            }
        }

        if (command.equals("close-guess")) {
            int index = this.guessGames.indexOf(new GuessGame(event.getUser().getId()));
            if (index == -1) {
                event.reply("You don't have an on going Guess Game").setEphemeral(true).queue();
            } else {
                GuessGame guessGame = this.guessGames.get(index);
                if (this.guessGames.size() == 1) {
                    guessGame.getGameChannel().getParentCategory().delete().queue();
                    guessGame.getGameChannel().delete().queue();
                } else {
                    guessGame.getGameChannel().delete().queue();
                }
                this.guessGames.remove(guessGame);
                event.getJDA().removeEventListener(guessGame);
                event.reply("Guess Game Completed!").setEphemeral(true).queue();
            }

        }
    }

    private void createGuessGame(@NotNull SlashCommandInteractionEvent event, Category category) {
        String channelName = event.getUser().getName() + "s-guessing-game";

        event.getGuild().createTextChannel(channelName)
                .addPermissionOverride(event.getMember(), EnumSet.of(Permission.VIEW_CHANNEL), null)
                .addPermissionOverride(event.getGuild().getBotRole(), EnumSet.of(Permission.VIEW_CHANNEL), null)
                .addPermissionOverride(event.getGuild().getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL))
                .setParent(category)
                .queue(channel -> {
                    GuessGame guessGame;
                    if (event.getOption("guesses") == null) {
                        guessGame = new GuessGame(event.getUser().getId(), channel, 5);
                    } else {
                        int numberOfGuesses = event.getOption("guesses").getAsInt();
                        guessGame = new GuessGame(event.getUser().getId(), channel, numberOfGuesses);
                    }

                    channel.getJDA().addEventListener(guessGame);
                    this.guessGames.add(guessGame);
                    event.reply("Started Guess Game. Play here: <#" + channel.getId() + ">").setEphemeral(true).queue();
                });
    }
}
