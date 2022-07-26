package pandorum.commands.client;

import arc.util.CommandHandler.CommandRunner;
import mindustry.content.UnitTypes;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.type.UnitType;
import pandorum.components.Icons;

import static pandorum.util.PlayerUtils.bundled;
import static pandorum.util.PlayerUtils.isAdmin;
import static pandorum.util.Search.findPlayer;
import static pandorum.util.Search.findUnit;
import static pandorum.util.StringUtils.unitsList;

public class UnitCommand implements CommandRunner<Player> {
    public void accept(String[] args, Player player) {
        if (!isAdmin(player)) {
            bundled(player, "commands.permission-denied");
            return;
        }

        UnitType type = findUnit(args[0]);
        if (type == null || type == UnitTypes.block) {
            bundled(player, "commands.unit-not-found", unitsList());
            return;
        }

        Player target = args.length > 1 ? findPlayer(args[1]) : player;
        if (target == null) {
            bundled(player, "commands.player-not-found", args[1]);
            return;
        }

        Unit unit = type.spawn(target.team(), target.x, target.y);
        target.unit(unit);
        unit.spawnedByCore(true);
        bundled(target, "commands.unit.success", Icons.get(type.name));
        if (target != player)
            bundled(player, "commands.unit.success.player", target.name, Icons.get(type.name));
    }
}
