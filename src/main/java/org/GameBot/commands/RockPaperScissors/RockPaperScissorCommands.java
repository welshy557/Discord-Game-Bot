package org.GameBot.commands.RockPaperScissors;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class RockPaperScissorCommands extends ListenerAdapter {
    ArrayList<RockPaperScissorGame> rockPaperScissorGames = new ArrayList<>();

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String command = event.getName();
        if (command.equals("rock-paper-scissors")) {
            if (event.getOption("opponent") == null) {
                event.reply("Missing Opponent").queue();
                return;
            }

            if (rockPaperScissorGames.contains(new RockPaperScissorGame(event.getUser(), event.getOption("opponent").getAsUser()))) {
                event.reply("You already have a game in progress!").setEphemeral(true).queue();
            } else {
                List<Category> RockPaperScissorCategories = event.getGuild().getCategoriesByName("Rock Paper Scissor Games", true);
                if (RockPaperScissorCategories.size() == 0) {
                    event.getGuild().createCategory("Rock Paper Scissor Games").queue(c -> createRockPaperScissorGame(event, c));
                } else {
                    createRockPaperScissorGame(event, RockPaperScissorCategories.get(0));
                }
            }
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getButton().getId().equals("no")) {
            for (RockPaperScissorGame game: this.rockPaperScissorGames) {
                if (game.getGameChannel().getIdLong() == event.getChannel().getIdLong()) {
                    event.reply("Ending Game...").setEphemeral(true).queue();
                    game.getGameChannel().getParentCategory().delete().queue();
                    game.getGameChannel().delete().queue();
                    this.rockPaperScissorGames.remove(game);
                }
            }
        }

    }

    private void createRockPaperScissorGame(@NotNull SlashCommandInteractionEvent event, Category category) {
        String channelName = event.getUser().getName() + "-vs-" + event.getOption("opponent").getAsUser().getName() + "-rock-paper-scissors";

        event.getGuild().createTextChannel(channelName)
                .addPermissionOverride(event.getMember(), EnumSet.of(Permission.VIEW_CHANNEL), null)
                .addPermissionOverride(event.getOption("opponent").getAsMember(), EnumSet.of(Permission.VIEW_CHANNEL), null)
                .addPermissionOverride(event.getGuild().getBotRole(), EnumSet.of(Permission.VIEW_CHANNEL), null)
                .addPermissionOverride(event.getGuild().getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL))
                .setParent(category)
                .queue(channel -> {
                    RockPaperScissorGame RockPaperScissorGame;
                    RockPaperScissorGame = new RockPaperScissorGame(event.getUser(), event.getOption("opponent").getAsUser(), channel);
                    channel.getJDA().addEventListener(RockPaperScissorGame);
                    this.rockPaperScissorGames.add(RockPaperScissorGame);
                    event.reply("Started Rock Paper Scissor Game. Play here: <#" + channel.getId() + ">").setEphemeral(true).queue();
                    event.getOption("opponent").getAsUser().openPrivateChannel()
                            .queue(dm -> dm.sendMessage(String.format("<@%d> has invited you to play Rock Paper Scissors in %s", event.getUser().getIdLong(), event.getGuild().getName())).queue());
                });
    }
}

