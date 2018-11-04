package thebetweenlands.common.tile.spawner;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import thebetweenlands.client.render.particle.BLParticles;
import thebetweenlands.common.entity.mobs.EntitySmolSludgeWorm;

public class TileEntityMudBricksSpawnerHole extends TileEntity implements ITickable {
	private int spawnDelay = 20;
	private int minSpawnDelay = 100;
	private int maxSpawnDelay = 400;
	private int spawnCount = 1;
	private int maxNearbyEntities = 6;
	private int activatingRangeFromPlayer = 16;
	private int spawnRange = 4;

	private boolean isActivated() {
		return getWorld().isAnyPlayerWithinRangeAt((double) getPos().getX() + 0.5D, (double) getPos().getY() + 0.5D, (double) getPos().getZ() + 0.5D, (double) activatingRangeFromPlayer);
	}

	@Override
	public void update() {

		if (isActivated()) {
			if (getWorld().isRemote) {
				double d3 = (double) ((float) getPos().getX() + getWorld().rand.nextFloat());
				double d4 = (double) ((float) getPos().getY() + getWorld().rand.nextFloat());
				double d5 = (double) ((float) getPos().getZ() + getWorld().rand.nextFloat());
				BLParticles.CAVE_WATER_DRIP.spawn(getWorld(), d3, d4, d5).setRBGColorF(0.4118F, 0.2745F, 0.1568F);

				if (spawnDelay > 0)
					--spawnDelay;
			} else {
				if (spawnDelay == -1)
					resetTimer();
				if (spawnDelay > 0) {
					--spawnDelay;
					return;
				}
				boolean reset = false;
				for (int i = 0; i < spawnCount; ++i) {
					Entity entity = new EntitySmolSludgeWorm(getWorld());

					int k = getWorld().getEntitiesWithinAABB(entity.getClass(), (new AxisAlignedBB((double) getPos().getX(), (double) getPos().getY(), (double) getPos().getZ(), (double) (getPos().getX() + 1), (double) (getPos().getY() + 1), (double) (getPos().getZ() + 1))).grow((double) spawnRange)).size();

					if (k >= maxNearbyEntities) {
						resetTimer();
						return;
					}

					EntityLiving entityliving = entity instanceof EntityLiving ? (EntityLiving) entity : null;
					entity.setLocationAndAngles(getPos().getX() + 0.5F, getPos().getY() + 0.5F, getPos().getZ() + 0.5F, getWorld().rand.nextFloat() * 360.0F, 0.0F);

					if (entity instanceof EntityLiving)
						((EntityLiving) entity).onInitialSpawn(getWorld().getDifficultyForLocation(new BlockPos(entity)), (IEntityLivingData) null);
					getWorld().spawnEntity(entity);
					getWorld().playEvent(2004, getPos(), 0);
					reset = true;
				}

				if (reset)
					resetTimer();
			}
		}
	}

	private void resetTimer() {
		if (maxSpawnDelay <= minSpawnDelay)
			spawnDelay = minSpawnDelay;
		else {
			int i = maxSpawnDelay - minSpawnDelay;
			spawnDelay = minSpawnDelay + getWorld().rand.nextInt(i);
		}
		broadcastEvent(1);
	}

    public void broadcastEvent(int id) {
    	getWorld().addBlockEvent(getPos(), Blocks.MOB_SPAWNER, id, 0);
    }

	@Override
    public boolean receiveClientEvent(int id, int type) {
        return setDelayToMin(id) ? true : super.receiveClientEvent(id, type);
    }

    public boolean setDelayToMin(int delay) {
        if (delay == 1 && getWorld().isRemote)  {
            spawnDelay = minSpawnDelay;
            return true;
        }
        else
            return false;
    }

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		spawnDelay = nbt.getShort("Delay");

		if (nbt.hasKey("MinSpawnDelay", 99)) {
			minSpawnDelay = nbt.getShort("MinSpawnDelay");
			maxSpawnDelay = nbt.getShort("MaxSpawnDelay");
			spawnCount = nbt.getShort("SpawnCount");
		}

		if (nbt.hasKey("MaxNearbyEntities", 99)) {
			maxNearbyEntities = nbt.getShort("MaxNearbyEntities");
			activatingRangeFromPlayer = nbt.getShort("RequiredPlayerRange");
		}

		if (nbt.hasKey("SpawnRange", 99))
			spawnRange = nbt.getShort("SpawnRange");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setShort("Delay", (short) spawnDelay);
		nbt.setShort("MinSpawnDelay", (short) minSpawnDelay);
		nbt.setShort("MaxSpawnDelay", (short) maxSpawnDelay);
		nbt.setShort("SpawnCount", (short) spawnCount);
		nbt.setShort("MaxNearbyEntities", (short) maxNearbyEntities);
		nbt.setShort("RequiredPlayerRange", (short) activatingRangeFromPlayer);
		nbt.setShort("SpawnRange", (short) spawnRange);
		return nbt;
	}

	@Override
    public NBTTagCompound getUpdateTag() {
		NBTTagCompound nbt = new NBTTagCompound();
        return writeToNBT(nbt);
    }

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		NBTTagCompound nbt = new NBTTagCompound();
		writeToNBT(nbt);
		return new SPacketUpdateTileEntity(getPos(), 0, nbt);
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
		readFromNBT(packet.getNbtCompound());
	}
}
