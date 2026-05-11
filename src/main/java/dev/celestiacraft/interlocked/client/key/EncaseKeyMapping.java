package dev.celestiacraft.interlocked.client.key;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class EncaseKeyMapping {
	public static final KeyMapping ACTIVATE = new KeyMapping(
			"key.interlocked.activate",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_LEFT_ALT,
			"key.interlocked.categories"
	);
}
