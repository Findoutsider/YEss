package cn.yvmou.yess.storage;

import cn.yvmou.yess.Y;

import java.util.UUID;

public class PlayerData {
    // 玩家发光状态
    public boolean getIsGlowing(UUID uuid) {
        Object value = Y.getStorage().loadData(uuid, "glow", Boolean.class);
        if (value != null) {
            return (boolean) value;
        }
        return false; // 默认值false
    }

    public void setIsGlowing(UUID uuid, boolean value) {
        Y.getStorage().saveData(uuid, "glow", value);
    }
}
