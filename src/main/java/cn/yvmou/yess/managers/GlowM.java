package cn.yvmou.yess.managers;

import cn.yvmou.yess.Y;
import cn.yvmou.yess.storage.Storage;

import java.util.UUID;

public class GlowM {
    private final Storage storage;

    public GlowM(Storage storage) {
        this.storage = Y.getStorage();
    }
    // 玩家发光信息
    public boolean getIsGlowing(UUID uuid) {
        Object value = storage.loadData(uuid, "glow", Boolean.class, false);
        if (value != null) {
            return (boolean) value;
        }
        return false; // 默认值false
    }

    public void setIsGlowing(UUID uuid, boolean value) {
        storage.saveData(uuid, "glow", value);
    }

}
