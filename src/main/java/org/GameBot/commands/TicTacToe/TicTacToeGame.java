package org.GameBot.commands.TicTacToe;

import net.dv8tion.jda.api.entities.EmbedType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;

public class TicTacToeGame extends ListenerAdapter {
    private static final String EMPTY = ":black_large_square:";
    private static final String USER = ":x:";
    private static final String OPPONENT = ":o:";

    private final User user;
    private final User opponent;
    private final TextChannel gameChannel;
    private User playersTurn;

    private Message boardMessage;
    private Message buttonsMessageOne;
    private Message buttonsMessageTwo;
    private boolean isCompleted = false;
    private User winner;


    private final String[][] board = new String[3][3];

    public TicTacToeGame(User user, User opponent, TextChannel gameChannel) {
        this.user = user;
        this.gameChannel = gameChannel;
        this.opponent = opponent;
        this.playersTurn = user;

        // Make empty board
        for (String[] row : this.board) {
            Arrays.fill(row, TicTacToeGame.EMPTY);
        }


        // Create Buttons
        ArrayList<Button> buttonsOne = new ArrayList<>();

        // column, row
        buttonsOne.add(Button.primary("0,0", "1"));
        buttonsOne.add(Button.primary("0,1", "2"));
        buttonsOne.add(Button.primary("0,2", "3"));

        ArrayList<Button> buttonsTwo = new ArrayList<>();
        buttonsTwo.add(Button.primary("1,0", "4"));
        buttonsTwo.add(Button.primary("1,1", "5"));
        buttonsTwo.add(Button.primary("1,2", "6"));

        ArrayList<Button> buttonsThree = new ArrayList<>();
        buttonsThree.add(Button.primary("2,0", "7"));
        buttonsThree.add(Button.primary("2,1", "8"));
        buttonsThree.add(Button.primary("2,2", "9"));

        // Draw Board Emojis
        gameChannel.sendMessageEmbeds(drawBoard()).addActionRow(buttonsOne).queue(boardMessage -> this.boardMessage = boardMessage);
        gameChannel.sendMessage("").addActionRow(buttonsTwo).queue(buttonsMessageOne -> this.buttonsMessageOne = buttonsMessageOne);
        gameChannel.sendMessage("").addActionRow(buttonsThree).queue(buttonsMessageTwo -> this.buttonsMessageTwo = buttonsMessageTwo);
    }

    public TicTacToeGame(User user, User opponent) {
        this.user = user;
        this.opponent = opponent;
        this.gameChannel = null;
        this.boardMessage = null;
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if ((event.getMessage().getIdLong() == this.boardMessage.getIdLong()
                || event.getMessage().getIdLong() == this.buttonsMessageOne.getIdLong()
                || event.getMessage().getIdLong() == this.buttonsMessageTwo.getIdLong())
            && event.getChannel().getIdLong() == this.gameChannel.getIdLong()
        ) {

            if (event.getUser().getId().equals(this.playersTurn.getId()) && !isCompleted) {
                this.playersTurn = event.getUser().getId().equals(this.user.getId()) ?
                        this.opponent : this.user;

                String[] movePosition = event.getButton().getId().split(",");
                int column = Integer.parseInt(movePosition[1]);
                int row = Integer.parseInt(movePosition[0]);
                makeMove(event, column, row);

                if (this.isCompleted) {
                    event.reply((this.winner == null ? "Tie Game" : this.winner.getAsMention() + " Won!") + "\nEnter /close-tic-tac-toe to close the channel").queue();
                } else {
                    event.reply("Successful Play").setEphemeral(true).queue();
                }

            } else if (isCompleted) {
                event.reply("Game is over.\nEnter /close-tic-tac-toe to close the channel").setEphemeral(true).queue();
            } else {
                event.reply("Not your turn!").setEphemeral(true).queue();

            }
        }
    }

    private void makeMove(ButtonInteractionEvent event, int column, int row) {
        if (this.board[row][column].equals(TicTacToeGame.EMPTY)) {

            this.board[row][column] = event.getUser().getId().equals(this.user.getId()) ?
                    TicTacToeGame.USER : TicTacToeGame.OPPONENT;

            if (isWinner()) {
                this.isCompleted = true;
                this.winner = this.playersTurn.getId().equals(this.user.getId()) ? this.opponent : this.user;
            }

            if (isDraw()) {
                this.isCompleted = true;
                this.winner = null;
            }

            this.boardMessage.editMessageEmbeds(drawBoard()).queue();
        }
    }

    private boolean isWinner() {
        for (int i = 0; i < this.board.length; i++) {
            for (int j = 0; j < this.board[i].length; j++) {
                String letter = this.board[i][j];
                if (!letter.equals(TicTacToeGame.EMPTY) && isThreeInRow(i, j, this.board[i][j])) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isDraw() {
        int count = 0;
        for (String[] row : this.board) {
            for (String letter : row) {
                if (!letter.equals(TicTacToeGame.EMPTY)) {
                    count++;
                }
            }
        }
        return count == 9;
    }

    private boolean isThreeInRow(int row, int column, String color) {
        int rightCount = 0;
        int rightColumn = column;
        for (int i = 0; i < 2; i++) {
            if (rightColumn + 1 < this.board.length && color.equals(this.board[row][rightColumn + 1])) {
                rightCount += 1;
            }
            rightColumn++;
        }
        int topRightCount = 0;
        int topRightColumn = column;
        int topRightRow = row;
        for (int i = 0; i < 2; i++) {
            if (topRightRow - 1 >= 0 && topRightColumn + 1 < this.board.length && color.equals(this.board[topRightRow - 1][topRightColumn + 1])) {
                topRightCount += 1;
            }
            topRightColumn++;
            topRightRow--;
        }

        int topCount = 0;
        int topRow = row;
        for (int i = 0; i < 2; i++) {
            if (topRow - 1 >= 0 && color.equals(this.board[topRow - 1][column])) {
                topCount += 1;
            }
            topRow--;
        }

        int topLeftCount = 0;
        int topLeftColumn = column;
        int topLeftRow = row;
        for (int i = 0; i < 2; i++) {
            if (topLeftRow - 1 >= 0 && topLeftColumn - 1 >= 0 && color.equals(this.board[topLeftRow - 1][topLeftColumn - 1])) {
                topLeftCount += 1;
            }
            topLeftColumn--;
            topLeftRow--;
        }

        int leftCount = 0;
        int leftColumn = column;
        for (int i = 0; i < 2; i++) {
            if (leftColumn - 1 >= 0 && color.equals(this.board[row][leftColumn - 1])) {
                leftCount += 1;
            }
            leftColumn--;
        }

        return rightCount == 2 || topRightCount == 2 || topCount == 2 || topLeftCount == 2 || leftCount == 2;
    }

    private MessageEmbed drawBoard() {
        ArrayList<MessageEmbed.Field> fields = new ArrayList<>();
        for (String[] row : this.board) {
            StringBuilder letters = new StringBuilder();
            for (int i = 0; i < row.length; i++) {
                if (i == row.length - 1) {
                    letters.append(row[i]);
                } else {
                    letters.append(row[i]).append("\t");
                }

            }

            fields.add(new MessageEmbed.Field("", letters.toString(), false));
        }

        return new MessageEmbed(
                null,
                "Tic Tac Toe",
                this.isCompleted ? (this.winner == null ? "Tie Game!" : this.winner.getAsMention() + " Won!") : this.playersTurn.getAsMention() + "'s turn",
                EmbedType.RICH,
                OffsetDateTime.now(),
                0,
                null,
                null,
                new MessageEmbed.AuthorInfo("Game Bot", null, null, null),
                null,
                null,
                null,
                fields);

    }


    public TextChannel getGameChannel() {
        return this.gameChannel;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (o.getClass() == this.getClass()) {
            TicTacToeGame ticTacToeGame = (TicTacToeGame) o;
            return ticTacToeGame.user.getId().equals(this.user.getId()) && ticTacToeGame.opponent.getId().equals(this.opponent.getId());
        }
        return false;
    }

}

