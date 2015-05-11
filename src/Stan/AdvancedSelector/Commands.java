package Stan.AdvancedSelector;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Commands implements CommandExecutor {

	Main P;
	public Commands(Main M) {
		P = M;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		
		if (sender.hasPermission("AdvancedSelector.Admin")){
			if (args.length == 1){
				if (args[0].equalsIgnoreCase("Reload")){
					P.loadInventories();
					P.loadSelectors();
					
					P.reloadConfig();
					P.saveConfig();
					
					sender.sendMessage(ChatColor.GREEN + "[AdvancedSelector] Reloaded All Config Files.");
				}
			}
		}
		return false;
	}
}