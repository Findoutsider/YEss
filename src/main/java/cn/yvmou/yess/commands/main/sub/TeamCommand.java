package cn.yvmou.yess.commands.main.sub;

import cn.yvmou.yess.Y;
import cn.yvmou.yess.commands.SubCommand;
import cn.yvmou.yess.managers.TeamManager;
import cn.yvmou.yess.utils.CommandUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class TeamCommand implements SubCommand {
    private final Y plugin;
    private final TeamManager teamManager = Y.getTeamManager();

    public TeamCommand(Y plugin) {
        this.plugin = plugin;
    }

    /**
     * 执行子命令逻辑
     *
     * @param sender Command sender
     * @param args Arguments passed to the command
     * @return true if successful, false otherwise
     */
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (CommandUtils.noPlayer(sender)) return false;
        if (CommandUtils.noPermission(sender, this)) return false;

        Player player = (Player) sender;

        if (args.length < 2) {
            sendHelp(player);
            return false;
        }

        String subCommand = args[1].toLowerCase();

        if (subCommand.equalsIgnoreCase("invite") ||
                subCommand.equalsIgnoreCase("remove") ||
                subCommand.equalsIgnoreCase("promote")) {
            if (args.length != 3) {
                sender.sendMessage(ChatColor.RED + "§c用法错误！请输入 /yess team " + subCommand + " <玩家>");
                return false;
            }
        }

        switch (subCommand) {
            case "create" -> teamManager.createTeam(player);
            case "invite" -> teamManager.inviteTeam(player, plugin.getServer().getPlayer(args[2]));
            case "remove" -> teamManager.removePlayer(player, plugin.getServer().getPlayer(args[2]));
            case "promote" -> teamManager.promotePlayer(player, plugin.getServer().getPlayer(args[2]));
            case "disband" -> teamManager.disbandTeam(player);
            case "prefix" -> teamManager.setPrefix(player, args[2]);
            case "show" -> teamManager.showTeam(player);
            case "leave" -> teamManager.leaveTeam(player);
            case "accept" -> teamManager.acceptTeam(player);
            case "deny" -> teamManager.denyTeam(player);
            default -> sendHelp(player);
        }

        return false;
    }

    /**
     * 提供子命令的使用说明
     *
     * @return A string representing command usage
     */
    @Override
    public String getUsage() {
        return "/yess team help";
    }

    /**
     * 检查发送者是否有权限使用子命令.
     *
     * @param sender The command sender
     * @return True if the sender has permission
     */
    @Override
    public String requirePermission(CommandSender sender) {
        return plugin.getConfig().getString("registerCommand.team.permission", "yess.command.team");
    }

    /**
     * 是否注册命令
     *
     * @return 注册命令返回是，否则否
     */
    @Override
    public Boolean requireRegister() {
        return plugin.getConfig().getBoolean("registerCommand.team.enable", true);
    }

    private void sendHelp(Player player) {
        player.sendMessage("§6=== 队伍命令帮助 ===");
        player.sendMessage("§a--- 队长命令 ---");
        player.sendMessage("§f/yess team create §7- 创建队伍");
        player.sendMessage("§f/yess team invite <玩家> §7- 邀请玩家加入队伍");
        player.sendMessage("§f/yess team remove <玩家> §7- 将玩家移出队伍");
        player.sendMessage("§f/yess team promote <玩家> §7- 将队长转让给玩家");
        player.sendMessage("§f/yess team disband §7- 解散队伍");
        player.sendMessage("§f/yess team prefix §7- 更改队员前缀");
        player.sendMessage("§b--- 队员命令 ---");
        player.sendMessage("§f/yess team show §7- 预览当前队伍");
        player.sendMessage("§f/yess team leave §7- 离开当前队伍");
        player.sendMessage("§f/yess team accept §7- 同意加入队伍");
        player.sendMessage("§f/yess team deny §7- 拒绝加入队伍");
    }
}
