package org.GameBot.commands.TicTacToe;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class TicTacToeCommands extends ListenerAdapter {
    ArrayList<TicTacToeGame> ticTacToeGames = new ArrayList<>();

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String command = event.getName();
        if (command.equals("tic-tac-toe")) {
            if (event.getOption("opponent") == null) {
                event.reply("Missing Opponent").queue();
                return;
            }

            if (ticTacToeGames.contains(new TicTacToeGame(event.getUser(), event.getOption("opponent").getAsUser()))) {
                event.reply("You already have a game in progress!").setEphemeral(true).queue();
            } else {
                List<Category> ticTacToeCategories = event.getGuild().getCategoriesByName("Tic Tac Toe Games", true);
                if (ticTacToeCategories.size() == 0) {
                    event.getGuild().createCategory("Tic Tac Toe Games").queue(c -> createTicTacToe(event, c));
                } else {
                    createTicTacToe(event, ticTacToeCategories.get(0));
                }
            }
        }

        if (command.equals("close-tic-tac-toe")) {
            if (event.getOption("opponent") == null) {
                event.reply("Missing Opponent").queue();
                return;
            }

            int index = this.ticTacToeGames.indexOf(new TicTacToeGame(event.getUser(), event.getOption("opponent").getAsUser()));
            if (index == -1) {
                event.reply("You don't have an on going game").setEphemeral(true).queue();
            } else {
                TicTacToeGame ticTacToeGame = this.ticTacToeGames.get(index);
                if (this.ticTacToeGames.size() == 1) {
                    ticTacToeGame.getGameChannel().getParentCategory().delete().queue();
                    ticTacToeGame.getGameChannel().delete().queue();
                } else {
                    ticTacToeGame.getGameChannel().delete().queue();
                }
                this.ticTacToeGames.remove(ticTacToeGame);
                event.getJDA().removeEventListener(ticTacToeGame);
                event.reply("Tic Tac Toe Game Completed!").setEphemeral(true).queue();
            }

        }
    }

    private void createTicTacToe(@NotNull SlashCommandInteractionEvent event, Category category) {
        String channelName = event.getUser().getName() + "-vs-" + event.getOption("opponent").getAsUser().getName() + "-tic-tac-toe";

        event.getGuild().createTextChannel(channelName)
                .addPermissionOverride(event.getMember(), EnumSet.of(Permission.VIEW_CHANNEL), null)
                .addPermissionOverride(event.getOption("opponent").getAsMember(), EnumSet.of(Permission.VIEW_CHANNEL), null)
                .addPermissionOverride(event.getGuild().getBotRole(), EnumSet.of(Permission.VIEW_CHANNEL), null)
                .addPermissionOverride(event.getGuild().getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL))
                .setParent(category)
                .queue(channel -> {
                    TicTacToeGame ticTacToeGame;
                    ticTacToeGame = new TicTacToeGame(event.getUser(), event.getOption("opponent").getAsUser(), channel);
                    channel.getJDA().addEventListener(ticTacToeGame);
                    this.ticTacToeGames.add(ticTacToeGame);
                    event.reply("Started Tic Tac Toe Game. Play here: <#" + channel.getId() + ">").setEphemeral(true).queue();
                    event.getOption("opponent").getAsUser().openPrivateChannel()
                            .queue(dm -> dm.sendMessage(String.format("<@%d> has invited you to play Tic Tac Toe in %s", event.getUser().getIdLong(), event.getGuild().getName())).queue());
                });
    }
}

