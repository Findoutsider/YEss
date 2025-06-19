package cn.yvmou.yess.events;

import cn.yvmou.yess.Y;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class DamageListener implements Listener {
    // 禁言队伍之间伤害
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player damager) || !(event.getEntity() instanceof Player player)) return;

        String leader_name = Y.getStorage().loadData(damager.getUniqueId(), "team-leader", String.class, null);
        if (leader_name == null) return;
        if (leader_name.equals(player.getName())) {
            event.setCancelled(true);
        }
    }
}
