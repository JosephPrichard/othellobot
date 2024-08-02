/*
 * Copyright (c) Joseph Prichard 2023.
 */

package commands;

import commands.context.CommandContext;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import services.player.Player;
import services.stats.IStatsService;
import services.stats.Stats;

import java.awt.*;

import static utils.LogUtils.LOGGER;

public class StatsCommand extends Command {

    private final IStatsService statsService;

    public StatsCommand(IStatsService statsService) {
        this.statsService = statsService;
    }

    public MessageEmbed buildStatsEmbed(Stats stats, User author) {
        var embed = new EmbedBuilder();
        embed.setColor(Color.GREEN)
            .setTitle(stats.player().name() + "'s stats")
            .addField("Rating", Float.toString(stats.elo()), false)
            .addField("Win Rate", stats.winRate() + "%", false)
            .addField("Won", Integer.toString(stats.won()), true)
            .addField("Lost", Integer.toString(stats.lost()), true)
            .addField("Drawn", Integer.toString(stats.drawn()), true)
            .setThumbnail(author.getAvatarUrl());
        return embed.build();
    }

    @Override
    public void onCommand(CommandContext ctx) {
        var userOpt = ctx.getParam("player");
        var user = userOpt != null ? userOpt.getAsUser() : ctx.getUser();

        var player = new Player(user);

        var stats = statsService.readStats(player);

        var embed = buildStatsEmbed(stats, user);
        ctx.replyEmbeds(embed);

        LOGGER.info("Retrieved stats for " + stats.player());
    }
}
