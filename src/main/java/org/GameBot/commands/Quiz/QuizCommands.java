package org.GameBot.commands.Quiz;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class QuizCommands extends ListenerAdapter {
    private final ArrayList<Quiz> quizGames = new ArrayList<>();


    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        List<CommandData> commandData = new ArrayList<>();

        // Commands
        commandData.add(Commands.slash("quiz", "Play a quiz game").addOption(OptionType.STRING, "questions", "The number of questions"));
        commandData.add(Commands.slash("close-quiz", "Close your current quiz"));
        // Sending commands to guild
        event.getGuild().updateCommands().addCommands(commandData).queue();
    }


    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String command = event.getName();
        if (command.equals("quiz")) {
            if (quizGames.contains(new Quiz(event.getUser().getId()))) {
                event.reply("You already have a quiz in progress!").setEphemeral(true).queue();
            } else {
                String channelName = event.getUser().getName() + "s-quiz";
                event.getGuild().createTextChannel(channelName)
                        .addPermissionOverride(event.getMember(), EnumSet.of(Permission.VIEW_CHANNEL), null)
                        .addPermissionOverride(event.getGuild().getBotRole(), EnumSet.of(Permission.VIEW_CHANNEL), null)
                        .addPermissionOverride(event.getGuild().getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL))
                        .queue(channel -> {
                            Quiz quiz;
                            if (event.getOption("questions") == null) {
                                quiz = new Quiz(event.getUser().getId(), channel, 5);
                            } else {
                                int numberOfQuestions = event.getOption("questions").getAsInt();
                                quiz = new Quiz(event.getUser().getId(), channel, numberOfQuestions);
                            }

                            channel.getJDA().addEventListener(quiz);
                            quizGames.add(quiz);
                            event.reply("Started Quiz. Play here: <#" + channel.getId() + ">").setEphemeral(true).queue();
                        });


            }
        }

        if (command.equals("close-quiz")) {
            int index = this.quizGames.indexOf(new Quiz(event.getUser().getId()));
            if (index == -1) {
                event.reply("You don't have an on going quiz").setEphemeral(true).queue();
            } else {
                Quiz quiz = this.quizGames.get(index);
                quiz.getQuizChannel().delete().queue();
                this.quizGames.remove(quiz);
                event.getJDA().removeEventListener(quiz);
                event.reply("Quiz Completed!").setEphemeral(true).queue();
            }

        }
    }
}
