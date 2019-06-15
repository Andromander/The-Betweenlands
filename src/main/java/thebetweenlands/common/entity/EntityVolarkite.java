package thebetweenlands.common.entity;

import java.util.Iterator;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFire;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thebetweenlands.common.item.misc.ItemVolarkite;
import thebetweenlands.common.registries.BlockRegistry;

public class EntityVolarkite extends Entity {
	public float prevRotationRoll;
	public float rotationRoll;

	public EntityVolarkite(World world) {
		super(world);
		this.setSize(0.6F, 1.7F);
	}

	@Override
	protected void entityInit() {
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
	}

	@Override
	public double getMountedYOffset() {
		return 0.325D;
	}

	@Override
	public void onEntityUpdate() {
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		this.prevRotationPitch = this.rotationPitch;
		this.prevRotationYaw = this.rotationYaw;
		this.prevRotationRoll = this.rotationRoll;

		double targetMotionY = -0.04D;

		this.motionY = targetMotionY + (this.motionY - targetMotionY) * 0.92D;

		this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
		this.handleWaterMovement();

		float invFriction = 1.0F;

		if(this.onGround) {
			invFriction = 0.8F;
		}

		this.motionX *= invFriction;
		this.motionY *= invFriction;
		this.motionZ *= invFriction;

		Entity controller = this.getControllingPassenger();

		Vec3d kiteDir = new Vec3d(Math.cos(Math.toRadians(this.rotationYaw + 90)), 0, Math.sin(Math.toRadians(this.rotationYaw + 90)));

		double rotIncr = 0;

		boolean hasEquipment = false;

		if(controller != null) {
			controller.fallDistance = 0;

			if(this.motionY < 0 && !this.onGround) {
				double speedBoost = -this.motionY * 0.1D + MathHelper.clamp(Math.sin(Math.toRadians(this.rotationPitch)) * 0.5F, -0.02D, 0.02D);

				this.motionX += kiteDir.x * (speedBoost + 0.01D);
				this.motionZ += kiteDir.z * (speedBoost + 0.01D);

				this.velocityChanged = true;
			}

			Vec3d controllerDir = new Vec3d(Math.cos(Math.toRadians(controller.rotationYaw + 90)), 0, Math.sin(Math.toRadians(controller.rotationYaw + 90)));
			double rotDiff = Math.toDegrees(Math.acos(kiteDir.dotProduct(controllerDir))) * -Math.signum(kiteDir.crossProduct(controllerDir).y);
			rotIncr = MathHelper.clamp(rotDiff * 0.05D, -1.0D, 1.0D);
			this.rotationYaw += rotIncr;

			if(!this.onGround && controller instanceof EntityLivingBase) {
				float forward = ((EntityLivingBase) controller).moveForward;
				if(forward > 0.1F) {
					this.rotationPitch = 20.0F + (this.rotationPitch - 20.0F) * 0.9F;
					this.motionY -= 0.01D;
				} else if(forward < -0.1F) {
					this.rotationPitch = -20.0F + (this.rotationPitch + 20.0F) * 0.9F;
				}
			}

			Iterator<ItemStack> it = controller.getHeldEquipment().iterator();
			while(it.hasNext()) {
				ItemStack stack = it.next();
				if(!stack.isEmpty() && stack.getItem() instanceof ItemVolarkite) {
					hasEquipment = true;
					break;
				}
			}
		}

		if(!this.onGround && Math.abs(rotIncr) > 0.1D) {
			this.rotationRoll = (float) (rotIncr * 15 + (this.rotationRoll - rotIncr * 15) * 0.9D);
		} else {
			this.rotationRoll *= 0.9F;
		}

		this.rotationPitch *= 0.9F;

		this.updateUpdraft();

		double speed = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);

		if(speed > 0.1D) {
			double dx = this.motionX / speed;
			double dz = this.motionZ / speed;

			this.motionX = (kiteDir.x + (dx - kiteDir.x) * 0.9D) * speed;
			this.motionZ = (kiteDir.z + (dz - kiteDir.z) * 0.9D) * speed;

			double maxSpeed = 0.6D;
			if(speed > maxSpeed) {
				double targetX = dx * maxSpeed;
				double targetZ = dz * maxSpeed;

				this.motionX = targetX + (this.motionX - targetX) * 0.8D;
				this.motionZ = targetZ + (this.motionZ - targetZ) * 0.8D;
			}

			this.velocityChanged = true;
		}

		if(!this.world.isRemote && !hasEquipment) {
			this.onKillCommand();
		}

		this.firstUpdate = false;
	}

	protected void updateUpdraft() {
		int range = 10;

		boolean hasUpdraft = false;

		int updraftPos = 0;

		PooledMutableBlockPos pos = PooledMutableBlockPos.retain();
		pos.setPos(MathHelper.floor(this.posX), MathHelper.floor(this.posY), MathHelper.floor(this.posZ));

		for(int i = 0; i <= range; i++) {
			IBlockState state = this.world.getBlockState(pos);

			Block block = state.getBlock();

			if(block instanceof IFluidBlock) {
				Fluid fluid = ((IFluidBlock) block).getFluid();
				if(fluid.getTemperature() > 373 /*roughly 100�C*/) {
					hasUpdraft = true;
				}
			} else if(state.getMaterial() == Material.FIRE || state.getMaterial() == Material.LAVA || block instanceof BlockFire || block == BlockRegistry.OCTINE_ORE || block == BlockRegistry.OCTINE_BLOCK) {
				hasUpdraft = true;
			} else if(!block.isAir(state, this.world, pos)) {
				break;
			}

			if(hasUpdraft) {
				updraftPos = pos.getY();
				break;
			}

			pos.setPos(pos.getX(), pos.getY() - 1, pos.getZ());
		}

		pos.release();

		if(hasUpdraft) {
			if(this.motionY < 1.0D) {
				this.motionY += 0.1D;
			}

			if(this.world.isRemote) {
				for(int i = 0; i < 10; i++) {
					float offsetX = this.world.rand.nextFloat() - 0.5F;
					float offsetZ = this.world.rand.nextFloat() - 0.5F;

					float len = (float)Math.sqrt(offsetX*offsetX + offsetZ*offsetZ);

					offsetX /= len;
					offsetZ /= len;

					this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX + offsetX, updraftPos + (this.posY + 1 - updraftPos) * this.world.rand.nextFloat(), this.posZ + offsetZ, this.motionX, this.motionY + 0.25D, this.motionZ);
				}
			}
		}
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		while(this.rotationYaw - this.prevRotationYaw < -180.0F) {
			this.prevRotationYaw -= 360.0F;
		}

		while(this.rotationYaw - this.prevRotationYaw >= 180.0F) {
			this.prevRotationYaw += 360.0F;
		}
	}

	@Override
	@Nullable
	public Entity getControllingPassenger() {
		return this.getPassengers().isEmpty() ? null : (Entity)this.getPassengers().get(0);
	}

	@Override
	public boolean canPassengerSteer() {
		return true;
	}

	@Override
	public boolean shouldRiderSit() {
		return false;
	}

	@Override
	protected void removePassenger(Entity passenger) {
		super.removePassenger(passenger);

		passenger.fallDistance = 0;

		passenger.motionX = this.motionX;
		passenger.motionY = this.motionY;
		passenger.motionZ = this.motionZ;
	}

	@Override
	public void fall(float distance, float damageMultiplier) {
		//No fall damage to node or rider
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean isInRangeToRenderDist(double distance) {
		return super.isInRangeToRenderDist(distance) || (this.getControllingPassenger() != null && this.getControllingPassenger().isInRangeToRenderDist(distance));
	}
}
