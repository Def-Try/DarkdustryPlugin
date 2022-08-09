package darkdustry.features.votes;

import mindustry.gen.Player;

import static mindustry.Vars.*;
import static darkdustry.components.Bundle.*;

public class VoteVnw extends VoteSession {

    @Override
    public void vote(Player player, int sign) {
        super.vote(player, sign);
        sendToChat("commands.vnw.vote", player.coloredName(), votes, votesRequired());
    }

    @Override
    public void success() {
        stop();
        sendToChat("commands.vnw.passed");
        state.wavetime = 0f;
    }

    @Override
    public void fail() {
        stop();
        sendToChat("commands.vnw.failed");
    }
}
