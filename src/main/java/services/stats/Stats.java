/*
 * Copyright (c) Joseph Prichard 2023.
 */

package services.stats;

import services.player.Player;

public record Stats(Player player, float elo, int won, int lost, int drawn) {

    public Stats(Player player) {
        this(player, 0f, 0, 0, 0);
    }

    public Stats(StatsEntity statsEntity, String playerName) {
        this(new Player(statsEntity.playerId, playerName), statsEntity.elo,
            statsEntity.won, statsEntity.lost, statsEntity.drawn);
    }

    public float winRate() {
        var total = won + lost + drawn;
        if (total == 0) {
            return 0f;
        }
        return won / (float) (won + lost + drawn) * 100f;
    }
}
