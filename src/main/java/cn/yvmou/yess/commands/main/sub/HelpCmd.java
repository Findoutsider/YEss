package cn.yvmou.yess.commands.main.sub;

import cn.yvmou.yess.Y;
import cn.yvmou.yess.commands.SubCommand;
import cn.yvmou.yess.utils.CommandUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class HelpCmd implements SubCommand {
    private final Y plugin;

    public HelpCmd(Y plugin) { this.plugin = plugin; }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (CommandUtils.noPermission(sender, this)) return false;

        seedHelpMessage(sender);
        return true;
    }

    @Override
    public String getUsage() {
        return "/yess help";
    }

    @Override
    public String requirePermission(CommandSender sender) {
        return plugin.getConfig().getString("registerCommand.help.permission", "yess.command.help");
    }

    @Override
    public boolean requireRegister() {
        return plugin.getConfig().getBoolean("registerCommand.help.enable", false);
    }

    public void seedHelpMessage(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "===== " + plugin.getDescription().getName() + " v" + plugin.getDescription().getVersion() + " 帮助 =====");
        sender.sendMessage(ChatColor.WHITE + "/" + plugin.getDescription().getName().toLowerCase() + " help");
        sender.sendMessage(ChatColor.WHITE + "/" + plugin.getDescription().getName().toLowerCase() + " reload");
        sender.sendMessage(ChatColor.WHITE + "/" + plugin.getDescription().getName().toLowerCase() + " craft <player>");
        sender.sendMessage(ChatColor.WHITE + "/" + plugin.getDescription().getName().toLowerCase() + " ec <player>");
        sender.sendMessage(ChatColor.WHITE + "/" + plugin.getDescription().getName().toLowerCase() + " glow <player>");
        sender.sendMessage(ChatColor.WHITE + "/" + plugin.getDescription().getName().toLowerCase() + " gift <create|edit|delete|give|help>");
    }
}
