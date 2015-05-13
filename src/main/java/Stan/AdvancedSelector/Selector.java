package Stan.AdvancedSelector;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import Stan.AdvancedSelector.Enums.ClickMode;
import Stan.AdvancedSelector.Enums.InteractType;

public class Selector {

	public String YCName;
	
	public ItemStack Item;
	public String Permission;
	public String NoPermission;
	public InteractType Type;
	public List<String> Value;
	public ClickMode Clicks;
	public int Spot;
	
	@SuppressWarnings("deprecation")
	public Selector(YamlConfiguration YC)
	{
		YCName = YC.getName();
		
		int ID = 1;
		short Data = 0;
		int Amount = 1;
		
		if (YC.contains("ID"))
		{
			try{
				ID = YC.getInt("ID");
			}catch(Exception e){
				logError("ID");
			}
		}
		
		if (YC.contains("Data"))
		{
			try{
				Data = (short)YC.getInt("Data");
			}catch(Exception e){
				logError("Data");
			}
		}
		
		if (YC.contains("Amount"))
		{
			try{
				Amount = YC.getInt("Amount");
			}catch(Exception e){
				logError("Amount");
			}
		}
		
		ItemMeta Meta = null;
		
		try{
			Item = new ItemStack(ID, Amount, Data);
			Meta = Item.getItemMeta();
		}catch(Exception e){
			logError("Item (ID:Data not found)");
		}
		
		if (Meta != null)
		{
			if (YC.contains("Name"))
			{
				try{
					Meta.setDisplayName(Functions.K(YC.getString("Name")));
				}catch(Exception e){
					logError("Name");
				}
			}
			
			if (YC.contains("Lore"))
			{
				try{
					Meta.setLore(Functions.K(YC.getStringList("Lore")));
				}catch(Exception e){
					logError("Lore");
				}
			}
			
			if (YC.contains("Enchantments"))
			{
				try{
					ConfigurationSection CS = YC.getConfigurationSection("Enchantments");
					if (CS != null){
						for (String S : CS.getKeys(false)){
							Meta.addEnchant(Enchantment.getByName(CS.getString(S + ".Name")), CS.getInt(S + ".Level"), true);
						}
					}
				}catch(Exception e){
					logError("Enchantments");
				}
			}
			
			Item.setItemMeta(Meta);
		}
		
		if (YC.contains("InteractPermission"))
		{
			try{
				Permission = YC.getString("InteractPermission");
			}catch(Exception e){
				logError("InteractPermission");
				Permission = "";
			}
		}
		else
		{
			Permission = "";
		}
		
		if (YC.contains("NoPermission"))
		{
			try{
				NoPermission = Functions.K(YC.getString("NoPermission"));
			}catch(Exception e){
				logError("NoPermission");
				NoPermission = "";
			}
		}
		else
		{
			NoPermission = "";
		}
		
		if (YC.contains("InteractType"))
		{
			try{
				Type = Enum.valueOf(InteractType.class, YC.getString("InteractType"));
			}catch(Exception e){
				logError("InteractType");
				Type = InteractType.PlayerMessage;
			}
		}
		else
		{
			Type = InteractType.PlayerMessage;
		}
		
		if (YC.contains("InteractValue"))
		{
			try{
				Value = YC.getStringList("InteractValue");
			}catch(Exception e){
				logError("InteractValue");
				Value = new ArrayList<String>();
				Value.add("Hi {Player}! :)");
			}
		}
		else
		{
			Value = new ArrayList<String>();
			Value.add("Hi {Player}! :)");
		}
		
		if (YC.contains("Clicks"))
		{
			try{
				if ((YC.contains("Clicks.Left") && YC.getBoolean("Clicks.Left")) && (YC.contains("Clicks.Right") && YC.getBoolean("Clicks.Right")))
				{
					Clicks = ClickMode.Both;
				}else
				if (YC.contains("Clicks.Left") && YC.getBoolean("Clicks.Left"))
				{
					Clicks = ClickMode.Left;
				}else
				if (YC.contains("Clicks.Right") && YC.getBoolean("Clicks.Right"))
				{
					Clicks = ClickMode.Right;
				}else{
					Clicks = ClickMode.Left;
				}
			}catch(Exception e){
				logError("Clicks");
				Clicks = ClickMode.Left;
			}
		}
		else
		{
			Clicks = ClickMode.Left;
		}
		
		if (YC.contains("Spot"))
		{
			try{
				Amount = YC.getInt("Spot")-1;
			}catch(Exception e){
				logError("Spot");
			}
		}else
		{
			Spot = 0;
		}
	}
	
	public void logError(String s)
	{
		Main.logger.info(Main.Prefix + "(Selector) Could not load '" + s + "' from " + YCName);
	}
}
