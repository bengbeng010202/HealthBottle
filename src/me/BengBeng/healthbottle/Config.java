package me.BengBeng.healthbottle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Config {
	
	private static FileConfiguration config;
	private static File configF;
	private static boolean load = false;
	
	private static void coppyFile(InputStream in, File out) throws Exception {
		InputStream fis = in;
		FileOutputStream fos = new FileOutputStream(out);
		try {
			byte[] buf = new byte[1024];
			int i = 0;
			while((i = fis.read(buf)) != -1) {
				fos.write(buf, 0, i);
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		} finally {
			fos.close();
			fis.close();
		}
	}
	
	public static void loadConfigFile() {
		configF = new File(Bukkit.getServer().getPluginManager().getPlugin("HealthBottle").getDataFolder(), "config.yml");
		if(configF.exists()) {
			config = new YamlConfiguration();
			try {
				config.load(configF);
			} catch (IOException | InvalidConfigurationException e) {
				e.printStackTrace();
			}
			load = true;
		} else {
			try {
				Bukkit.getServer().getPluginManager().getPlugin("HealthBottle").getDataFolder().mkdirs();
				InputStream jarURL = Config.class.getResourceAsStream("/config.yml");
				coppyFile(jarURL, configF);
				config = new YamlConfiguration();
				config.load(configF);
				load = true;
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	
	
	public static void saveConfigFile() {
		if(!load) {
			loadConfigFile();
		} else {
			try {
				config.save(configF);
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	
	
	public static void reloadConfigFile() {
		try {
			config.load(configF);
			InputStream input = new FileInputStream(configF);
			if(input != null) {
				config.setDefaults(YamlConfiguration.loadConfiguration((Reader)new InputStreamReader(input, StandardCharsets.UTF_8)));
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
	
	public static FileConfiguration getConfigFile() {
		if(!load) {
			loadConfigFile();
		}
		return config;
	}
	
}
