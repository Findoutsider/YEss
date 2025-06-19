package cn.yvmou.yess;

import cn.yvmou.yess.managers.GiftM;
import cn.yvmou.yess.expansion.PapiExpansion;
import cn.yvmou.yess.managers.TeamM;
import cn.yvmou.yess.managers.GlowM;
import cn.yvmou.yess.storage.Storage;
import cn.yvmou.yess.utils.LoggerUtils;
import cn.yvmou.yess.utils.manager.CommandManager;
import cn.yvmou.yess.storage.StorageType;
import cn.yvmou.yess.utils.manager.ListenerManager;
import com.tcoded.folialib.FoliaLib;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class Y extends JavaPlugin {
    private static Y instance;
    private static FoliaLib foliaLib;
    private static GlowM glowM;
    private static Storage storage;
    private static GiftM giftM;
    private static TeamM teamM;

    public static Plugin getInstance() {
        return instance;
    }
    public static FoliaLib getFoliaLib() {return foliaLib;}
    public static Storage getStorage() {return storage;}
    public static GlowM glowM() { return glowM; }
    public static GiftM getGiftM() {return giftM;}
    public static TeamM getTeamM() {return teamM;}

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // 初始化
        instance = this;

        foliaLib = new FoliaLib(this);

        storage = StorageType.createStorage(this); // 初始化插件存储

        giftM = new GiftM(this); // 初始化礼包管理器
        teamM = new TeamM(this, storage);
        glowM = new GlowM(storage);

        new CommandManager(this).registerCommands();
        new ListenerManager(this).registerListener();

        // HOOK
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PapiExpansion(this).register(); //
            LoggerUtils.info(ChatColor.BLUE + "成功挂钩PlaceholderAPI");
        }
        LoggerUtils.info(ChatColor.GREEN + "插件加载成功！");
    }

    @Override
    public void onDisable() {
        // 关闭存储系统
        if (storage != null) {storage.close();}

        LoggerUtils.info(ChatColor.RED + "插件卸载成功！");
    }
}