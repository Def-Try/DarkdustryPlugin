package rewrite.listeners;

import arc.Events;
import arc.func.Cons;
import mindustry.game.EventType.*;
import mindustry.net.Administration.Config;
import rewrite.DarkdustryPlugin;
import rewrite.components.Database.PlayerData;
import rewrite.features.Effects;
import rewrite.features.Ranks;
import rewrite.features.history.*;
import rewrite.features.history.History.HistoryStack;
import rewrite.utils.Find;

import static rewrite.PluginVars.*;
import static rewrite.components.Bundle.*;
import static rewrite.components.Database.*;
import static rewrite.components.MenuHandler.*;

public class PluginEvents {

    public static Cons<GameOverEvent> gameover;

    public static void load() {
        Events.on(AdminRequestEvent.class, event -> {
            switch (event.action) {
                case wave -> sendToChat("events.admin.wave", event.player.name);
                case kick -> sendToChat("events.admin.kick", event.player.name, event.other.name);
                case ban -> sendToChat("events.admin.ban", event.player.name, event.other.name);
                default -> {} // без этой строки vscode кидает ошибку
            }
        });
        Events.on(BlockBuildEndEvent.class, event -> {
            if (!event.unit.isPlayer()) return;
            if (History.enabled() && event.tile.build != null) History.put(new BlockEntry(event), event.tile);
            if (event.breaking) return;

            PlayerData data = getPlayerData(event.unit.getPlayer());
            data.buildingsBuilt++;
            setPlayerData(data);
        });
        Events.on(BuildSelectEvent.class, event -> {});
        Events.on(ConfigEvent.class, event -> {});
        Events.on(DepositEvent.class, event -> {
            if (History.enabled() && event.player != null) History.put(new DepositEntry(event), event.tile.tile);
        });
        Events.on(GameOverEvent.class, gameover = event -> {});
        Events.on(PlayerJoin.class, event -> {
            PlayerData data = getPlayerData(event.player);
            Ranks.setRank(event.player, Ranks.getRank(data.rank));

            Effects.onJoin(event.player);
            DarkdustryPlugin.info("@ зашел на сервер. [@]", event.player.name, event.player.uuid());
            sendToChat("events.player.join", event.player.name);
            bundled(event.player, "welcome.message", Config.serverName.string(), discordServerUrl);
        
            // Bot.sendEmbed(botChannel, Color.green, "@ присоединился", Strings.stripColors(event.player.name));
            // app.post(Bot::updateBotStatus); // TODO: добавить бота

            if (data.welcomeMessage) showMenu(event.player, welcomeMenu, "welcome.menu.header", "welcome.menu.content",
                    new String[][] { { "ui.menus.close" }, { "welcome.menu.disable" } }, null, Config.serverName.string(), discordServerUrl);
        });
        Events.on(PlayerLeave.class, event -> {
            Effects.onLeave(event.player);
            DarkdustryPlugin.info("@ вышел с сервера. [@]", event.player.name, event.player.uuid());
            sendToChat("events.player.leave", event.player.name);

            // Bot.sendEmbed(botChannel, Color.red, "@ отключился", Strings.stripColors(event.player.name));
            // app.post(Bot::updateBotStatus);

            // activeHistoryPlayers.remove(event.player.uuid());
            // activeSpectatingPlayers.remove(event.player.uuid());

            // if (currentVoteKick != null && event.player == currentVoteKick.target()) {
            //     currentVoteKick.stop();
            //     netServer.admins.handleKicked(event.player.uuid(), event.player.ip(), kickDuration);
            //     sendToChat("commands.votekick.left", event.player.name, kickDuration / 60000);
            // }

            //     if (config.mode == Gamemode.pvp) {
            //         Seq<String> teamVotes = votesSurrender.get(event.player.team(), Seq::new);
            //         if (teamVotes.remove(event.player.uuid())) {
            //             sendToChat("commands.surrender.left", coloredTeam(event.player.team()), event.player.name, teamVotes.size, Mathf.ceil(voteRatio * Groups.player.count(p -> p.team() == event.player.team())));
            //         }
            //     }

            // if (votesRtv.remove(event.player.uuid())) TODO: переделать эту ср*нь
            //     sendToChat("commands.rtv.left", event.player.name, votesRtv.size, Mathf.ceil(voteRatio * Groups.player.size()));

            // if (votesVnw.remove(event.player.uuid())) 
            //     sendToChat("commands.vnw.left", event.player.name, votesVnw.size, Mathf.ceil(voteRatio * Groups.player.size()));
        });
        Events.on(ServerLoadEvent.class, event -> {});
        Events.on(TapEvent.class, event -> {
            if (!History.enabled() || !activeHistory.contains(event.player.uuid()) || event.tile == null) return;

            StringBuilder result = new StringBuilder(format("history.title", Find.locale(event.player.locale), event.tile.x, event.tile.y));
            HistoryStack stack = History.get(event.tile.array());

            if (stack.isEmpty()) result.append(format("history.empty", Find.locale(event.player.locale)));
            else stack.each(entry -> result.append("\n").append(entry.getMessage(event.player)));

            event.player.sendMessage(result.toString());
        });
        Events.on(WithdrawEvent.class, event -> {
            if (History.enabled() && event.player != null) History.put(new WithdrawEntry(event), event.tile.tile);
        });
        Events.on(WorldLoadEvent.class, event -> {
            History.clear();
        });

        Events.run("HexedGameOver", () -> gameover.get(null));
        Events.run("CastleGameOver", () -> gameover.get(null));
    }
}
