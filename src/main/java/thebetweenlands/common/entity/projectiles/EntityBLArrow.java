package thebetweenlands.common.entity.projectiles;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IThrowableEntity;
import thebetweenlands.common.herblore.elixir.ElixirEffectRegistry;
import thebetweenlands.common.item.tools.bow.EnumArrowType;
import thebetweenlands.common.registries.EntityRegistry;
import thebetweenlands.common.registries.ItemRegistry;

//TODO 1.13 BL arrow is missing IThrowableEntity. Removed or renamed?
public class EntityBLArrow extends EntityArrow implements IThrowableEntity /*for shooter sync*/ {
	private static final DataParameter<String> DW_TYPE = EntityDataManager.<String>createKey(EntityArrow.class, DataSerializers.STRING);

	public EntityBLArrow(World worldIn) {
		super(EntityRegistry.BETWEENLANDS_ARROW, worldIn);
	}

	public EntityBLArrow(World worldIn, double x, double y, double z) {
		super(EntityRegistry.BETWEENLANDS_ARROW, x, y, z, worldIn);
	}

	public EntityBLArrow(World worldIn, EntityLivingBase shooter) {
		super(EntityRegistry.BETWEENLANDS_ARROW, shooter, worldIn);
	}

	@Override
	public Entity getThrower() {
		return this.shootingEntity;
	}
	
	@Override
	public void setThrower(Entity entity) {
		this.shootingEntity = entity;
	}
	
	@Override
	public void registerData() {
		super.registerData();
		this.dataManager.register(DW_TYPE, "");
	}

	@Override
	public void writeAdditional(NBTTagCompound nbt) {
		super.writeAdditional(nbt);
		nbt.setString("arrowType", this.getArrowType().getName());
	}

	@Override
	public void readAdditional(NBTTagCompound nbt) {
		super.readAdditional(nbt);
		this.setType(EnumArrowType.getEnumFromString(nbt.getString("arrowType")));
	}

	@Override
	protected void arrowHit(EntityLivingBase living) {
		switch(this.getArrowType()) {
		case ANGLER_POISON:
			living.addPotionEffect(new PotionEffect(MobEffects.POISON, 200, 2));
			break;
		case OCTINE:
			if(living.isBurning()) {
				living.setFire(9);
			} else {
				living.setFire(5);
			}
			break;
		case BASILISK:
			if(!living.isNonBoss()) {
				
			}
			living.addPotionEffect(ElixirEffectRegistry.EFFECT_PETRIFY.createEffect(100, 1));
			break;
		default:
		}
	}

	/**
	 * Sets the arrow type
	 * @param type
	 */
	public void setType(EnumArrowType type) {
		this.dataManager.set(DW_TYPE, type.getName());
	}

	/**
	 * Returns the arrow type
	 * @return
	 */
	public EnumArrowType getArrowType(){
		return EnumArrowType.getEnumFromString(this.dataManager.get(DW_TYPE));
	}

	@Override
	protected ItemStack getArrowStack() {
		switch(this.getArrowType()) {
		case ANGLER_POISON:
			return new ItemStack(ItemRegistry.POISONED_ANGLER_TOOTH_ARROW);
		case OCTINE:
			return new ItemStack(ItemRegistry.OCTINE_ARROW);
		case BASILISK:
			return new ItemStack(ItemRegistry.BASILISK_ARROW);
		case DEFAULT:
		default:
			return new ItemStack(ItemRegistry.ANGLER_TOOTH_ARROW);
		}
	}
}
