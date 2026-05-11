package dev.celestiacraft.interlocked.client;

import dev.celestiacraft.interlocked.Interlocked;
import dev.celestiacraft.interlocked.client.key.EncaseKeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Interlocked.MODID, value = Dist.CLIENT)
public class InterlockedClient {
	public static void onCtorClient(IEventBus bus) {
		bus.addListener(InterlockedClient::onRegisterKeys);
	}

	@SubscribeEvent
	public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
		event.register(EncaseKeyMapping.ACTIVATE);
	}
}
