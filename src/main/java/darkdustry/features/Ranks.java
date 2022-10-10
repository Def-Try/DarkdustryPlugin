package darkdustry.features;

import arc.struct.Seq;
import darkdustry.features.Effects.FxPack;
import mindustry.gen.Player;

import java.util.Locale;

import static darkdustry.components.Bundle.*;
import static darkdustry.features.Effects.cache;

public class Ranks {

    public static Rank player, active, hyperActive, veteran, contributor, developer, admin, console, owner;

    public static void load() {
        player = new Rank() {{
            tag = "";
            name = "player";
            effects = Effects.pack1;

            next = active = new Rank() {{
                tag = "[accent]<[white]\uE800[]>[] ";
                name = "active";
                effects = Effects.pack2;
                req = new Requirements(300, 25000, 20);

                next = hyperActive = new Rank() {{
                    tag = "[accent]<[white]\uE813[]>[] ";
                    name = "hyperActive";
                    effects = Effects.pack3;
                    req = new Requirements(750, 50000, 40);

                    next = veteran = new Rank() {{
                        tag = "[accent]<[gold]\uE809[]>[] ";
                        name = "veteran";
                        effects = Effects.pack4;
                        req = new Requirements(1500, 100000, 100);
                    }};
                }};
            }};
        }};

        contributor = new Rank() {{
            tag = "[accent]<[yellow]\uE80F[]>[] ";
            name = "contributor";
            effects = Effects.pack5;
        }};

        developer = new Rank() {{
            tag = "[accent]<[#86dca2]\uE816[]>[] ";
            name = "developer";
            effects = Effects.pack5;
        }};

        admin = new Rank() {{
            tag = "[accent]<[scarlet]\uE817[]>[] ";
            name = "admin";
            effects = Effects.pack6;
        }};

        console = new Rank() {{
            tag = "[accent]<[#8d56b1]\uE85D[]>[] ";
            name = "console";
            effects = Effects.pack6;
        }};

        owner = new Rank() {{
            tag = "[accent]<[#0088ff]\uE810[]>[] ";
            name = "owner";
            effects = Effects.pack7;
        }};
    }

    public static Rank getRank(int id) {
        return Rank.ranks.get(id);
    }

    public static void setRank(Player player, Rank rank) {
        player.name(rank.tag + player.getInfo().lastName);
        cache.put(player.uuid(), rank.effects);
    }

    public static class Rank {
        public static final Seq<Rank> ranks = new Seq<>();

        public final int id;
        public String tag;
        public String name;
        public FxPack effects;

        public Requirements req;
        public Rank next;

        public Rank() {
            this.id = ranks.size;
            ranks.add(this);
        }

        public boolean hasNext() {
            return next != null && next.req != null;
        }

        public boolean checkNext(int playTime, int buildingsBuilt, int gamesPlayed) {
            return hasNext() && next.req.check(playTime, buildingsBuilt, gamesPlayed);
        }

        public String localisedName(Locale locale) {
            return tag + get("ranks." + name + ".name", "", locale);
        }

        public String localisedDesc(Locale locale) {
            return get("ranks." + name + ".desc", "", locale);
        }

        public String localisedReq(Locale locale) {
            return format("ranks.req", locale, localisedName(locale), req.playTime(), req.buildingsBuilt(), req.gamesPlayed());
        }
    }

    public record Requirements(int playTime, int buildingsBuilt, int gamesPlayed) {
        public boolean check(int playTime, int buildingsBuilt, int gamesPlayed) {
            return playTime >= this.playTime && buildingsBuilt >= this.buildingsBuilt && gamesPlayed >= this.gamesPlayed;
        }
    }
}