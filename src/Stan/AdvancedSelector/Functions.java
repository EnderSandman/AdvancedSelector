package Stan.AdvancedSelector;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

public class Functions {	
	public static String K(String s){
		if (s.contains("&")){
			s = s.replace("&1", "" + ChatColor.DARK_BLUE);
			s = s.replace("&2", "" + ChatColor.DARK_GREEN);
			s = s.replace("&3", "" + ChatColor.DARK_AQUA);
			s = s.replace("&4", "" + ChatColor.DARK_RED);
			s = s.replace("&5", "" + ChatColor.DARK_PURPLE);
			s = s.replace("&6", "" + ChatColor.GOLD);
			s = s.replace("&7", "" + ChatColor.GRAY);
			s = s.replace("&8", "" + ChatColor.DARK_GRAY);
			s = s.replace("&9", "" + ChatColor.BLUE);
			s = s.replace("&0", "" + ChatColor.BLACK);
			s = s.replace("&a", "" + ChatColor.GREEN);
			s = s.replace("&b", "" + ChatColor.AQUA);
			s = s.replace("&c", "" + ChatColor.RED);
			s = s.replace("&d", "" + ChatColor.LIGHT_PURPLE);
			s = s.replace("&e", "" + ChatColor.YELLOW);
			s = s.replace("&f", "" + ChatColor.WHITE);
			s = s.replace("&r", "" + ChatColor.RESET);
			s = s.replace("&l", "" + ChatColor.BOLD);
			s = s.replace("&n", "" + ChatColor.UNDERLINE);
			s = s.replace("&o", "" + ChatColor.ITALIC);
			s = s.replace("&m", "" + ChatColor.STRIKETHROUGH);
			s = s.replace("&k", "" + ChatColor.MAGIC);
		}
		return s;
	}
	
	public static List<String> K(List<String> s){
		ArrayList<String> List = new ArrayList<String>();
		for (String S : s){
			List.add(K(S));
		}
		return List;
	}
	
	@SuppressWarnings("deprecation")
	public static boolean isSelector(ItemStack Item, Selector Selector)
	{
		if (Item.getTypeId() == Selector.Item.getTypeId())
		{
			if (Item.getData().getData() == Selector.Item.getData().getData())
			{
				if (Item.getItemMeta().getDisplayName() == Selector.Item.getItemMeta().getDisplayName())
				{
					return true;
				}
			}
		}
		return false;
	}
}
