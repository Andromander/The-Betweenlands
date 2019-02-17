package thebetweenlands.client.render.model.entity;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.entity.model.ModelBase;
import net.minecraft.client.renderer.entity.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelMireSnailEgg extends ModelBase {
    ModelRenderer shell;
    ModelRenderer inners;

    public ModelMireSnailEgg() {
        this.textureWidth = 64;
        this.textureHeight = 32;
        this.shell = new ModelRenderer(this, 0, 0);
        this.shell.setRotationPoint(0.0F, 24.0F, 0.0F);
        this.shell.addBox(-1.0F, -2.0F, -1.0F, 2, 2, 2, 0.0F);
        this.inners = new ModelRenderer(this, 0, 5);
        this.inners.setRotationPoint(0.0F, 24.0F, 0.0F);
        this.inners.addBox(-0.5F, -1.5F, -0.5F, 1, 1, 1, 0.0F);
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAngle, float entityTickTime, float rotationYaw, float rotationPitch, float unitPixel) {
        super.render(entity, limbSwing, limbSwingAngle, entityTickTime, rotationYaw, rotationPitch, unitPixel);
        GL11.glPushMatrix();
        this.inners.render(unitPixel);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        this.shell.render(unitPixel);
        GL11.glDisable(GL11.GL_BLEND);

        GL11.glPopMatrix();
    }

}
