package darkdustry.listeners;

import arc.Events;
import arc.util.Log;
import darkdustry.components.*;
import darkdustry.discord.Bot;
import darkdustry.features.*;
import darkdustry.features.history.*;
import darkdustry.features.menus.MenuHandler;
import mindustry.content.Blocks;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import useful.*;

import static arc.Core.app;
import static darkdustry.PluginVars.*;
import static darkdustry.components.Config.Gamemode.sandbox;
import static darkdustry.components.Database.*;
import static darkdustry.discord.Bot.Palette.*;
import static darkdustry.discord.Bot.*;
import static darkdustry.components.DoubleTap.lastTaps;
import static darkdustry.features.Effects.effectsCache;
import static darkdustry.features.Ranks.updateRank;
import static darkdustry.features.menus.MenuHandler.showMenu;
import static mindustry.Vars.*;
import static mindustry.net.Administration.Config.serverName;
import static useful.Bundle.*;

public class PluginEvents {

    public static void load() {
        Events.on(ServerLoadEvent.class, event -> sendEmbed(botChannel, info, "Server launched"));

        Events.on(PlayEvent.class, event -> {
            state.rules.unitPayloadUpdate = true;
            state.rules.showSpawns = true;

            state.rules.revealedBlocks.addAll(Blocks.slagCentrifuge, Blocks.heatReactor, Blocks.scrapWall, Blocks.scrapWallLarge, Blocks.scrapWallHuge, Blocks.scrapWallGigantic, Blocks.thruster);

            if (config.mode == sandbox)
                state.rules.revealedBlocks.addAll(Blocks.shieldProjector, Blocks.largeShieldProjector, Blocks.beamLink);
        });

        Events.on(GameOverEvent.class, event -> getPlayersData(Groups.player).doOnNext(data -> data.gamesPlayed++).flatMap(Database::setPlayerData).subscribe());

        Events.on(WaveEvent.class, event -> getPlayersData(Groups.player).doOnNext(data -> data.wavesSurvived++).flatMap(Database::setPlayerData).subscribe());

        Events.on(WorldLoadEvent.class, event -> {
            History.clear();
            DoubleTap.clear();
            DynamicMenus.clear();

            app.post(Bot::updateBotStatus);
        });

        Events.on(WithdrawEvent.class, event -> {
            if (History.enabled() && event.player != null)
                History.put(new WithdrawEntry(event), event.tile.tile);
        });

        Events.on(DepositEvent.class, event -> {
            if (History.enabled() && event.player != null)
                History.put(new DepositEntry(event), event.tile.tile);
            Alerts.depositAlert(event);
        });

        Events.on(ConfigEvent.class, event -> {
            if (History.enabled() && event.player != null)
                History.put(new ConfigEntry(event), event.tile.tile);
        });

        Events.on(TapEvent.class, event -> {
            if (!History.enabled() || event.tile == null) return;

            getPlayerData(event.player).subscribe(data -> {
                if (!data.doubleTapHistory) return;

                DoubleTap.check(event, () -> {
                    var builder = new StringBuilder();
                    var stack = History.get(event.tile.array());

                    if (stack.isEmpty()) builder.append(Bundle.get("history.empty", event.player));
                    else stack.each(entry -> builder.append("\n").append(entry.getMessage(event.player)));

                    bundled(event.player, "history.title", event.tile.x, event.tile.y, builder.toString());
                });
            });
        });

        Events.on(BlockBuildEndEvent.class, event -> {
            if (!event.unit.isPlayer()) return;

            if (History.enabled() && event.tile.build != null)
                History.put(new BlockEntry(event), event.tile);

            getPlayerData(event.unit.getPlayer()).subscribe(data -> {
                if (event.breaking) data.blocksBroken++;
                else data.blocksPlaced++;
                setPlayerData(data).subscribe();
            });
        });

        Events.on(BuildSelectEvent.class, event -> {
            if (event.breaking || event.builder == null || event.builder.buildPlan() == null || !event.builder.isPlayer())
                return;
            Alerts.buildAlert(event);
        });

        Events.on(PlayerJoin.class, event -> getPlayerData(event.player).subscribe(data -> {
            updateRank(event.player, data);
            setPlayerData(data).subscribe(); // Update last name in database

            app.post(() -> Effects.onJoin(event.player));

            Log.info("@ has connected. [@]", event.player.plainName(), event.player.uuid());
            sendToChat(player -> player.con.isConnected(), "events.join", event.player.coloredName());
            bundled(event.player, "welcome.message", serverName.string(), discordServerUrl);

            sendEmbed(botChannel, success, "@ joined", event.player.plainName());

            if (data.welcomeMessage) {
                var builder = new StringBuilder();
                welcomeMessageCommands.each(command -> builder.append("\n[cyan]").append(clientCommands.getPrefix()).append(command).append("[gray] - [lightgray]").append(Bundle.get("commands." + command + ".description", event.player)));

                showMenu(event.player, "welcome.header", "welcome.content", new String[][] {{"ui.button.close"}, {"ui.button.discord"}, {"ui.button.disable"}}, MenuHandler::welcome, serverName.string(), builder.toString());
            }

            app.post(Bot::updateBotStatus);
        }));

        Events.on(PlayerLeave.class, event -> {
            Effects.onLeave(event.player);

            Log.info("@ has disconnected. [@]", event.player.plainName(), event.player.uuid());
            sendToChat(player -> player.con.isConnected(), "events.leave", event.player.coloredName());
            sendEmbed(botChannel, error, "@ left", event.player.plainName());

            lastTaps.remove(event.player.id);
            effectsCache.remove(event.player.id);

            if (vote != null) vote.left(event.player);
            if (voteKick != null) voteKick.left(event.player);

            app.post(Bot::updateBotStatus);
        });

        Events.run(Trigger.update, () -> Groups.player.each(player -> player != null && player.unit().moving(), Effects::onMove));

        Events.run("Gameover", () -> getPlayersData(Groups.player).doOnNext(data -> data.gamesPlayed++).flatMap(Database::setPlayerData).subscribe());
    }
}