package pandorum.commands.client;

import mindustry.gen.Player;
import pandorum.Misc;

public class HubCommand implements ClientCommand {
    public static void run(final String[] args, final Player player) {
        Misc.connectToHub(player);
    }
}
