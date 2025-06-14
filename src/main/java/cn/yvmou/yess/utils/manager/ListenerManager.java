package cn.yvmou.yess.utils.manager;

import cn.yvmou.yess.Y;
import cn.yvmou.yess.events.*;

public class ListenerManager {
    private final Y plugin;

    public ListenerManager(Y plugin) { this.plugin = plugin; }

    public void registerListener() {
        plugin.getServer().getPluginManager().registerEvents(new PlayerJoinListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PortalListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new GiftGUIListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new DamageListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerPortalListener(), plugin);
    }
}
