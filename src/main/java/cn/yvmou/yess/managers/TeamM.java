package cn.yvmou.yess.managers;

import cn.yvmou.yess.Y;
import cn.yvmou.yess.storage.Storage;
import cn.yvmou.yess.utils.SchedulerUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

/*
    队长名称-leader（以队长的名称标记唯一队伍！）

    队长的标识：
        created: true
        joined: false
        leader: 队长名称
    队员的标识:
        created: false
        joined: true
        leader: 队长名称
    暂时的标识:
        inviter: 邀请者（一般为队长）名称 -> 标识被邀请玩家的邀请者（队长）是谁。同时用于标识玩家是否处于被邀请的状态。
*/
public class TeamM {
    private final Y plugin;
    private final Storage storage;

    public TeamM(Y plugin, Storage storage) {
        this.plugin = plugin;
        this.storage = storage;
    }

    /**
     * 玩家是否正在被邀请
     * @param player 玩家对象
     * @return true: 正在被邀请, false: 未被邀请
     */
    private boolean isInvited(Player player) {
        String s = storage.loadData(player.getUniqueId(), "team-inviter", String.class, null);
        return s != null;
    }

    /**
     * 玩家是否已经加入一个队伍，仅判断
     * @param player 需要判断的玩家对象
     * @return true: 已经加入队伍, false: 未加入队伍
     */
    private boolean hasTeam(Player player) {
        boolean joined = storage.loadData(player.getUniqueId(), "team-joined", Boolean.class, false);
        boolean created = storage.loadData(player.getUniqueId(), "team-created", Boolean.class, false);
        return joined || created;
    }

    /**
     * 创建队伍
     * @param senderPlayer 发送命令的玩家对象
     */
    public void createTeam(Player senderPlayer) {
        if (isInvited(senderPlayer)) {
            senderPlayer.sendMessage("§c你有一个待处理的队伍邀请！");
            return;
        }
        if (hasTeam(senderPlayer)) {
            senderPlayer.sendMessage("§c你已经加入了一个队伍！");
            return;
        }
        // 更新玩家队伍状态
        storage.saveData(senderPlayer.getUniqueId(), "team-created", true);
        storage.saveData(senderPlayer.getUniqueId(), "team-joined", false);
        storage.saveData(senderPlayer.getUniqueId(), "team-leader", senderPlayer.getName());
    }

    public void inviteTeam(Player senderPlayer, Player targetPlayer) {
        if (!hasTeam(senderPlayer)) {
            senderPlayer.sendMessage("§c你还没有队伍！");
            return;
        }
        if (isInvited(targetPlayer)) {
            senderPlayer.sendMessage("§c该玩家已经被其他队伍邀请了！");
            return;
        }
        if (hasTeam(targetPlayer)) {
            senderPlayer.sendMessage("§c该玩家已经加入了一个队伍！");
            return;
        }
        targetPlayer.sendMessage("§a* " + senderPlayer.getName() + " 邀请你加入他的队伍\n" +
                "输入/yess team accept接收邀请，/yess team deny拒绝邀请\n2分钟内有效");
        senderPlayer.sendMessage("§a你已向" + targetPlayer.getName() + "发送邀请");
        // 更新玩家队伍状态。标记邀请人即队长名称，并将目标玩家设置为被邀请状态
        storage.saveData(targetPlayer.getUniqueId(), "team-inviter", senderPlayer.getName());

        String inviterName = senderPlayer.getName();

        // 20s 后将目标玩家恢复至未被邀请的状态，并将队长名称设置为 null
        SchedulerUtils.runTaskLater(plugin, () -> {
            // 只有在邀请还没被处理时才发过期消息
            String currentInviter = storage.loadData(targetPlayer.getUniqueId(), "team-inviter", String.class, null);
            if (inviterName.equals(currentInviter)) {
                storage.saveData(targetPlayer.getUniqueId(), "team-inviter", null);
                targetPlayer.sendMessage("§c来自玩家 " + inviterName + " 组队邀请已过期");
                senderPlayer.sendMessage("§c你发送的的组队邀请已过期");
            }
        }, 20 * 5);
    }

    public void acceptTeam(Player senderPlayer) {
        if (hasTeam(senderPlayer)) {
            senderPlayer.sendMessage("§c你已经加入一个队伍了");
            return;
        }
        if (!isInvited(senderPlayer)) {
            senderPlayer.sendMessage("§c你没有被邀请");
            return;
        }

        // 获取队长名称（以队长的名称标记唯一队伍！）
        String leaderName = storage.loadData(senderPlayer.getUniqueId(), "team-inviter", String.class, null);
        if (leaderName == null) {
            senderPlayer.sendMessage("§c邀请数据无效");
            return;
        }

        senderPlayer.sendMessage("§a你已接受 " + leaderName + " 的邀请");

        // 移除被邀请的状态
        storage.saveData(senderPlayer.getUniqueId(), "team-inviter", null);

        // 更新玩家队伍状态。加入队伍
        storage.saveData(senderPlayer.getUniqueId(), "team-joined", true);
        storage.saveData(senderPlayer.getUniqueId(), "team-created", false);
        storage.saveData(senderPlayer.getUniqueId(), "team-leader", leaderName);

        // 向所有在线的队伍成员发送新人进队伍的消息
        getAllOnlineTeamPlayers(leaderName).forEach(player -> {
            Player p = Bukkit.getPlayer(player);
            if (p != null) {
                p.sendMessage("§a玩家 " + senderPlayer.getName() + " 加入了队伍");
            }
        });
    }

    public void denyTeam(Player senderPlayer) {
        if (!isInvited(senderPlayer)) {
            senderPlayer.sendMessage("§c你没有被邀请");
            return;
        }

        // 获取队长名称
        String leaderName = storage.loadData(senderPlayer.getUniqueId(), "team-inviter", String.class, null);
        if (leaderName == null) {
            senderPlayer.sendMessage("§c邀请数据无效");
            return;
        }
        senderPlayer.sendMessage("§c你拒绝了 " + leaderName + " 的邀请");

        // 移除被邀请状态
        storage.saveData(senderPlayer.getUniqueId(), "team-inviter", null);


        // 更新玩家队伍状态。保险起见，移除玩家队伍，即使玩家未加入任何队伍
        storage.saveData(senderPlayer.getUniqueId(), "team-joined", false);
        storage.saveData(senderPlayer.getUniqueId(), "team-leader", null);
        storage.saveData(senderPlayer.getUniqueId(), "team-created", false);

        // 向邀请者发送消息
        Player leaderPlayer = plugin.getServer().getPlayer(leaderName);
        if (leaderPlayer != null) leaderPlayer.sendMessage("§c玩家 " + senderPlayer.getName() + " 拒绝了你的邀请");
    }

    public void leaveTeam(Player senderPlayer) {
        if (!hasTeam(senderPlayer)) {
            senderPlayer.sendMessage("§c你没有加入队伍");
            return;
        }

        // 提取队长名称
        String leaderName = storage.loadData(senderPlayer.getUniqueId(), "team-leader", String.class, null);
        if (leaderName == null) {
            senderPlayer.sendMessage("§c队伍数据异常");
            return;
        }

        // 检查是否是队长自己
        if (leaderName.equals(senderPlayer.getName())) {
            senderPlayer.sendMessage("§c队长不允许退出队伍，请使用/yess team disband来解散队伍");
            return;
        }

        senderPlayer.sendMessage("§a你退出了队伍");

        // 更新玩家队伍状态
        storage.saveData(senderPlayer.getUniqueId(), "team-joined", false);
        storage.saveData(senderPlayer.getUniqueId(), "team-leader", null);
        storage.saveData(senderPlayer.getUniqueId(), "team-created", false);

        // 向所有在线的队伍成员发送玩家离开队伍的消息
        getAllOnlineTeamPlayers(leaderName).forEach(player -> {
            Player p = Bukkit.getPlayer(player);
            if (p != null) {
                p.sendMessage("§a成员 " + senderPlayer.getName() + " 退出了队伍");
            }
        });
    }

    public void disbandTeam(Player senderPlayer) {
        if (!hasTeam(senderPlayer)) {
            senderPlayer.sendMessage("§c你还未加入队伍");
            return;
        }
        String leaderName = storage.loadData(senderPlayer.getUniqueId(), "team-leader", String.class, null);
        if (leaderName == null || !leaderName.equals(senderPlayer.getName())) {
            senderPlayer.sendMessage("§c只有队长能够使用该命令");
            return;
        }
        senderPlayer.sendMessage("§a你已解散你的队伍");

        // 更新玩家队伍状态。解散队伍
        storage.saveData(senderPlayer.getUniqueId(), "team-created", false);
        storage.saveData(senderPlayer.getUniqueId(), "team-leader", null);
        storage.saveData(senderPlayer.getUniqueId(), "team-joined", false);


        // 向队伍的所有在线的玩家发送队伍解散的消息
        getAllOnlineTeamPlayers(senderPlayer.getName()).forEach(player -> {
            Player p = Bukkit.getPlayer(player);
            if (p != null) {
                p.sendMessage("§c队长 " + senderPlayer.getName() + " 已解散队伍");
            }
        });

        // 将队伍所有玩家，无论是否在线全部移出队伍。
        getAllTeamPlayers(leaderName).forEach(name -> {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
            UUID uuid = offlinePlayer.getUniqueId();
            storage.saveData(uuid, "team-joined", false);
            storage.saveData(uuid, "team-leader", null);
            storage.saveData(uuid, "team-created", false);
        });
    }

    public void removePlayer(Player senderPlayer, Player targetPlayer) {
        if (!hasTeam(senderPlayer)) {
            senderPlayer.sendMessage("§c你还未加入队伍");
            return;
        }
        String leaderName = storage.loadData(senderPlayer.getUniqueId(), "team-leader", String.class, null);
        if (leaderName == null || !leaderName.equals(senderPlayer.getName())) {
            senderPlayer.sendMessage("§c只有队长能够使用该命令");
            return;
        }
        if (!hasTeam(targetPlayer) || !storage.loadData(targetPlayer.getUniqueId(), "team-leader", String.class, null).equals(senderPlayer.getName())) {
            senderPlayer.sendMessage("§c无法操作不在自己队伍中的玩家");
            return;
        }
        if (senderPlayer == targetPlayer) {
            senderPlayer.sendMessage("§c你不能从队伍中移除自己");
            return;
        }

        // 更新玩家队伍状态。
        storage.saveData(targetPlayer.getUniqueId(), "team-joined", false);
        storage.saveData(targetPlayer.getUniqueId(), "team-leader", null);
        storage.saveData(targetPlayer.getUniqueId(), "team-created", false);
        targetPlayer.sendMessage("§a队长 " + senderPlayer.getName() + " 将你从队伍中移除");
        senderPlayer.sendMessage("§a你已将 " + targetPlayer.getName() + " 从队伍中移除");
    }

    public void promotePlayer(Player senderPlayer, Player targetPlayer) {
        if (!hasTeam(senderPlayer)) {
            senderPlayer.sendMessage("§c你还未加入队伍");
            return;
        }
        String leaderName = storage.loadData(senderPlayer.getUniqueId(), "team-leader", String.class, null);
        if (leaderName == null || !leaderName.equals(senderPlayer.getName())) {
            senderPlayer.sendMessage("§c只有队长能够使用该命令");
            return;
        }
        if (senderPlayer == targetPlayer) {
            senderPlayer.sendMessage("§c你不能提拔自己为队长！");
            return;
        }
        // 判断目标玩家是否在本队伍中
        String targetLeader = storage.loadData(targetPlayer.getUniqueId(), "team-leader", String.class, null);
        if (targetLeader == null || !targetLeader.equals(leaderName)) {
            senderPlayer.sendMessage("§c该玩家不在你的队伍中");
            return;
        }
        // 通知所有在线成员
        getAllOnlineTeamPlayers(leaderName).forEach(name -> {
            Player p = Bukkit.getPlayerExact(name);
            if (p != null) {
                p.sendMessage("§a队长 " + senderPlayer.getName() + " 将 " + targetPlayer.getName() + " 提升为队长");
                if (p.getName().equals(targetPlayer.getName())) {
                    p.sendMessage("§a你被 " + senderPlayer.getName() + " 提拔为队长");
                }
            }
        });
        // 更新数据
        storage.saveData(targetPlayer.getUniqueId(), "team-created", true);
        storage.saveData(targetPlayer.getUniqueId(), "team-leader", targetPlayer.getName());
        storage.saveData(targetPlayer.getUniqueId(), "team-joined", false);
        storage.saveData(senderPlayer.getUniqueId(), "team-created", false);
        storage.saveData(senderPlayer.getUniqueId(), "team-leader", targetPlayer.getName());
        storage.saveData(senderPlayer.getUniqueId(), "team-joined", true);
    }

    public void showTeam(Player senderPlayer) {
        if (!hasTeam(senderPlayer)) {
            senderPlayer.sendMessage("§c你没有加入队伍");
            return;
        }
        String leaderName = storage.loadData(senderPlayer.getUniqueId(), "team-leader", String.class, null);
        if (leaderName == null) {
            senderPlayer.sendMessage("§c队伍数据异常");
            return;
        }
        List<String> members = getAllTeamPlayers(leaderName);
        senderPlayer.sendMessage("§a队长: " + leaderName + "\n你的队伍成员: ");
        if (!members.isEmpty()) {
            senderPlayer.sendMessage(String.join(", ", members));
        } else senderPlayer.sendMessage("无");
    }

    /**
     * 获取当前队伍所有在线的玩家
     *
     * @param leaderName 队长名称（队伍唯一标识）
     * @return 返回 当前队伍所有在线的玩家
     */
    private List<String> getAllOnlineTeamPlayers(String leaderName) {
        List<String> onlineMembers = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            String l = storage.loadData(player.getUniqueId(), "team-leader", String.class, null);
            if (l != null && l.equals(leaderName)) {
                onlineMembers.add(player.getName());
            }
        }
        return onlineMembers;
    }

    /**
     * 获取队伍的所有玩家，无论是否在线
     *
     * @param leaderName 队长名称（队伍唯一标识）
     * @return 返回 当前队伍所有的玩家
     */
    private List<String> getAllTeamPlayers(String leaderName) {
        List<String> members = new ArrayList<>();

        File playerDataFile = new File(plugin.getDataFolder(), "data/player_data.yml");
        YamlConfiguration playerDataConfig = YamlConfiguration.loadConfiguration(playerDataFile);
        ConfigurationSection configurationSection = playerDataConfig.getConfigurationSection("player_data");

        if (configurationSection == null) return members;

        Set<String> keys = configurationSection.getKeys(false);
        for (String key : keys) {
            UUID uuid;
            try {
                uuid = UUID.fromString(key);
            } catch (Exception e) {
                continue;
            }
            String l = storage.loadData(uuid, "team-leader", String.class, null);
            if (l != null && l.equals(leaderName)) {
                String name = Bukkit.getOfflinePlayer(uuid).getName();
                if (name != null) {
                    members.add(name);
                }
            }
        }
        return members;
    }
}
