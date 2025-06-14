package cn.yvmou.yess.managers;

import cn.yvmou.yess.Y;
import cn.yvmou.yess.storage.PlayerDataStorage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class TeamManager {
    private final Y plugin;
    private final PlayerDataStorage playerDataStorage;

    public TeamManager(Y plugin, PlayerDataStorage playerDataStorage) {
        this.plugin = plugin;
        this.playerDataStorage = playerDataStorage;
    }

    /**
     * 玩家是否被邀请
     * @param player 玩家对象
     * @return true: 已经被邀请, false: 未被邀请
     */
    private boolean isInvited(Player player) {
        return playerDataStorage.isInvited(player);
    }

    /**
     * 玩家是否已经加入一个队伍，会发送信息
     * @param player 玩家对象
     * @return true: 已经加入队伍, false: 未加入队伍
     */
    private boolean senderIsTeamed(Player player) {
        String teamStatus = playerDataStorage.getPlayerData(player);
        if (teamStatus != null) {
            if (isInvited(player)) player.sendMessage("§c你有一个待处理的队伍邀请！");
            else player.sendMessage("§c你已经加入了一个队伍！");
            return true;
        }
        return false;
    }

    /**
     * 玩家是否已经加入一个队伍，会发送信息
     * @param senderPlayer 发送命令的玩家对象
     * @param targetPlayer 被邀请的玩家对象
     * @return true: 已经加入队伍, false: 未加入队伍
     */
    private boolean targetIsTeamed(Player senderPlayer, Player targetPlayer) {
        String teamStatus = playerDataStorage.getPlayerData(targetPlayer);
        if (teamStatus != null) {
            if (isInvited(targetPlayer)) senderPlayer.sendMessage("§c该玩家已经被其他队伍邀请了！");
            else senderPlayer.sendMessage("§c该玩家已经加入了一个队伍！");
            return true;
        }
        return false;
    }
    /**
     * 玩家是否已经加入一个队伍，仅判断
     * @param player 需要判断的玩家对象
     * @return true: 已经加入队伍, false: 未加入队伍
     */
    private boolean hasTeam(Player player) {
        String teamStatus = playerDataStorage.getPlayerData(player);
        if (teamStatus != null && teamStatus.contains("-invited")) return false;
        else return teamStatus != null;
    }

    private void removePrefix(Player player) {
        player.setDisplayName(player.getName());
        player.setPlayerListName(player.getName());
    }

    /**
     * 创建队伍
     * @param senderPlayer 发送命令的玩家对象
     */
    public void createTeam(Player senderPlayer) {
        if (senderIsTeamed(senderPlayer)) return;
        playerDataStorage.setPlayerData(senderPlayer, senderPlayer, false);
        senderPlayer.sendMessage("§a你已成功创建一个队伍");
    }

    public void inviteTeam(Player senderPlayer, Player targetPlayer) {
        if (targetIsTeamed(senderPlayer, targetPlayer)) return;
        targetPlayer.sendMessage("§a* " + senderPlayer.getName() + " 邀请你加入他的队伍\n" +
                "输入/yess team accept接收邀请，/yess team deny拒绝邀请\n2分钟内有效");
        senderPlayer.sendMessage("§a你已向" + targetPlayer.getName() + "发送邀请");
        playerDataStorage.setPlayerData(senderPlayer, targetPlayer, true);
        UUID targetPlayerId = targetPlayer.getUniqueId();
        new BukkitRunnable()  {
            @Override
            public void run() {
                if (playerDataStorage.isInvited(targetPlayer)) {
                    if (Bukkit.getServer().getOnlinePlayers().contains(targetPlayer)) {
                        playerDataStorage.removePlayerData(targetPlayer);
                    } else playerDataStorage.removePlayerData(Bukkit.getServer().getOfflinePlayer(targetPlayerId));
                }
            }
        }.runTaskLater(plugin, 20 * 120);
    }

    public void removePlayer(Player senderPlayer, Player targetPlayer) {
        if (!hasTeam(senderPlayer) ||
                !playerDataStorage.getPlayerData(senderPlayer).equalsIgnoreCase(senderPlayer.getName())) {
            senderPlayer.sendMessage("§c只有队长能够使用该命令");
            return;
        }

        if (!hasTeam(targetPlayer) ||
                !playerDataStorage.getPlayerData(targetPlayer).equalsIgnoreCase(senderPlayer.getName())) {
            senderPlayer.sendMessage("§c无法操作不在自己队伍中的玩家");
            return;
        }

        playerDataStorage.removePlayerData(targetPlayer);
        removePrefix(targetPlayer);
        targetPlayer.sendMessage("§a* " + senderPlayer.getName() + "将你从队伍中移除");
        senderPlayer.sendMessage("§a你已将" + targetPlayer.getName() + "从队伍中移除");
    }

    public void promotePlayer(Player senderPlayer, Player targetPlayer) {
        if (!hasTeam(senderPlayer) ||
                !playerDataStorage.getPlayerData(senderPlayer).equalsIgnoreCase(senderPlayer.getName())) {
            senderPlayer.sendMessage("§c只有队长能够使用该命令");
            return;
        }

        List<Player> members = new ArrayList<>();

        for (Player member : plugin.getServer().getOnlinePlayers()) {
            String memberData = playerDataStorage.getPlayerData(member);
            if (memberData != null && memberData.equalsIgnoreCase(senderPlayer.getName())) {
                members.add(member);
            }
        }

        if (!members.contains(targetPlayer)) {
            senderPlayer.sendMessage("§c该玩家不在线或不在队伍中");
            return;
        }

        for (Player member : members) {
            member.sendMessage("§a" + senderPlayer + "将" + targetPlayer + "提升为队长");
            if (member.getName().equalsIgnoreCase(targetPlayer.getName())) member.sendMessage("§a你被" + senderPlayer + "提升为队长");
            playerDataStorage.removePlayerData(member);
            playerDataStorage.setPlayerData(targetPlayer, member, false);
        }

    }

    public void disbandTeam(Player senderPlayer) {
        if (!hasTeam(senderPlayer) ||
                !playerDataStorage.getPlayerData(senderPlayer).equalsIgnoreCase(senderPlayer.getName())) {
            senderPlayer.sendMessage("§c只有队长能够使用该命令");
            return;
        }

        playerDataStorage.removePlayerData(senderPlayer);
        senderPlayer.sendMessage("§a你已解散你的队伍");

        List<Player> onlineMembers = new ArrayList<>();
        List<OfflinePlayer> offlineMembers = new ArrayList<>();

        for (Player member : plugin.getServer().getOnlinePlayers()) {
            String memberData = playerDataStorage.getPlayerData(member);
            if (memberData != null && memberData.equalsIgnoreCase(senderPlayer.getName())) {
                onlineMembers.add(member);
            }
        }
        for (OfflinePlayer member : plugin.getServer().getOfflinePlayers()) {
            String memberData = playerDataStorage.getPlayerData(member);
            if (memberData != null && memberData.equalsIgnoreCase(senderPlayer.getName())) {
                offlineMembers.add(member);
            }
        }
        for (Player member : onlineMembers) {
            member.sendMessage("§a" + senderPlayer + "已解散你的队伍");
            playerDataStorage.removePlayerData(member);
            removePrefix(member);
        }
        for (OfflinePlayer member : offlineMembers) {
            playerDataStorage.removePlayerData(member);
        }
    }

    public void showTeam(Player senderPlayer) {
        Set<String> members = new HashSet<>();
        
        // 检查发送者是否在队伍中
        if (!hasTeam(senderPlayer)) {
            senderPlayer.sendMessage("§c你没有加入队伍");
            return;
        }
        
        String teamLeader = playerDataStorage.getPlayerData(senderPlayer);
        
        for (Player member : plugin.getServer().getOnlinePlayers()) {
            String memberData = playerDataStorage.getPlayerData(member);
            if (memberData != null && memberData.equalsIgnoreCase(teamLeader)) {
                members.add(member.getName());
            }
        }
        for (OfflinePlayer member : plugin.getServer().getOfflinePlayers()) {
            String memberData = playerDataStorage.getPlayerData(member);
            if (memberData != null && memberData.equalsIgnoreCase(teamLeader)) {
                members.add(member.getName());
            }
        }

        senderPlayer.sendMessage("§a你的队伍成员: ");
        for (String member : members) {
            senderPlayer.sendMessage("§b" + member);
        }

    }

    public void acceptTeam(Player senderPlayer) {
        if (hasTeam(senderPlayer)) {
            senderPlayer.sendMessage("§c你已经加入一个队伍了");
            return;
        } else if (!isInvited(senderPlayer)) {
            senderPlayer.sendMessage("§c你没有被邀请");
            return;
        }

        // 提取队长名称
        String playerData = playerDataStorage.getPlayerData(senderPlayer);
        if (playerData == null || !playerData.contains("-invited")) {
            senderPlayer.sendMessage("§c邀请数据无效");
            return;
        }
        
        String leader_name = playerData.split("-")[0];
        leader_name = leader_name.split("=")[1].replace("}", "");
        senderPlayer.sendMessage("§a你已接受" + leader_name + "的邀请");
        Player leader = plugin.getServer().getPlayer(leader_name);
        senderPlayer.sendMessage(String.valueOf(leader));
        if (leader == null) {
            senderPlayer.sendMessage("§c队长不在线，无法加入队伍");
            return;
        }

        // 移除被邀请状态
        playerDataStorage.removePlayerData(senderPlayer);

        for (Player member : plugin.getServer().getOnlinePlayers()) {
            String memberData = playerDataStorage.getPlayerData(member);
            if (memberData != null && memberData.equalsIgnoreCase(leader_name)) {
                member.sendMessage("§a" + senderPlayer.getName() + "加入了队伍");
            }
        }

        // 加入队伍
        playerDataStorage.setPlayerData(leader, senderPlayer, false);
    }

    public void denyTeam(Player senderPlayer) {
        if (!isInvited(senderPlayer)) {
            senderPlayer.sendMessage("§c你没有被邀请");
            return;
        }

        // 提取队长名称
        String playerData = playerDataStorage.getPlayerData(senderPlayer);
        if (playerData == null || !playerData.contains("-invited")) {
            senderPlayer.sendMessage("§c邀请数据无效");
            return;
        }
        
        String leader = playerData.split("-")[0];

        // 移除被邀请状态
        playerDataStorage.removePlayerData(senderPlayer);

        senderPlayer.sendMessage("§c你拒绝了" + leader + "的邀请");
        Player leaderPlayer = plugin.getServer().getPlayer(leader);
        if (leaderPlayer != null) leaderPlayer.sendMessage("§c" + senderPlayer.getName() + "拒绝了你的邀请");
    }

    public void leaveTeam(Player senderPlayer) {
        if (!hasTeam(senderPlayer)) {
            senderPlayer.sendMessage("§c你没有加入队伍");
            return;
        }

        // 提取队长名称
        String leader = playerDataStorage.getPlayerData(senderPlayer);
        
        // 检查是否是队长自己
        if (leader != null && leader.equalsIgnoreCase(senderPlayer.getName())) {
            senderPlayer.sendMessage("§c队长不允许退出队伍，请使用/yess team disband来解散队伍");
            return;
        }

        playerDataStorage.removePlayerData(senderPlayer);

        senderPlayer.sendMessage("§a你退出了队伍");
        for (Player member : plugin.getServer().getOnlinePlayers()) {
            String memberData = playerDataStorage.getPlayerData(member);
            if (memberData != null && memberData.equalsIgnoreCase(leader)) {
                member.sendMessage("§a" + senderPlayer.getName() + "退出了队伍");
            }
        }
    }

    public void setPrefix(Player senderPlayer, String p) {
        String prefix;
        if (!hasTeam(senderPlayer) ||
                !playerDataStorage.getPlayerData(senderPlayer).equalsIgnoreCase(senderPlayer.getName())) {
            senderPlayer.sendMessage("§c只有队长能够使用该命令");
            return;
        }

        List<Player> members = new ArrayList<>();

        for (Player member : plugin.getServer().getOnlinePlayers()) {
            String memberData = playerDataStorage.getPlayerData(member);
            if (memberData != null && memberData.equalsIgnoreCase(senderPlayer.getName())) {
                members.add(member);
            }
        }

        if (p.length() > 10) {
            senderPlayer.sendMessage("§c前缀过长！不能超过10");
            return;
        }

        for (Player member : members) {
            prefix = ChatColor.translateAlternateColorCodes('&', p);
            member.setDisplayName(prefix + member.getName());
            member.setPlayerListName(prefix + member.getName());
            playerDataStorage.setPlayerData(senderPlayer, member, prefix);
        }
    }
}
