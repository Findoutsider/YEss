package cn.yvmou.yess.gui;

import cn.yvmou.yess.Y;
import cn.yvmou.yess.utils.GUIUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class GiftLookGUI {
    private Inventory inventory;
    private String giftName;

    public Inventory getInventory() { return inventory; }
    public String getGiftName() { return giftName; }

    /**
     * 初始化
     */
    public void init(String giftName) {
        this.giftName = giftName;
        inventory = Bukkit.createInventory(null, 54, "§6预览礼包: " + giftName);
        // 加载已保存的物品
        List<ItemStack> savedItems = Y.getGiftM().loadGiftItems(giftName);
        for (int i = 0; i < savedItems.size() && i < 45; i++) {
            inventory.setItem(i, savedItems.get(i));
        }
        // 添加控制按钮
        ItemStack cancelButton = GUIUtils.createButton(Material.REDSTONE_BLOCK, "§c关闭", "§7点击取消编辑");
        inventory.setItem(49, cancelButton);
    }

    /**
     *
     * @param player 玩家
     */
    public void open(Player player) {
        player.openInventory(inventory);
    }


}
