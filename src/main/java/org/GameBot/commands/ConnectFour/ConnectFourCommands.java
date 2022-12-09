package org.GameBot.commands.ConnectFour;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class ConnectFourCommands extends ListenerAdapter {
    ArrayList<ConnectFourGame> connectFourGames = new ArrayList<>();

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String command = event.getName();
        if (command.equals("connect-four")) {
            if (event.getOption("opponent") == null) {
                event.reply("Missing Opponent").queue();
                return;
            }

            if (connectFourGames.contains(new ConnectFourGame(event.getUser(), event.getOption("opponent").getAsUser()))) {
                event.reply("You already have a game in progress!").setEphemeral(true).queue();
            } else {
                List<Category> connectFourCategories = event.getGuild().getCategoriesByName("Connect 4 Games", true);
                if (connectFourCategories.size() == 0) {
                    event.getGuild().createCategory("Connect 4 Games").queue(c -> createConnectFourGame(event, c));
                } else {
                    createConnectFourGame(event, connectFourCategories.get(0));
                }
            }
        }

        if (command.equals("close-connect-four")) {
            if (event.getOption("opponent") == null) {
                event.reply("Missing Opponent").queue();
                return;
            }

            int index = this.connectFourGames.indexOf(new ConnectFourGame(event.getUser(), event.getOption("opponent").getAsUser()));
            if (index == -1) {
                event.reply("You don't have an on going game").setEphemeral(true).queue();
            } else {
                ConnectFourGame connectFourGame = this.connectFourGames.get(index);
                if (this.connectFourGames.size() == 1) {
                    connectFourGame.getGameChannel().getParentCategory().delete().queue();
                    connectFourGame.getGameChannel().delete().queue();
                } else {
                    connectFourGame.getGameChannel().delete().queue();
                }
                this.connectFourGames.remove(connectFourGame);
                event.getJDA().removeEventListener(connectFourGame);
                event.reply("Connect 4 Game Completed!").setEphemeral(true).queue();
            }

        }
    }

    private void createConnectFourGame(@NotNull SlashCommandInteractionEvent event, Category category) {
        String channelName = event.getUser().getName() + "-vs-" + event.getOption("opponent").getAsUser().getName() + "-connect4";

        event.getGuild().createTextChannel(channelName)
                .addPermissionOverride(event.getMember(), EnumSet.of(Permission.VIEW_CHANNEL), null)
                .addPermissionOverride(event.getOption("opponent").getAsMember(), EnumSet.of(Permission.VIEW_CHANNEL), null)
                .addPermissionOverride(event.getGuild().getBotRole(), EnumSet.of(Permission.VIEW_CHANNEL), null)
                .addPermissionOverride(event.getGuild().getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL))
                .setParent(category)
                .queue(channel -> {
                    ConnectFourGame connectFourGame;
                    connectFourGame = new ConnectFourGame(event.getUser(), event.getOption("opponent").getAsUser(), channel);
                    channel.getJDA().addEventListener(connectFourGame);
                    this.connectFourGames.add(connectFourGame);
                    event.reply("Started Connect 4 Game. Play here: <#" + channel.getId() + ">").setEphemeral(true).queue();
                    event.getOption("opponent").getAsUser().openPrivateChannel()
                            .queue(dm -> dm.sendMessage(String.format("<@%d> has invited you to play Connect 4 in %s", event.getUser().getIdLong(), event.getGuild().getName())).queue());
                });
    }
}
