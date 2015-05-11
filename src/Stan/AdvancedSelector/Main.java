package Stan.AdvancedSelector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import Stan.AdvancedSelector.Enums.ClickMode;

public class Main extends JavaPlugin implements Listener{
	
	public static final Logger logger = Logger.getLogger("Minecraft");
	public static final String Prefix = "[AdvancedSelector] ";
	
	private ArrayList<Selector> Selectors;
	
	HashMap<String, YamlConfiguration> InventoryManager;
	
	public void onEnable(){
		registerEvents();
		registerCommands();
		
		loadConfig();
		loadSelectors();
		loadInventories();
		loadMetrics();
		
		logger.info("[" + getDescription().getName() + "] Enabled " + getDescription().getName() + " v" + getDescription().getVersion());
	}
	
	public void onDisable(){
		reloadConfig();
		saveConfig();
		
		logger.info("[" + getDescription().getName() + "] Disabled " + getDescription().getName() + " v" + getDescription().getVersion());
	}
	
	private void registerEvents(){
		getServer().getPluginManager().registerEvents(this, this);
	}

	private void registerCommands(){
		getCommand("AS").setExecutor(new Commands(this));
	}
	
	public void loadSelectors(){
		Selectors = new ArrayList<Selector>();
		for (File F : new File(getDataFolder().getPath() + File.separatorChar + "Selectors").listFiles()){
			Selectors.add(new Selector(YamlConfiguration.loadConfiguration(F)));
		}
	}
	
	public void loadInventories(){
		InventoryManager = new HashMap<String, YamlConfiguration>();
		for (File F : new File(getDataFolder().getPath() + File.separatorChar + "Inventories").listFiles()){
			InventoryManager.put(F.getName().replace(".yml", "").toLowerCase(), YamlConfiguration.loadConfiguration(F));
		}
	}

	private void loadConfig(){
		getConfig().options().copyDefaults(true);
		saveConfig();
		
		if (getConfig().getBoolean("DontChangeMe")){
			reloadConfig();
			getConfig().set("DontChangeMe", false);
			saveConfig();
			
			new File(getDataFolder().getPath() + File.separatorChar + "Selectors").mkdir();
			new File(getDataFolder().getPath() + File.separatorChar + "Inventories").mkdir();
			
			if (!new File(getDataFolder().getPath() + File.separatorChar + "Selectors" + File.separatorChar + "Example.yml").exists()){
				copy(getResource("Example.yml"), new File(getDataFolder().getPath() + File.separatorChar + "Selectors" + File.separatorChar + "Example.yml"));
			}
			if (!new File(getDataFolder().getPath() + File.separatorChar + "Inventories" + File.separatorChar + "Lol.yml").exists()){
				copy(getResource("Lol.yml"), new File(getDataFolder().getPath() + File.separatorChar + "Inventories" + File.separatorChar + "Lol.yml"));
			}
		}
	}

	private void loadMetrics(){
		try{Metrics metrics = new Metrics(this);metrics.start();}catch (IOException localIOException){}
	}
	
	private void copy(InputStream in, File file) {
	    try {
	        OutputStream out = new FileOutputStream(file);
	        byte[] buf = new byte[1024];
	        int len;
	        while((len=in.read(buf))>0){
	            out.write(buf,0,len);
	        }
	        out.close();
	        in.close();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	@EventHandler
	private void onMove(PlayerMoveEvent e){
		if (!e.isCancelled()){
			if (e.getTo().getBlock().getRelative(BlockFace.DOWN) != null){
				if (getConfig().getStringList("onWalkOverLocation") != null){
					ConfigurationSection CS = getConfig().getConfigurationSection("onWalkOverLocation");
					if (CS != null){
						for (String S : CS.getKeys(false)){
							if (e.getTo().getWorld().getName().equals(CS.getString(S + ".World"))){
								if (e.getTo().getBlockX() == CS.getInt(S + ".X")){
									if (e.getTo().getBlockZ() == CS.getInt(S + ".Z")){
										if (e.getTo().getBlockY() == CS.getInt(S + ".Y")){
											if (CS.getString(S + ".Permission").equalsIgnoreCase("None") || e.getPlayer().hasPermission(CS.getString(S + ".Permission"))){
												if (CS.getString(S + ".Type").equalsIgnoreCase("Selector")){
													giveSelector(CS.getString(S + ".Value"), e.getPlayer());
												}
												if (CS.getString(S + ".Type").equalsIgnoreCase("Inventory")){
													openInventory(CS.getString(S + ".Value"), e.getPlayer());
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	@EventHandler
	private void onInteract(PlayerInteractEvent e){
		if (e.getItem() != null){
			if (e.getItem().hasItemMeta()){
				if (e.getItem().getItemMeta().hasDisplayName()){
					
					for (Selector Selector : Selectors)
					{
						if (Functions.isSelector(e.getItem(), Selector))
						{
							e.setCancelled(true);
							if (e.getPlayer().hasPermission(Selector.Permission))
							{
								if (Selector.Clicks.equals(ClickMode.Both) || e.getAction().toString().toLowerCase().contains(Selector.Clicks.toString().toLowerCase()))
								{
									List<String> Values = new ArrayList<String>();
									for(String s : Selector.Value)
									{
										s = s.replace("{Player}", e.getPlayer().getName());
										Values.add(s);
									}
									
									switch(Selector.Type)
									{
										case Inventory:
											openInventory(Values.get(0), e.getPlayer());
											break;
											
										case ConsoleCommand:
											for (String Cmd : Values){
												getServer().dispatchCommand(getServer().getConsoleSender(), Cmd);
											}
											break;
											
										case PlayerCommand:
											for (String Cmd : Values){
												e.getPlayer().performCommand(Cmd);
											}
											break;
											
										case ConsoleMessage:
											for (String Cmd : Values){
												getServer().broadcastMessage(Functions.K(Cmd));
											}
											break;
											
										case PlayerMessage:
											for (String Cmd : Values){
												e.getPlayer().sendMessage(Functions.K(Cmd));
											}
											break;
											
										default:
											break;
									}
								}
							}
							else
							{
								if (Selector.NoPermission != "")
								{
									e.getPlayer().sendMessage(Selector.NoPermission);
								}
							}
						}	
					}
				}
			}
		}
		
		
		if (e.getClickedBlock() != null){
			if (getConfig().getStringList("onInteractWithLocation") != null){
				ConfigurationSection CS = getConfig().getConfigurationSection("onInteractWithLocation");
				if (CS != null){
					for (String S : CS.getKeys(false)){
						if (e.getClickedBlock().getWorld().getName().equals(CS.getString(S + ".World"))){
							if (e.getClickedBlock().getX() == CS.getInt(S + ".X")){
								if (e.getClickedBlock().getZ() == CS.getInt(S + ".Z")){
									if (e.getClickedBlock().getY() == CS.getInt(S + ".Y")){
										if (CS.getString(S + ".Permission").equalsIgnoreCase("None") || e.getPlayer().hasPermission(CS.getString(S + ".Permission"))){
											if ((e.getAction().toString().contains("LEFT") && CS.getBoolean(S + ".Clicks.Left")) || (e.getAction().toString().contains("RIGHT") && CS.getBoolean(S + ".Clicks.Right"))){
												e.setCancelled(true);
												if (CS.getString(S + ".Type").equalsIgnoreCase("Selector")){
													giveSelector(CS.getString(S + ".Value"), e.getPlayer());
												}
												if (CS.getString(S + ".Type").equalsIgnoreCase("Inventory")){
													openInventory(CS.getString(S + ".Value"), e.getPlayer());
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	private void preCmd(PlayerCommandPreprocessEvent e){
		if (e.getMessage() != null){
			String Msg = "";
			if (e.getMessage().contains(" ")){
				Msg = e.getMessage().split(" ")[0];
			}else{
				Msg = e.getMessage();
			}
			Msg = Msg.replace("/", "");
			
			if (getConfig().getStringList("onCommand") != null){
				ConfigurationSection CS = getConfig().getConfigurationSection("onCommand");
				if (CS != null){
					for (String S : CS.getKeys(false)){
						if (Msg.equalsIgnoreCase(CS.getString(S + ".Command"))){
							if (CS.getString(S + ".Permission").equalsIgnoreCase("None") || e.getPlayer().hasPermission(CS.getString(S + ".Permission"))){
								e.setCancelled(true);
								if (CS.getString(S + ".Type").equalsIgnoreCase("Selector")){
									giveSelector(CS.getString(S + ".Value"), e.getPlayer());
								}
								if (CS.getString(S + ".Type").equalsIgnoreCase("Inventory")){
									openInventory(CS.getString(S + ".Value"), e.getPlayer());
								}
							}
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	private void onJoin(PlayerJoinEvent e){
		ConfigurationSection CS = getConfig().getConfigurationSection("onJoin");
		if (CS != null){
			for (String S : CS.getKeys(false)){
				if (CS.getString(S + ".Permission").equalsIgnoreCase("None") || e.getPlayer().hasPermission(CS.getString(S + ".Permission"))){
					if (CS.getString(S + ".Type").equalsIgnoreCase("Selector")){
						giveSelector(CS.getString(S + ".Value"), e.getPlayer());
					}
					if (CS.getString(S + ".Type").equalsIgnoreCase("Inventory")){
						openInventory(CS.getString(S + ".Value"), e.getPlayer());
					}
				}
			}
		}
	}
	
	private void giveSelector(String Name, Player Speler){
		Selector Selector = getSelector(Name);
		if (Selector != null){
			Speler.getInventory().setItem(Selector.Spot, Selector.Item);
		}
	}
	
	@SuppressWarnings("deprecation")
	private void openInventory(String Name, Player Speler){
		if (getInventory(Name) != null){
			YamlConfiguration Inventory = getInventory(Name);
			
			int Size = Inventory.getInt("Size");
			Inventory Inv = getServer().createInventory(null, Size, Functions.K(Inventory.getString("Name")));
			
			for (int i = 1; i <= Size; i++){
				if (Inventory.getString("Items." + i) != null){
					ItemStack I = new ItemStack(Material.getMaterial(Inventory.getInt("Items." + i + ".ID")), 1, Short.parseShort("" + Inventory.getInt("Items." + i + ".Data")));
					ItemMeta M = I.getItemMeta();
					M.setDisplayName("" + Functions.K(Inventory.getString("Items." + i + ".Name")));
					M.setLore(Functions.K(Inventory.getStringList("Items." + i + ".Lore")));
					ConfigurationSection CS = Inventory.getConfigurationSection("Items." + i + ".Enchantments");
					if (CS != null){
						for (String S : CS.getKeys(false)){
							M.addEnchant(Enchantment.getByName(CS.getString(S + ".Name")), CS.getInt(S + ".Level"), true);
						}
					}
					I.setItemMeta(M);
					Inv.setItem(i-1, I);
				}
			}
			Speler.openInventory(Inv);
		}
	}

	@EventHandler
	private void onClick(InventoryClickEvent e){
		
		if (e.getCurrentItem() != null && e.getInventory() != null && e.getInventory().getName() != null){
			
			/*
			for (YamlConfiguration Selector : SelectorManager.values()){
				if (e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName() && ChatColor.stripColor(K(Selector.getString("Name"))).equalsIgnoreCase(ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName()))){
					e.setCancelled(true);
				}
				if (e.getClick().equals(ClickType.NUMBER_KEY)){
					if (e.getInventory().getItem(e.getHotbarButton()) != null && e.getInventory().getItem(e.getHotbarButton()).hasItemMeta() && e.getInventory().getItem(e.getHotbarButton()).getItemMeta().hasDisplayName() && ChatColor.stripColor(e.getInventory().getItem(e.getHotbarButton()).getItemMeta().getDisplayName()).equalsIgnoreCase(ChatColor.stripColor(K(Selector.getString("Name"))))){
						e.setCancelled(true);
					}
				}
			}
			*/
			
			if (e.getInventory().getType().equals(InventoryType.CHEST)){
				for (YamlConfiguration Inventory : InventoryManager.values()){
						
					if (Functions.K(Inventory.getString("Name")).equalsIgnoreCase(e.getInventory().getName())){
						e.setCancelled(true);
						
						if (Inventory.getBoolean("Clicks.Close")){
							e.getWhoClicked().closeInventory();
						}
						
						int Slot = e.getRawSlot()+1;
						if (Inventory.contains("Items." + Slot)){
							
							ConfigurationSection CS = Inventory.getConfigurationSection("Items." + Slot);
							
							if (CS.getString("InteractPermission").equalsIgnoreCase("None") || e.getWhoClicked().hasPermission(CS.getString("InteractPermission"))){
								
								String Mode = CS.getString("InteractType");
								List<String> Does = CS.getStringList("InteractValue");
									
								if (Mode.equalsIgnoreCase("Inventory")){
									openInventory(Does.get(0), (Player) e.getWhoClicked());
								}else
								if (Mode.equalsIgnoreCase("ConsoleCommand")){
									for (String Cmd : Does){
										Cmd = Cmd.replace("{Player}", e.getWhoClicked().getName());
										getServer().dispatchCommand(getServer().getConsoleSender(), Cmd);
									}
								}else
								if (Mode.equalsIgnoreCase("PlayerCommand")){
									for (String Cmd : Does){
										Cmd = Cmd.replace("{Player}", e.getWhoClicked().getName());
										((Player) e.getWhoClicked()).performCommand(Cmd);
									}
								}else
								if (Mode.equalsIgnoreCase("PlayerMessage")){
									for (String Cmd : CS.getStringList("InteractValue")){
										Cmd = Cmd.replace("{Player}", e.getWhoClicked().getName());
										((Player) e.getWhoClicked()).sendMessage(Functions.K(Cmd));
									}
								}else
								if (Mode.equalsIgnoreCase("ConsoleMessage")){
									for (String Cmd : CS.getStringList("InteractValue")){
										Cmd = Cmd.replace("{Player}", e.getWhoClicked().getName());
										getServer().broadcastMessage(Functions.K(Cmd));
									}
								}
							}else{
								((Player) e.getWhoClicked()).sendMessage(Functions.K(CS.getString("NoPermission")));
							}
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	private void onDrop(PlayerDropItemEvent e){
		if (!e.getPlayer().hasPermission("AdvancedSelector.Bypass")){
			if (e.getItemDrop() != null && e.getItemDrop().getItemStack() != null && e.getItemDrop().getItemStack().getAmount() != 0 && e.getItemDrop().getItemStack().hasItemMeta() && e.getItemDrop().getItemStack().getItemMeta().hasDisplayName()){
				for (Selector Selector : Selectors){
					if (Functions.isSelector(e.getItemDrop().getItemStack(), Selector)){
						e.setCancelled(true);
					}
				}
			}
		}
	}
	
	@EventHandler
	private void onDrag(InventoryDragEvent e){
		if (!e.getWhoClicked().hasPermission("AdvancedSelector.Bypass")){
			if (e.getInventory() != null && e.getInventory().getName() != null){
				for (YamlConfiguration Inventory : InventoryManager.values()){
					if (Functions.K(Inventory.getString("Name")).equalsIgnoreCase(e.getInventory().getName())){
						e.setCancelled(true);
					}
				}
			}
		}
	}
	
	private Selector getSelector(String Name){
		for (Selector Selector : Selectors)
		{
			if (ChatColor.stripColor(Functions.K(Selector.Item.getItemMeta().getDisplayName())).equalsIgnoreCase(Name))
			{
				return Selector;
			}
		}
		return null;
	}
	
	private YamlConfiguration getInventory(String Name){
		return InventoryManager.get(Name.toLowerCase());
	}
}