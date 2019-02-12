package thebetweenlands.api.rune.impl;

import net.minecraft.util.ResourceLocation;
import thebetweenlands.common.lib.ModInfo;

public final class RuneMarkDescriptors {
	private RuneMarkDescriptors()  {}

	public static final ResourceLocation BLOCK = new ResourceLocation(ModInfo.ID, "block");
	public static final ResourceLocation ENTITY = new ResourceLocation(ModInfo.ID, "entity");
	public static final ResourceLocation RAY = new ResourceLocation(ModInfo.ID, "ray");
	public static final ResourceLocation POSITION = new ResourceLocation(ModInfo.ID, "position");
}
