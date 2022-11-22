package org.GameBot.commands.Quiz;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class QuizCommands extends ListenerAdapter {
    private final ArrayList<QuizGame> quizGames = new ArrayList<>();

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String command = event.getName();
        if (command.equals("quiz")) {
            if (quizGames.contains(new QuizGame(event.getUser().getId()))) {
                event.reply("You already have a quiz in progress!").setEphemeral(true).queue();
            } else {
                List<Category> quizCategories = event.getGuild().getCategoriesByName("Quiz Games", true);
                if (quizCategories.size() == 0) {
                    event.getGuild().createCategory("Quiz Games").queue(c -> createQuiz(event, c));
                } else {
                    createQuiz(event, quizCategories.get(0));
                }
            }
        }

        if (command.equals("close-quiz")) {
            int index = this.quizGames.indexOf(new QuizGame(event.getUser().getId()));
            if (index == -1) {
                event.reply("You don't have an on going quiz").setEphemeral(true).queue();
            } else {
                QuizGame quizGame = this.quizGames.get(index);
                if (this.quizGames.size() == 1) {
                    quizGame.getQuizChannel().getParentCategory().delete().queue();
                    quizGame.getQuizChannel().delete().queue();
                } else {
                    quizGame.getQuizChannel().delete().queue();
                }
                quizGame.getQuizChannel().delete().queue();
                this.quizGames.remove(quizGame);
                event.getJDA().removeEventListener(quizGame);
                event.reply("Quiz Completed!").setEphemeral(true).queue();
            }

        }
    }

    private void createQuiz(@NotNull SlashCommandInteractionEvent event, Category category) {
        String channelName = event.getUser().getName() + "s-quiz";

        event.getGuild().createTextChannel(channelName)
                .addPermissionOverride(event.getMember(), EnumSet.of(Permission.VIEW_CHANNEL), null)
                .addPermissionOverride(event.getGuild().getBotRole(), EnumSet.of(Permission.VIEW_CHANNEL), null)
                .addPermissionOverride(event.getGuild().getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL))
                .setParent(category)
                .queue(channel -> {
                    QuizGame quizGame;
                    if (event.getOption("questions") == null) {
                        quizGame = new QuizGame(event.getUser().getId(), channel, 5);
                    } else {
                        int numberOfQuestions = event.getOption("questions").getAsInt();
                        quizGame = new QuizGame(event.getUser().getId(), channel, numberOfQuestions);
                    }

                    channel.getJDA().addEventListener(quizGame);
                    quizGames.add(quizGame);
                    event.reply("Started Quiz. Play here: <#" + channel.getId() + ">").setEphemeral(true).queue();
                });
    }
}
