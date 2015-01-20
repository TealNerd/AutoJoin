package com.biggestnerd.autojoin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;

@Mod(modid="autojoin", name="AutoJoin", version="v1.0")
public class AutoJoin {

	Minecraft mc = Minecraft.getMinecraft();
	ServerData last;
	int counter = 0;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		FMLCommonHandler.instance().bus().register(this);
	}
	
	
	@SubscribeEvent
	public void onTick(ClientTickEvent event) {
		if(mc.currentScreen instanceof GuiDisconnected) {
			if(counter <= 420) {
				counter++;
			}
		}
		if(mc.currentScreen instanceof GuiConnecting && mc.func_147104_D() != null) {
			last = mc.func_147104_D();
		}
		if(counter >= 450) {
			mc.displayGuiScreen(new GuiConnecting(mc.currentScreen, mc, last));
			System.out.println("Connecting to " + last.serverIP);
			counter = 0;
		}
	}
}
