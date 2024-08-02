/*
 * Copyright (c) Joseph Prichard 2023.
 */

package services.game;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import othello.OthelloBoard;
import othello.Tile;
import services.player.Player;

import java.util.List;

@ToString
@EqualsAndHashCode
public class Game {

    @EqualsAndHashCode.Exclude
    private final OthelloBoard board;

    private final Player whitePlayer;

    private final Player blackPlayer;

    @EqualsAndHashCode.Exclude
    private List<Tile> currPotentialMoves;

    public Game(OthelloBoard board, Player blackPlayer, Player whitePlayer) {
        this.board = board;
        this.blackPlayer = blackPlayer;
        this.whitePlayer = whitePlayer;
        this.currPotentialMoves = null;
    }

    public static Game start(Player blackPlayer, Player whitePlayer) {
        return new Game(OthelloBoard.initial(), blackPlayer, whitePlayer);
    }

    public static Game from(Game game) {
        var copiedGame = new Game(OthelloBoard.from(game.board), game.blackPlayer, game.whitePlayer);
        copiedGame.currPotentialMoves = game.currPotentialMoves;
        return copiedGame;
    }

    public OthelloBoard board() {
        return board;
    }

    public Player whitePlayer() {
        return whitePlayer;
    }

    public Player blackPlayer() {
        return blackPlayer;
    }

    public Player currentPlayer() {
        return board.isBlackMove() ? blackPlayer : whitePlayer;
    }

    public Player otherPlayer() {
        return board.isBlackMove() ? whitePlayer : blackPlayer;
    }

    public int blackScore() {
        return (int) board.blackScore();
    }

    public int whiteScore() {
        return (int) board.whiteScore();
    }

    public boolean isAgainstBot() {
        return whitePlayer.isBot() || blackPlayer.isBot();
    }

    public List<Tile> findPotentialMoves() {
        if (currPotentialMoves != null) {
            return currPotentialMoves;
        }
        var potentialMoves = board.findPotentialMoves();
        currPotentialMoves = potentialMoves;
        return potentialMoves;
    }

    public void makeMove(Tile move) {
        board.makeMove(move);
        currPotentialMoves = null;

        if (findPotentialMoves().isEmpty()) {
            board.skipTurn();
            currPotentialMoves = null;
        }
    }

    public boolean isOver() {
        return findPotentialMoves().isEmpty();
    }

    public boolean isBlackMove() {
        return board.isBlackMove();
    }

    public GameResult createResult() {
        var diff = blackScore() - whiteScore();
        if (diff > 0) {
            return GameResult.WinLoss(blackPlayer, whitePlayer);
        } else if (diff < 0) {
            return GameResult.WinLoss(whitePlayer, blackPlayer);
        } else {
            return GameResult.Draw(whitePlayer, blackPlayer);
        }
    }

    public GameResult createForfeitResult(Player forfeitingPlayer) {
        if (whitePlayer.equals(forfeitingPlayer)) {
            return GameResult.WinLoss(blackPlayer, whitePlayer);
        } else if (blackPlayer.equals(forfeitingPlayer)) {
            return GameResult.WinLoss(whitePlayer, blackPlayer);
        } else {
            throw new IllegalStateException("Player not part of a game attempted to forfeit");
        }
    }
}
