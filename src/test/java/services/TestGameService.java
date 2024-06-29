/*
 * Copyright (c) Joseph Prichard 2023.
 */

package services;

import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import othello.Tile;
import services.game.Game;
import services.game.GameService;
import services.game.exceptions.AlreadyPlayingException;
import services.game.exceptions.InvalidMoveException;
import services.game.exceptions.NotPlayingException;
import services.game.exceptions.TurnException;
import services.player.Player;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
public class TestGameService {

    @InjectMocks
    private GameService gameService;

    @Test
    public void whenDuplicateCreate_fail() {
        var whitePlayer = new Player(1000, "Player1");
        var blackPlayer = new Player(1001, "Player2");

        Assertions.assertThrows(AlreadyPlayingException.class, () -> {
            gameService.createGame(blackPlayer, whitePlayer);
            gameService.createGame(blackPlayer, whitePlayer);
        });
    }

    @Test
    public void whenSaveThenDelete_success() throws AlreadyPlayingException {
        var whitePlayer = new Player(1000, "Player1");
        var blackPlayer = new Player(1001, "Player2");

        gameService.createGame(blackPlayer, whitePlayer);

        var game = gameService.getGame(blackPlayer);
        assert game != null;

        gameService.deleteGame(game);
        game = gameService.getGame(whitePlayer);

        Assertions.assertNull(game);
    }

    @Test
    public void whenGetGame_success() throws AlreadyPlayingException {
        var whitePlayer = new Player(1000, "Player1");
        var blackPlayer = new Player(1001, "Player2");
        gameService.createGame(blackPlayer, whitePlayer);

        var game = gameService.getGame(whitePlayer);

        Assertions.assertNotNull(game);
        Assertions.assertEquals(game.whitePlayer(), whitePlayer);
    }

    @Test
    public void whenGetInvalidGame_returnNull() {
        var player = new Player(1000, "Player1");

        var game = gameService.getGame(player);

        Assertions.assertNull(game);
    }

    @Test
    public void whenMove_ifInvalid_fail() throws AlreadyPlayingException {
        var whitePlayer = new Player(1000, "Player1");
        var blackPlayer = new Player(1001, "Player2");
        gameService.createGame(blackPlayer, whitePlayer);

        Assertions.assertThrows(InvalidMoveException.class, () ->
            gameService.makeMove(blackPlayer, Tile.fromNotation("a1")));
    }

    @Test
    public void whenMove_ifAlreadyPlaying_fail() throws AlreadyPlayingException {
        var whitePlayer = new Player(1000, "Player1");
        var blackPlayer = new Player(1001, "Player2");
        gameService.createGame(blackPlayer, whitePlayer);

        Assertions.assertThrows(TurnException.class, () ->
            gameService.makeMove(whitePlayer, Tile.fromNotation("d3")));
    }

    @Test
    public void whenMove_ifNotPlaying_fail() {
        var player = new Player(1000, "Player1");

        Assertions.assertThrows(NotPlayingException.class, () ->
            gameService.makeMove(player, Tile.fromNotation("d3")));
    }

    @Test
    public void whenMove_success() throws AlreadyPlayingException, TurnException, NotPlayingException, InvalidMoveException {
        var whitePlayer = new Player(1000, "Player1");
        var blackPlayer = new Player(1001, "Player2");
        var game = gameService.createGame(blackPlayer, whitePlayer);

        var movedGame = gameService.makeMove(blackPlayer, Tile.fromNotation("d3"));

        Assertions.assertEquals(game, movedGame);
        Assertions.assertNotEquals(game.board(), movedGame.board());
        Assertions.assertNotEquals(game.board(), movedGame.board().makeMoved("d3"));
    }

    @Test
    public void whenMove_parallel_success() throws Exception {
        var whitePlayer = new Player(1000, "Player1");
        var blackPlayer = new Player(1001, "Player2");
        var game = gameService.createGame(blackPlayer, whitePlayer);

        var results = Stream
            .generate(() -> CompletableFuture.supplyAsync(() -> {
                try {
                    var movedGame = gameService.makeMove(blackPlayer, Tile.fromNotation("d3"));

                    Assertions.assertEquals(game, movedGame);
                    Assertions.assertNotEquals(game.board(), movedGame.board());
                    Assertions.assertNotEquals(game.board(), movedGame.board().makeMoved("d3"));

                    return Pair.<Game, Exception>of(movedGame, null);
                } catch (Exception e) {
                    return Pair.<Game, Exception>of(null, e);
                }
            }))
            .limit(100)
            .map(CompletableFuture::join)
            .toList();

        int successCount = 0;

        for (var result : results) {
            if (result.getLeft() != null) {
                var movedGame = result.getLeft();

                Assertions.assertEquals(game, movedGame);
                Assertions.assertNotEquals(game.board(), movedGame.board());
                Assertions.assertNotEquals(game.board(), movedGame.board().makeMoved("d3"));

                successCount += 1;
            } else {
                if (!(result.getRight() instanceof TurnException)) {
                    throw result.getRight();
                }
            }
        }

        Assertions.assertEquals(successCount, 1);
    }
}
