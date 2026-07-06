package dev.celestiacraft.interlocked.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class CommonConfig {
	private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

	public static final ForgeConfigSpec.IntValue INTERLOCK_LIMIT;

	static {
		BUILDER.comment("All settings below will only take effect after restarting the server or client.")
				.push("general");

		INTERLOCK_LIMIT = BUILDER
				.comment("The limit of interlock distance")
				.comment("type: int")
				.comment("default: 32")
				.defineInRange("interlock_distance_limit", 32, 0, 128);

		BUILDER.pop();
	}

	public static final ForgeConfigSpec SPEC = BUILDER.build();
}
