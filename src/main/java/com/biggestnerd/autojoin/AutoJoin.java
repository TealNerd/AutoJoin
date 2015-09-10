package com.biggestnerd.autojoin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

@Mod(modid="autojoin", name="AutoJoin", version="v1.5.2")
public class AutoJoin {

	Minecraft mc = Minecraft.getMinecraft();
	ServerData last;
	int counter = 0;
	boolean logoutFlag;
	File logFile;
	SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
	boolean macromod = false;
	File macroFolder;
	File macroGlobalVars;
	long logoutCheckTimer = 0;
	boolean relog = false;
	long relogTime = 0;
	long relogStartTime = 0;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		FMLCommonHandler.instance().bus().register(this);
		logFile = new File(mc.mcDataDir, "AutojoinLog.txt");
		if(!logFile.isFile()) {
			try {
				logFile.createNewFile();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		macroFolder = new File(mc.mcDataDir, "liteconfig/common/macros");
		if(macroFolder.isDirectory()) {
			macroGlobalVars = new File(macroFolder, ".globalvars.xml");
			if(macroGlobalVars.exists()) {
				macromod = true;
			}
		}
	}
	
	//fires every tick, 30 times per second?
	@SubscribeEvent
	public void onTick(ClientTickEvent event) {
		//if we're looking at minecraft's disconnected/kicked by server screen
		if(mc.currentScreen instanceof GuiDisconnected) {
			if(logoutFlag) {
				Date date = new Date();
				String log = "[" + (System.currentTimeMillis() / 1000L) + "][" + format.format(date) + "] Disconnected";
				logoutFlag = false;
			}
			//increments counter if it's less than 420
			if(counter <= 420) {
				counter++;
			}
		}
		if(mc.currentScreen instanceof GuiConnecting && mc.getCurrentServerData() != null) {
			last = mc.getCurrentServerData();
		}
		if(mc.theWorld != null && !logoutFlag) {
			last = mc.getCurrentServerData();
			Date date = new Date();
			String log = "[" + (System.currentTimeMillis() / 1000L) + "][" + format.format(date) + "] Connected";
			mc.setIngameFocus();
			logoutFlag = true;
		}
		//if counter is >= 420 then it's been 14 seconds since we were disconnected, and we should try logging in again
		if(counter >= 420) {
			mc.displayGuiScreen(new GuiConnecting(new GuiMainMenu(), mc, last));
			System.out.println("Connecting to " + last.serverIP);
			//reset the counter for next time
			counter = 0;
		}
		//checks if we're ingame, then checks if it's been 5 seconds since we last checked for the global logout variable
		//calculates it by saying currentTime - lastCheckTime > 5 seconds
		if(mc.theWorld != null && !mc.isSingleplayer() && System.currentTimeMillis() - logoutCheckTimer > 5000) {
			//resets check timer
			logoutCheckTimer = System.currentTimeMillis();
			try {
				//getAutoRelogTime gives us a time in milliseconds for how long we should stay logged out, it will be 0 if we shouldnt log out
				long time = getAndResetAutoRelogTime();
				if(time > 0) {
					last = mc.getCurrentServerData();
					mc.theWorld.sendQuittingDisconnectingPacket();
					mc.displayGuiScreen(new GuiMainMenu());
					relogStartTime = System.currentTimeMillis();
					relogTime = time;
					relog = true;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		//if we're supposed to relog, and we're not ingame, and it's been relogtime since we logged out, log back in and set relog to false
		if(relog && (mc.currentScreen instanceof GuiMainMenu) && System.currentTimeMillis() - relogStartTime > relogTime) {
			mc.displayGuiScreen(new GuiConnecting(new GuiMainMenu(), mc, last));
			relog = false;
		}
	}
	
	public long getAndResetAutoRelogTime() throws Exception {
		if(!macromod) {
			return 0;
		}
		long time = 0;
		Pattern p = Pattern.compile("#auto_relog\">(\\d+)");
		BufferedReader reader = new BufferedReader(new FileReader(macroGlobalVars));
		List<String> lines = new ArrayList<String>();
		String line = "";
		while((line = reader.readLine()) != null) {
			Matcher m = p.matcher(line);
			if(m.find()) {
				time = Long.parseLong(m.group(1));
			} else {
				lines.add(line);
			}
		}
		reader.close();
		BufferedWriter writer = new BufferedWriter(new FileWriter(macroGlobalVars));
		for(String s : lines) {
			writer.write(s + "\n");
		}
		writer.close();
		return time;
	}
	
	public void logToFile(String message) throws Exception {
		BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true));
		writer.write(message);
		writer.close();
	}
}