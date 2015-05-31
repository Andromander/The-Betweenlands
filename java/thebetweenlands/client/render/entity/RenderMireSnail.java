package thebetweenlands.client.render.entity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import thebetweenlands.client.model.entity.ModelMireSnail;
import thebetweenlands.entities.mobs.EntityMireSnail;

@SideOnly(Side.CLIENT)
public class RenderMireSnail extends RenderLiving {
	private static final ResourceLocation texture = new ResourceLocation("thebetweenlands:textures/entity/mireSnail.png");

	public RenderMireSnail() {
		super(new ModelMireSnail(), 0.5F);
	}

	@Override
	protected void preRenderCallback(EntityLivingBase entityliving, float partialTickTime) {
		EntityMireSnail snail = (EntityMireSnail) entityliving;
		if (snail.isClimbing())
			rotateSnail(entityliving);
	}

	protected void rotateSnail(EntityLivingBase entityliving) {
		GL11.glRotatef(90.0F, -1.0F, 0.0F, 0.0F);
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return texture;
	}
}