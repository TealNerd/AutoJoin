package com.biggestnerd.autojoin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

@Mod(modid="autojoin", name="AutoJoin", version="v1.3.1")
public class AutoJoin {

	Minecraft mc = Minecraft.getMinecraft();
	ServerData last;
	int counter = 0;
	boolean logoutFlag;
	File logFile;
	SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
	
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
			mc.displayGuiScreen(new GuiConnecting(mc.currentScreen, mc, last));
			System.out.println("Connecting to " + last.serverIP);
			counter = 0;
		}
	}
	
	public void logToFile(String message) throws Exception {
		BufferedWriter writer = new BufferedWriter(new FileWriter(logFile));
		writer.write(message);
		writer.close();
	}
}