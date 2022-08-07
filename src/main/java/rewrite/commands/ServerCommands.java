package rewrite.commands;

import arc.util.CommandHandler;
import arc.util.Log;
import mindustry.core.GameState.State;
import mindustry.game.Gamemode;
import mindustry.maps.Map;
import mindustry.maps.MapException;
import rewrite.discord.Bot;
import rewrite.utils.Find;

import java.util.Locale;

import static arc.Core.*;
import static mindustry.Vars.*;
import static rewrite.components.Bundle.*;
import static rewrite.utils.Checks.*;

public class ServerCommands extends Commands<NullPointerException> {

    public ServerCommands(CommandHandler handler, Locale def) {
        super(handler, def);

        for (String command : new String[] {"fillitems", "pause", "shuffle", "runwave"})
            handler.removeCommand(command);

        register("exit", args -> {
            Log.info("Shutting down server.");
            System.exit(2);
        });

        register("stop", args -> {
            net.closeServer();
            state.set(State.menu);
            Log.info("Stopped server.");
        });

        register("host", args -> {
            if (isLaunched()) return;

            Gamemode mode;
            if (args.length > 1) {
                mode = Find.mode(args[1]);
                if (notFound(mode, args)) return;
            } else {
                mode = Gamemode.survival;
                Log.info("Default mode selected to be @.", mode.name());
            }

            Map map;
            if (args.length > 0) {
                map = Find.map(args[0]);
                if (notFound(map, args)) return;
            } else {
                map = maps.getShuffleMode().next(mode, state.map);
                Log.info("Randomized next map to be @.", map.name());
            }

            logic.reset();

            app.post(() -> {
                try {
                    Log.info("Loading map...");

                    world.loadMap(map, map.applyRules(mode));
                    state.rules = map.applyRules(mode);
                    logic.play();
                    netServer.openServer();

                    Log.info("Map loaded.");
                } catch (MapException exception) {
                    Log.err("@: @", exception.map.name(), exception.getMessage());
                }
            });
        });

        register("say", args -> {
            Log.info("&fi@: &fr&lw@", "&lcServer", "&lw" + args[0]);
            sendToChat("commands.say.chat", args[0]);
            Bot.sendMessage(Bot.botChannel, "Сервер » @", args[0]);
        });
    }
}
