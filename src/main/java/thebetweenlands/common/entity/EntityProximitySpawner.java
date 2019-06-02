package thebetweenlands.common.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

public abstract class EntityProximitySpawner extends EntityMob {

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0D);
		getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(5.0D);
	}

	public EntityProximitySpawner(World world) {
		super(world);
	}
	/**
	 * Amount to extend proximity area in XZ axis
	 *
	 * @return amount to expand proximity box all around in the x & z axis.
	 */
	protected abstract float getProximityHorizontal();
	
	/**
	 * Amount to extend proximity area in Y axis
	 *
	 * @return amount to expand proximity box all around in the y axis.
	 */
	protected abstract float getProximityVertical();

	/**
	 * Test if entity can sneak past
	 *
	 * @return true to allow player to sneak past. false to deny it.
	 */
	protected abstract boolean canSneakPast();

	/**
	 * Test if entity needs line of sight to activate
	 *
	 * @return true for yes. false for no.
	 */
	protected abstract boolean checkSight();

	/**
	 * Which entity should be spawned on activation
	 *
	 * @return an Entity.
	 */
	protected abstract Entity getEntitySpawned();

	/**
	 * How Many Entities should be spawned
	 *
	 * @return an int amount.
	 */
	protected abstract int getEntitySpawnCount();

	/**
	 * Test if spawner is just a single use 
	 *
	 * @return true to set dead after spawn. false to deny it.
	 */
	protected abstract boolean isSingleUse();

	/**
	 * How many spawns this does (NYI)
	 *
	 * @return amount of uses.
	 */
	protected abstract int maxUseCount();
	
	/**
	 * The Proximity box used
	 *
	 * @return an AxisAlignedBB for the proximity area.
	 */
	protected abstract AxisAlignedBB proximityBox();

}
