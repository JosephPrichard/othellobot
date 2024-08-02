/*
 * Copyright (c) Joseph Prichard 2023.
 */

package services.challenge;

import services.player.Player;

public record Challenge(Player challenged, Player challenger) {

}
