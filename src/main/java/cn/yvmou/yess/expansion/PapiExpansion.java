package cn.yvmou.yess.expansion;

import cn.yvmou.yess.Y;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class PapiExpansion extends PlaceholderExpansion {

    private final Y plugin;

    public PapiExpansion(Y plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "yess";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (params.equalsIgnoreCase("glow_status")) {
            return Y.glowM().getIsGlowing(player.getUniqueId()) ? "on" : "off";
        }
        return null;
    }
}
