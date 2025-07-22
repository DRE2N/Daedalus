package de.erethon.daedalus.thirdparty;

import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;

public class Floodgate {
    private Floodgate() {
    }

    public static boolean isBedrock(Player player) {
        return FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId());
    }
}
