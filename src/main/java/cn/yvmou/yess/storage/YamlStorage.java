package cn.yvmou.yess.storage;

import cn.yvmou.yess.Y;
import cn.yvmou.yess.utils.LoggerUtils;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.LogManager;

public class YamlStorage implements Storage{
    private final Y plugin;
    private File dataFolder;
    private File dataFile;
    private YamlConfiguration dataConfig;

    public YamlStorage(Y plugin) {
        this.plugin = plugin;
        init();
    }

    @Override
    public void init() {
        dataFolder = new File(plugin.getDataFolder(), "data");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        dataFile = new File(dataFolder, "player_data.yml");

        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> void saveData(UUID uuid, String type, T data) {
        dataConfig.set("player_data." + uuid + ".name", Bukkit.getOfflinePlayer(uuid).getName());
        if (data instanceof Boolean || data instanceof String || data instanceof Integer || data instanceof Double) {
            dataConfig.set("player_data." + uuid + "." + type, data);
        } else {
            LoggerUtils.error("不支持的数据类型" + data.getClass().getName());
        }
        save();
    }

    @Override
    public <T> T loadData(UUID uuid, String type, Class<T> clazz) {
        Object data = dataConfig.get("player_data." + uuid + "." + type);
        if (clazz.isInstance(data)) {
            return clazz.cast(data);
        } else {
            LoggerUtils.warn("数据类型不匹配: " + type + "，实际类型: " + (data == null ? "null" : data.getClass().getName()));
            return null;
        }
    }

    @Override
    public void close() {
    }


    private void save() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("无法保存玩家数据: " + e.getMessage());
        }
    }
}
