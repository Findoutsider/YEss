package cn.yvmou.yess.storage;

import java.util.UUID;

public interface Storage {
    /**
     * 初始化存储系统
     */
    void init();

    /**
     * 保存玩家数据
     * 
     * @param uuid 玩家 UUID
     * @param type 数据保存类型标识
     * @param data 需要保存的数据
     * @param <T> 保存数据的类型
     */
    <T> void saveData(UUID uuid, String type, T data);

    /**
     * 加载玩家数据
     *
     * @param uuid 玩家 UUID
     * @param type 数据保存类型标识
     * @param clazz 期望返回的数据类型的 Class 对象
     * @param defaultData 价值数据的默认值
     * @return 如果类型匹配则返回数据，否则返回 null
     * @param <T> 返回的数据类型
     */
    <T> T loadData(UUID uuid, String type, Class<T> clazz, T defaultData);

    /**
     * 关闭存储连接
     */
    void close();
}
