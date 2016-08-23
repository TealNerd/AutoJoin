package com.biggestnerd.autojoin;

import java.awt.Color;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

@Mod(modid="autojoin", name="AutoJoin", version="2.0")
public class AutoJoin {

	Minecraft mc;
	ServerData data;
	long last = 0;
	int time = 10;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		mc = Minecraft.getMinecraft();
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent
	public void onGuiOpen(GuiOpenEvent event) {
		if(event.getGui() instanceof GuiDisconnected) {
			last = System.currentTimeMillis();
			time = 10;
		}
	}
	
	@SubscribeEvent
	public void onDrawScreen(GuiScreenEvent.DrawScreenEvent event) {
		if(event.getGui() instanceof GuiDisconnected) {
			event.getGui().drawCenteredString(mc.fontRendererObj, "Reconnect in: " + time + " seconds", event.getGui().width / 2, 30, Color.WHITE.getRGB());
		}
	}
	
	@SubscribeEvent
	public void onTick(ClientTickEvent event) {
		if(mc.currentScreen instanceof GuiDisconnected) {
			GuiDisconnected current = (GuiDisconnected) mc.currentScreen;
			current.drawCenteredString(mc.fontRendererObj, time + " seconds", current.width / 2, 30, Color.WHITE.getRGB());
			if(System.currentTimeMillis() - last >= 1000l) {
				time--;
				last = System.currentTimeMillis();
			}
			if(time == 0) {
				connectToServer();
			}
		}
		if(mc.getCurrentServerData() != null) {
			data = mc.getCurrentServerData();
		}
	}
	
	public void connectToServer() {
		if(data == null) {
			mc.displayGuiScreen(new GuiMainMenu());
		} else {
			mc.displayGuiScreen(new GuiConnecting(new GuiMainMenu(), mc, data));
		}
	}
}
