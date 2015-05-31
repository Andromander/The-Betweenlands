package thebetweenlands.event.render;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import thebetweenlands.client.render.shader.ShaderHelper;
import thebetweenlands.client.render.shader.impl.MainShader;
import thebetweenlands.utils.confighandler.ConfigHandler;

import java.lang.reflect.Method;

public class ShaderHandler {
	public static final ShaderHandler INSTANCE = new ShaderHandler();
	
	private boolean failedLoading = false;
	private MainShader currentShader;
	private ShaderGroup currentShaderGroup;
	private Method renderHandMethod = null;

	public MainShader getShader() {
		return this.currentShader;
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onPreRender(RenderWorldLastEvent event) {
		if(ConfigHandler.USE_SHADER && ShaderHelper.INSTANCE.isShaderSupported()) {
			Minecraft mc = Minecraft.getMinecraft();
			if(!mc.gameSettings.fboEnable) {
				mc.gameSettings.fboEnable = true;
				mc.getFramebuffer().createBindFramebuffer(mc.displayWidth, mc.displayHeight);
			}
			ShaderHelper.INSTANCE.enableShader();
			ShaderHelper.INSTANCE.updateShader();
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onPostRender(TickEvent.RenderTickEvent event) {
		if(ConfigHandler.USE_SHADER && event.phase == Phase.END && ShaderHelper.INSTANCE.isShaderSupported()) {
			ShaderHelper.INSTANCE.clearDynLights();
		}
	}
}