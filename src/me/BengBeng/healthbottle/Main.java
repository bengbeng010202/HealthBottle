package me.BengBeng.healthbottle;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class Main
	extends JavaPlugin
	implements CommandExecutor, Listener {
	
	public Plugin plugin;
	
	private List<String> clicked = new ArrayList<String>();
	private List<String> used = new ArrayList<String>();
	
	public FileConfiguration config;
	
	public Plugin getPlugin() {
		return plugin;
	}
	
	@Override
	public void onEnable() {
		plugin = this;
		
		Config.loadConfigFile();
		config = Config.getConfigFile();
		
		getCommand("healthbottle").setExecutor(this);
		
		getServer().getPluginManager().registerEvents(this, this);
		
	}
	
	@Override
	public void onDisable() {}
	
	/*
	 * CÂU LỆNH CỦA PLUGIN:
	 */
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		String noPerm = config.getString("MESSAGE.FAIL.no-permission");
		String tooMany = config.getString("MESSAGE.FAIL.too-many-args");
		
		String labelRegex = "(\\{label}|\\%label%)";
		
		if(args.length == 0) {
			if((!sender.hasPermission("healthbottle.admin")) && (!sender.hasPermission("healthbottle.command.help"))) {
				sendMessage(sender, noPerm);
				return true;
			}
			
			sendMessage(sender, config.getString("HELP.header"));
			if((sender.hasPermission("healthbottle.admin")) || (sender.hasPermission("healthbottle.command.help"))) {
				sendMessage(sender, config.getString("HELP.help").replaceAll(labelRegex, label));
			}
			if((sender.hasPermission("healthbottle.admin")) || (sender.hasPermission("healthbottle.command.give"))) {
				sendMessage(sender, config.getString("HELP.give").replaceAll(labelRegex, label));
			}
			if((sender.hasPermission("healthbottle.admin")) || (sender.hasPermission("healthbottle.command.list"))) {
				sendMessage(sender, config.getString("HELP.list").replaceAll(labelRegex, label));
			}
			if((sender.hasPermission("healthbottle.admin")) || (sender.hasPermission("healthbottle.command.reload"))) {
				sendMessage(sender, config.getString("HELP.reload").replaceAll(labelRegex, label));
			}
			sendMessage(sender, config.getString("HELP.footer"));
			
			return true;
		}
		
		String cmd = args[0];
		
		if(cmd.matches("(?ium)(help|\\?)")) {
			if((!sender.hasPermission("healthbottle.admin")) && (!sender.hasPermission("healthbottle.command.help"))) {
				sendMessage(sender, noPerm);
				return true;
			}
			
			sendMessage(sender, config.getString("HELP.header"));
			if((sender.hasPermission("healthbottle.admin")) || (sender.hasPermission("healthbottle.command.help"))) {
				sendMessage(sender, config.getString("HELP.help").replaceAll(labelRegex, label));
			}
			if((sender.hasPermission("healthbottle.admin")) || (sender.hasPermission("healthbottle.command.give"))) {
				sendMessage(sender, config.getString("HELP.give").replaceAll(labelRegex, label));
			}
			if((sender.hasPermission("healthbottle.admin")) || (sender.hasPermission("healthbottle.command.list"))) {
				sendMessage(sender, config.getString("HELP.list").replaceAll(labelRegex, label));
			}
			if((sender.hasPermission("healthbottle.admin")) || (sender.hasPermission("healthbottle.command.reload"))) {
				sendMessage(sender, config.getString("HELP.reload").replaceAll(labelRegex, label));
			}
			sendMessage(sender, config.getString("HELP.footer"));		
			
			return true;
		}
		
		if(cmd.matches("(?ium)(give|get|load)")) {
			if((!sender.hasPermission("healthbottle.admin")) && (!sender.hasPermission("healthbottle.command.give"))) {
				sendMessage(sender, noPerm);
				return true;
			}
			
			if(args.length == 1) {
				sendMessage(sender, config.getString("MESSAGE.FAIL.type-bottle-name"));
				return true;
			}
			if(args.length == 2) {
				String bottleName = String.valueOf(args[1]);
				
				if(!isExist(bottleName)) {
					sendMessage(sender, config.getString("MESSAGE.FAIL.bottle-not-exist").replaceAll("(\\{value}|\\%value%)", args[1]));
					return true;
				}
				
				if(!(sender instanceof Player)) {
					sendMessage(sender, config.getString("MESSAGE.FAIL.Console.must-specify-player"));
					return true;
				}
				
				Player player = (Player)sender;
				
				ItemStack item = getItem(bottleName);
				giveItem(player, item);
				
				sendMessage(player, config.getString("MESSAGE.SUCCESS.Give.self").replaceAll("(\\{name}|\\%name%)", getItemName(bottleName)));
				
				return true;
			}
			if(args.length == 3) {
				String bottleName = String.valueOf(args[1]);
				
				if(!isExist(bottleName)) {
					sendMessage(sender, config.getString("MESSAGE.FAIL.bottle-not-exist").replaceAll("(\\{value}|\\%value%)", args[1]));
					return true;
				}
				
				Player target = Bukkit.getServer().getPlayer(args[2]);
				if((target == null) || (!target.isOnline())) {
					sendMessage(sender, config.getString("MESSAGE.FAIL.player-not-found").replaceAll("(\\{value}|\\%value%)", args[2]));
					return true;
				}
				
				ItemStack item = getItem(bottleName);
				giveItem(target, item);
				
				sendMessage(sender, config.getString("MESSAGE.SUCCESS.Give.sender").replaceAll("(\\{player}|\\%player%)", target.getName()).replaceAll("(\\{amount}|\\%amount%)", "1").replaceAll("(\\{name}|\\%name%)", getItemName(bottleName)));
				sendMessage(sender, config.getString("MESSAGE.SUCCESS.Give.target").replaceAll("(\\{sender}|\\%sender%)", sender.getName()).replaceAll("(\\{amount}|\\%amount%)", "1").replaceAll("(\\{name}|\\%name%)", getItemName(bottleName)));
				
				return true;
			}
			if(args.length == 4) {
				String bottleName = String.valueOf(args[1]);
				
				if(!isExist(bottleName)) {
					sendMessage(sender, config.getString("MESSAGE.FAIL.bottle-not-exist").replaceAll("(\\{value}|\\%value%)", args[1]));
					return true;
				}
				
				Player target = Bukkit.getServer().getPlayer(args[2]);
				if((target == null) || (!target.isOnline())) {
					sendMessage(sender, config.getString("MESSAGE.FAIL.player-not-found").replaceAll("(\\{value}|\\%value%)", args[2]));
					return true;
				}
				
				if(!isInt(args[3])) {
					sendMessage(sender, config.getString("MESSAGE.FAIL.not-number").replaceAll("(\\{value}|\\%value%)", args[3]));
					return true;
				}
				
				int amount = Integer.parseInt(args[3]);
				
				ItemStack item = getItem(bottleName, amount);
				giveItem(target, item);
				
				sendMessage(sender, config.getString("MESSAGE.SUCCESS.Give.sender").replaceAll("(\\{player}|\\%player%)", target.getName()).replaceAll("(\\{amount}|\\%amount%)", String.valueOf(amount)).replaceAll("(\\{name}|\\%name%)", getItemName(bottleName)));
				sendMessage(sender, config.getString("MESSAGE.SUCCESS.Give.target").replaceAll("(\\{sender}|\\%sender%)", sender.getName()).replaceAll("(\\{amount}|\\%amount%)", String.valueOf(amount)).replaceAll("(\\{name}|\\%name%)", getItemName(bottleName)));
				
				return true;
			}
			if(args.length > 4) {
				sendMessage(sender, tooMany);
				return true;
			}
			
			return true;
		}
		if(cmd.matches("(?ium)(list)")) {
			if((!sender.hasPermission("healthbottle.admin")) && (!sender.hasPermission("healthbottle.command.list"))) {
				sendMessage(sender, noPerm);
				return true;
			}
			
			if(!hasAnyItem()) {
				sendMessage(sender, config.getString("MESSAGE.FAIL.no-item-available"));
				return true;
			}
			
			String list = getKeyList().toString().replaceAll("(\\[|\\])", "");
			String size = String.valueOf(getSize());
			
			sendMessage(sender, config.getString("MESSAGE.SUCCESS.List.done").replaceAll("(\\{amount}|\\%amount%)", size).replaceAll("(\\{list}|\\%list%)", list));
			
			return true;
		}
		if(cmd.matches("(?ium)(reload|rload|rl)")) {
			if((!sender.hasPermission("healthbottle.admin")) && (!sender.hasPermission("healthbottle.command.reload"))) {
				sendMessage(sender, noPerm);
				return true;
			}
			
			Config.reloadConfigFile();
			
			sendMessage(sender, config.getString("MESSAGE.SUCCESS.config-reloaded"));
			
			return true;
		}
		
		return false;
	}
	
	/*
	 * CÁC SỰ KIỆN CỦA PLUGIN:
	 */
	
	@EventHandler
	public void onClick(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		String name = player.getName();
		
		Action act = event.getAction();
		
		if((act == Action.LEFT_CLICK_AIR) || (act == Action.LEFT_CLICK_BLOCK) || (act == Action.RIGHT_CLICK_AIR) || (act == Action.RIGHT_CLICK_BLOCK)) {
			
			/*
			 * SỰ KIỆN NHẤP CHUỘT PHẢI VÀO KHỐI NÀY THƯỜNG BỊ
			 * LẶP LẠI 2 LẦN (DÙ NHẤP CHUỘT CHỈ 1 LẦN), NÊN SẼ
			 * SỬ DỤNG LIST ĐỂ CHỈ CHO SỰ KIỆN THỰC HIỆN 1 LẦN.
			 */
			
			if(act == Action.RIGHT_CLICK_BLOCK) {
				if(!clicked.contains(name)) {
					clicked.add(name);
				} else {
					clicked.remove(name);
					return;
				}
			}
			
			for(String key : config.getConfigurationSection("BOTTLE").getKeys(false)) {
				ItemStack item = getItem(key);
				ItemStack stackHand = player.getInventory().getItemInMainHand();
				if(item.isSimilar(stackHand)) {
					event.setCancelled(true);
					event.setUseInteractedBlock(Result.DENY);
					event.setUseItemInHand(Result.DENY);
					
					if(hasRequirePermission(key)) {
						if((!player.hasPermission("healthbottle.admin")) && (!player.hasPermission("healthbottle.bypass.require")) && (!player.hasPermission(getRequirePermission(key)))) {
							sendMessage(player, config.getString("MESSAGE.FAIL.require-perm").replaceAll("(\\{permission}|\\%permission%|\\{perm}|\\%perm%)", getRequirePermission(key)));
							return;
						}
					}
					
					if(isInUse(name)) {
						sendMessage(player, config.getString("MESSAGE.FAIL.is-in-use"));
						return;
					} else{
						setUsed(name, true);
					}
					
					double maxHealth = player.getMaxHealth();
					double curHeart = ((int)(player.getHealth() * 100)) / 100.0;
					
					if(curHeart >= maxHealth) {
						sendMessage(player, config.getString("MESSAGE.FAIL.heart-is-full"));
						setUsed(name, false);
						return;
					}
					
					int stackHandAmount = stackHand.getAmount();
					
					if((!player.hasPermission("healthbottle.admin")) && (!player.hasPermission("healthbottle.bypass.remove")) && (hasNoRemovePermission(key) ? (!player.hasPermission(getNoRemovePermission(key))) : true)) {
						if(hasRemoveAmount(key)) {
							int removeAmount = getRemoveAmount(key);
							int result = stackHandAmount - removeAmount;
							if(result < 0) {
								sendMessage(player, config.getString("MESSAGE.FAIL.not-enough-item"));
								return;
							}
							stackHand.setAmount(result);
						} else {
							int result = stackHandAmount - 1;
							if(result < 0) {
								sendMessage(player, config.getString("MESSAGE.FAIL.not-enough-item"));
								return;
							}
							stackHand.setAmount(result);
						}
					}
					
					double heartRegen = config.getDouble("BOTTLE." + key + ".heart-regen");
					int regenPer = config.getInt("BOTTLE." + key + ".regen-per");
					int regenTimeOut = config.getInt("BOTTLE." + key + ".regen-time-out");
					
					new RunTask(this, player, heartRegen, regenPer, regenTimeOut).runTaskTimerAsynchronously(this, 20, 20);
					
					if(isUseTitle()) {
						
						String healthRegex = "(\\{health}|\\%health%|\\{heart}|\\%heart%)";
						String maxHealthRegex = "(\\{max_health}|\\%max_health%|\\{max_heart}|\\%max_heart%)";
						
						String main = config.getString("MESSAGE.SUCCESS.Title.main").replaceAll(healthRegex, String.valueOf(curHeart)).replaceAll(maxHealthRegex, String.valueOf(maxHealth));
						String sub = config.getString("MESSAGE.SUCCESS.Title.sub").replaceAll(healthRegex, String.valueOf(curHeart)).replaceAll(maxHealthRegex, String.valueOf(maxHealth));
						int fadeIn = config.getInt("MESSAGE.SUCCESS.Title.fadeIn");
						int stay = config.getInt("MESSAGE.SUCCESS.Title.stay");
						int fadeOut = config.getInt("MESSAGE.SUCCESS.Title.fadeOut");
						
						sendTitle(player, main, sub, fadeIn, stay, fadeOut);
					}
					
					return;
				}
			}
			
		}
		
	}
	
	@EventHandler
	public void onItemConsume(PlayerItemConsumeEvent event) {
		ItemStack item = event.getItem();
		for(String key : config.getConfigurationSection("BOTTLE").getKeys(false)) {
			ItemStack stack = getItem(key);
			if(item.isSimilar(stack)) {
				event.setCancelled(true);
				return;
			}
		}
	}
	
	
	
	/*
	 * CÁC API CỦA PLUGIN:
	 */
	
	public boolean isInUse(String name) {
		return used.contains(name);
	}
	
	public void setUsed(String name, boolean set) {
		if(set == true) {
			used.add(name);
		} else if(set == false) {
			used.remove(name);
		}
	}
	
	
	
	public List<String> getKeyList() {
		List<String> list = new ArrayList<String>();
		for(String key : config.getConfigurationSection("BOTTLE").getKeys(false)) {
			list.add(key);
		}
		return list;
	}
	
	public boolean hasAnyItem() {
		Set<String> set = config.getConfigurationSection("BOTTLE").getKeys(false);
		return ((set != null) && (!set.isEmpty()) && (set.size() > 0));
	}
	
	public int getSize() {
		return config.getConfigurationSection("BOTTLE").getKeys(false).size();
	}
	
	
	
	public void giveItem(Player player, ItemStack item) {
		player.getInventory().addItem(new ItemStack[] { item } );
	}
	
	
	
	public boolean isInt(String str) {
		try {
			Integer.parseInt(str);
		} catch(NumberFormatException ex) {
			return false;
		}
		return true;
	}
	
	public Material getMaterial(String str) {
		Material material = null;
		if(isInt(str)) {
			int id = Integer.parseInt(str);
			material = Material.getMaterial(id);
			for(Material materials : Material.values()) {
				if(material == materials) {
					return materials;
				}
			}
		} else {
			material = Material.valueOf(str.toUpperCase());
		}
		return material;
	}
	
	public boolean isUseTitle() {
		return config.getBoolean("use-title-broadcast");
	}
	
	public String toColor(String str) {
		return ChatColor.translateAlternateColorCodes('&', str.replaceAll("(\\{prefix}|\\%prefix%|\\{pref}|\\%pref%)", config.getString("PREFIX")));
	}
	
	public void sendMessage(CommandSender sender, String msg) {
		String newMsg = toColor(msg);
		if(sender != null) {
			sender.sendMessage(newMsg);
		}
	}
	
	public void sendTitle(Player player, String mainTitle, String subTitle, int fadeIn, int stay, int fadeOut) {
		String main = toColor(mainTitle);
		String sub = toColor(subTitle);
		if(player != null) {
			player.sendTitle(main, sub, 20 * fadeIn, 20 * stay, 20 * fadeOut);
		}
	}
	
	public ItemStack getItem(String key) {
		String type = config.getString("BOTTLE." + key + ".type");
		String[] split = type.split(":");
		String material = split[0];
		byte data = Byte.parseByte(split[1]);
		int amount = Integer.parseInt(split[2]);
		
		ItemStack item = new ItemStack(getMaterial(material), amount, data);
		ItemMeta meta = item.getItemMeta();
		
		meta.setDisplayName(toColor(config.getString("BOTTLE." + key + ".name")));
		
		List<String> lores = config.getStringList("BOTTLE." + key + ".lore");
		for(int x = 0; x < lores.size(); x++) {
			String str = toColor(lores.get(x));
			if((str.contains("{heart-regen}")) || (str.contains("%heart-regen%"))) {
				str = str.replaceAll("(\\{heart-regen}|\\%heart-regen%)", String.valueOf(getHeartRegen(key)));
			} else if((str.contains("{regen-per}")) || (str.contains("%regen-per%"))) {
				str = str.replaceAll("(\\{regen-per}|\\%regen-per%)", String.valueOf(getRegenPer(key)));
			} else if((str.contains("{regen-time-out}")) || (str.contains("%regen-time-out%"))) {
				str = str.replaceAll("(\\{regen-time-out}|\\%regen-time-out%)", String.valueOf(getRegenTimeOut(key)));
			}
			lores.set(x, str);
		}
		meta.setLore(lores);
		
		item.setItemMeta(meta);
		
		boolean glow = config.getBoolean("BOTTLE." + key + ".glow");
		if(glow == true) {
			meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
			item.setItemMeta(meta);
			item.addUnsafeEnchantment(Enchantment.DURABILITY, 10);
		}
		
		return item;
	}
	
	public ItemStack getItem(String key, int amount) {
		String type = config.getString("BOTTLE." + key + ".type");
		String[] split = type.split(":");
		String material = split[0];
		byte data = Byte.parseByte(split[1]);
		
		ItemStack item = new ItemStack(getMaterial(material), amount, data);
		ItemMeta meta = item.getItemMeta();
		
		meta.setDisplayName(toColor(config.getString("BOTTLE." + key + ".name")));
		
		List<String> lores = config.getStringList("BOTTLE." + key + ".lore");
		for(int x = 0; x < lores.size(); x++) {
			String str = toColor(lores.get(x));
			if((str.contains("{heart-regen}")) || (str.contains("%heart-regen%"))) {
				str = str.replaceAll("(\\{heart-regen}|\\%heart-regen%)", String.valueOf(getHeartRegen(key)));
			} else if((str.contains("{regen-per}")) || (str.contains("%regen-per%"))) {
				str = str.replaceAll("(\\{regen-per}|\\%regen-per%)", String.valueOf(getRegenPer(key)));
			} else if((str.contains("{regen-time-out}")) || (str.contains("%regen-time-out%"))) {
				str = str.replaceAll("(\\{regen-time-out}|\\%regen-time-out%)", String.valueOf(getRegenTimeOut(key)));
			}
			lores.set(x, str);
		}
		meta.setLore(lores);
		
		item.setItemMeta(meta);
		
		boolean glow = config.getBoolean("BOTTLE." + key + ".glow");
		if(glow == true) {
			meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
			item.setItemMeta(meta);
			item.addUnsafeEnchantment(Enchantment.DURABILITY, 10);
		}
		
		return item;
	}
	
	
	
	public boolean isExist(String key) {
		return config.contains("BOTTLE." + key);
	}
	
	
	
	public String getItemName(String key) {
		return toColor(config.getString("BOTTLE." + key + ".name"));
	}
	
	public double getHeartRegen(String key) {
		return config.getDouble("BOTTLE." + key + ".heart-regen");
	}
	
	public int getRegenPer(String key) {
		return config.getInt("BOTTLE." + key + ".regen-per");
	}
	
	public int getRegenTimeOut(String key) {
		return config.getInt("BOTTLE." + key + ".regen-time-out");
	}
	
	public boolean hasRequirePermission(String key) {
		String path = config.getString("BOTTLE." + key + ".require-permission");
		return ((path != null) && (!path.isEmpty()) && (path.length() > 0) && (!path.equalsIgnoreCase("none")));
	}
	
	public String getRequirePermission(String key) {
		return config.getString("BOTTLE." + key + ".require-permission");
	}
	
	public boolean hasRemoveAmount(String key) {
		String path = config.getString("BOTTLE." + key + ".remove-amount");
		return ((path != null) && (!path.isEmpty()) && (path.length() > 0) && (!path.equalsIgnoreCase("none")));
	}
	
	public int getRemoveAmount(String key) {
		return config.getInt("BOTTLE." + key + ".remove-amount");
	}
	
	public boolean hasNoRemovePermission(String key) {
		String path = config.getString("BOTTLE." + key + ".no-remove-permission");
		return ((path != null) && (!path.isEmpty()) && (path.length() > 0) && (!path.equalsIgnoreCase("none")));
	}
	
	public String getNoRemovePermission(String key) {
		return config.getString("BOTTLE." + key + ".no-remove-permission");
	}
	
}
