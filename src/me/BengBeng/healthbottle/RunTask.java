package me.BengBeng.healthbottle;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class RunTask
	extends BukkitRunnable {
	
	Main main;
	Player player;
	double heartRegen;
	int regenPer;
	int timeOut;
	
	int countTimeOut = 0;
	
	public RunTask(Main main, Player player, double heartRegen, int regenPer, int timeOut) {
		this.main = main;
		this.player = player;
		this.heartRegen = heartRegen;
		this.regenPer = regenPer;
		this.timeOut = timeOut;
	}
	
	@Override
	public void run() {
		if(countTimeOut == timeOut) {
			main.setUsed(player.getName(), false);
			this.cancel();
			return;
		}
		countTimeOut += 1;
		if((countTimeOut % regenPer) == 0) {
			// Thực hiện hồi phục máu cho player tại đây.
			double curHeart = player.getHealth();
			double maxHeart = player.getMaxHealth();
			
			double result = (curHeart + heartRegen);
			
			if(result >= maxHeart) {
				player.setHealth(maxHeart);
			} else {
				player.setHealth(result);
			}
			
			if(main.isUseTitle()) {
				double maxHealth = player.getMaxHealth();
				double curHealth = ((int)(player.getHealth() * 100)) / 100.0;
				
				String healthRegex = "(\\{health}|\\%health%|\\{heart}|\\%heart%)";
				String maxHealthRegex = "(\\{max_health}|\\%max_health%|\\{max_heart}|\\%max_heart%)";
				
				String mainTitle = main.config.getString("MESSAGE.SUCCESS.Title.main").replaceAll(healthRegex, String.valueOf(curHealth)).replaceAll(maxHealthRegex, String.valueOf(maxHealth));
				String subTitle = main.config.getString("MESSAGE.SUCCESS.Title.sub").replaceAll(healthRegex, String.valueOf(curHealth)).replaceAll(maxHealthRegex, String.valueOf(maxHealth));
				int fadeIn = main.config.getInt("MESSAGE.SUCCESS.Title.fadeIn");
				int stay = main.config.getInt("MESSAGE.SUCCESS.Title.stay");
				int fadeOut = main.config.getInt("MESSAGE.SUCCESS.Title.fadeOut");
				
				main.sendTitle(player, mainTitle, subTitle, fadeIn, stay, fadeOut);
			}
		}
	}
	
}
