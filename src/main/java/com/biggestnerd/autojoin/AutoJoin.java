package com.biggestnerd.autojoin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

@Mod(modid="autojoin", name="AutoJoin", version="v1.4")
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
	
	@SubscribeEvent
	public void onTick(ClientTickEvent event) {
		if(mc.currentScreen instanceof GuiDisconnected) {
			if(logoutFlag) {
				Date date = new Date();
				String log = "[" + (System.currentTimeMillis() / 1000L) + "][" + format.format(date) + "] Disconnected";
				logoutFlag = false;
			}
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
		if(counter >= 420) {
			mc.displayGuiScreen(new GuiConnecting(new GuiMainMenu(), mc, last));
			System.out.println("Connecting to " + last.serverIP);
			counter = 0;
		}
		if(mc.theWorld != null && !mc.isSingleplayer() && System.currentTimeMillis() - logoutCheckTimer > 5000) {
			logoutCheckTimer = System.currentTimeMillis();
			try {
				if(isAutoRelog()) {
					last = mc.getCurrentServerData();
					mc.theWorld.sendQuittingDisconnectingPacket();
					mc.displayGuiScreen(new GuiDisconnected(null, "Get fukt", new ChatComponentText("lelelelele")));
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public boolean isAutoRelog() throws Exception {
		if(!macromod) {
			return false;
		}
		Pattern p = Pattern.compile("auto_relog\">(0|1)");
		BufferedReader reader = new BufferedReader(new FileReader(macroGlobalVars));
		String line = "";
		while((line = reader.readLine()) != null) {
			Matcher m = p.matcher(line);
			if(m.find()) {
				reader.close();
				return Integer.parseInt(m.group(1)) == 1;
			}
		}
		reader.close();
		return false;
	}
	
	public void logToFile(String message) throws Exception {
		BufferedWriter writer = new BufferedWriter(new FileWriter(logFile));
		writer.write(message);
		writer.close();
	}
}