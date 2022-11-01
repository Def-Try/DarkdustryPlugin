package darkdustry.features.menus;

import darkdustry.features.Ranks;
import mindustry.gen.*;
import mindustry.ui.Menus.MenuListener;
import useful.*;

import static darkdustry.PluginVars.discordServerUrl;
import static darkdustry.components.MongoDB.*;
import static useful.Bundle.bundled;

public class MenuHandler {

    public static MenuListener choose(Runnable confirmed, Runnable denied) {
        return (player, option) -> {
            if (option == 0 && confirmed != null) confirmed.run();
            else if (option == 1 && denied != null) denied.run();
        };
    }

    public static void welcome(Player player, int option) {
        if (option == 1) Call.openURI(player.con, discordServerUrl);
        else if (option == 2) {
            getPlayerData(player).subscribe(data -> {
                data.welcomeMessage = false;
                setPlayerData(data).subscribe();
                bundled(player, "welcome.disabled");
            });
        }
    }

    public static void rankInfo(Player player, int option) {
        if (option != 1) return;

        var builder = new StringBuilder();
        Ranks.all.each(rank -> rank.req != null, rank -> builder.append(rank.localisedReq(player)).append("\n"));

        showMenuClose(player, "commands.rank.requirements.header", builder.toString());
    }

    public static void showMenu(Player player, String title, String content, String[][] buttons, MenuListener listener, Object... values) {
        for (int i = 0; i < buttons.length; i++)
            for (int j = 0; j < buttons[i].length; j++)
                buttons[i][j] = Bundle.get(buttons[i][j], player);

        DynamicMenus.menu(player, Bundle.get(title, player), Bundle.format(content, player, values), buttons, listener);
    }

    public static void showMenuClose(Player player, String title, String content, Object... values) {
        showMenu(player, title, content, new String[][] {{"ui.button.close"}}, null, values);
    }

    public static void showMenuConfirm(Player player, String title, String content, Runnable confirmed, Object... values) {
        showMenuConfirm(player, title, content, confirmed, null, values);
    }

    public static void showMenuConfirm(Player player, String title, String content, Runnable confirmed, Runnable denied, Object... values) {
        showMenu(player, title, content, new String[][] {{"ui.button.yes", "ui.button.no"}}, choose(confirmed, denied), values);
    }
}