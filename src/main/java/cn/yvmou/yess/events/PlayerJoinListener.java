package cn.yvmou.yess.events;

import cn.yvmou.yess.Y;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PlayerJoinListener implements Listener {
    /**
     * 玩家进入游戏时给予光芒效果
     *
     * @param e 事件
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        if (Y.glowM().getIsGlowing(player.getUniqueId())) {
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.GLOWING,
                    Integer.MAX_VALUE,
                    0,
                    false,
                    false,
                    true
            ));
        } else {
            player.removePotionEffect(PotionEffectType.GLOWING);
        }
    }
}
