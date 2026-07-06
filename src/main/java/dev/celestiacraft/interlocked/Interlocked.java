package dev.celestiacraft.interlocked;

import dev.celestiacraft.interlocked.client.InterlockedClient;
import dev.celestiacraft.interlocked.config.CommonConfig;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Interlocked.MODID)
public class Interlocked {
	public static final String MODID = "interlocked";

	public Interlocked(FMLJavaModLoadingContext context) {
		IEventBus bus = context.getModEventBus();

		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> InterlockedClient.onCtorClient(bus));

		context.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC, "nebula/interlocked/common.toml");
	}
}
