package cn.yvmou.yess.events;

import cn.yvmou.yess.Y;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GiftGUIListener implements Listener {
    private int identifier;

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        String title = event.getView().getTitle();
        // 编辑礼包界面
        if (title.startsWith("§6编辑礼包: ")) {
            identifier = 1;
            // 只允许在前45格放置物品
            if (event.getRawSlot() >= 45 && event.getRawSlot() <= 53) {
                event.setCancelled(true);

                handleClick(player, event.getSlot());
            }
        }
        // 预览礼包界面
        if (title.startsWith("§6预览礼包: ")) {
            identifier = 2;
            event.setCancelled(true);
            handleClick(player, event.getSlot());
        }
    }

    /**
     * 处理按钮的点击
     *
     * @param player 玩家
     * @param slot 位置
     */
    public void handleClick(Player player, int slot) {
        if (identifier == 1) {
            // 编辑里面界面
            if (slot == 49) { // 保存按钮
                List<ItemStack> items = new ArrayList<>();
                for (int i = 0; i < 45; i++) {
                    ItemStack item = Y.getGiftM().getGiftEditGUI().getInventory().getItem(i);

                    if (item != null) {
                        items.add(item);
                    }

                    Y.getGiftM().getItems().addAll(items);
                }

                Y.getGiftM().saveGiftItems(Y.getGiftM().getGiftEditGUI().getGiftName());

                player.closeInventory();
            } else if (slot == 45) { // 取消按钮
                player.closeInventory();
            } else if (slot == 53) { // 清空按钮
                for (int i = 0; i < 45; i++) {
                    Y.getGiftM().getGiftEditGUI().getInventory().setItem(i, null);
                }
                player.sendMessage("§c礼包已清空！");
            }
        } else if (identifier == 2) {
            // 预览里面界面
            if (slot == 49) {
                player.closeInventory();
            }
        }

    }
}
