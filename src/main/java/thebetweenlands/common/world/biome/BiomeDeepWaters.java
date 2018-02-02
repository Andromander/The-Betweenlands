package thebetweenlands.common.world.biome;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import thebetweenlands.common.entity.mobs.EntityAngler;
import thebetweenlands.common.entity.mobs.EntityBlindCaveFish;
import thebetweenlands.common.entity.mobs.EntityChiromaw;
import thebetweenlands.common.entity.mobs.EntityFirefly;
import thebetweenlands.common.entity.mobs.EntityLurker;
import thebetweenlands.common.entity.mobs.EntitySporeling;
import thebetweenlands.common.entity.mobs.EntityWight;
import thebetweenlands.common.lib.ModInfo;
import thebetweenlands.common.world.WorldProviderBetweenlands;
import thebetweenlands.common.world.biome.spawning.spawners.CaveSpawnEntry;
import thebetweenlands.common.world.biome.spawning.spawners.SporelingSpawnEntry;
import thebetweenlands.common.world.biome.spawning.spawners.SurfaceSpawnEntry;
import thebetweenlands.common.world.biome.spawning.spawners.SwampHagCaveSpawnEntry;
import thebetweenlands.common.world.biome.spawning.spawners.TreeSpawnEntry;
import thebetweenlands.common.world.gen.biome.decorator.BiomeDecoratorDeepWaters;
import thebetweenlands.common.world.gen.biome.feature.AlgaeFeature;
import thebetweenlands.common.world.gen.biome.feature.CragSpiresFeature;
import thebetweenlands.common.world.gen.biome.feature.DeepWatersFeature;

public class BiomeDeepWaters extends BiomeBetweenlands {

	public BiomeDeepWaters() {
		super(new ResourceLocation(ModInfo.ID, "deep_waters"),
				new BiomeProperties("Deep Waters")
				.setBaseHeight(WorldProviderBetweenlands.LAYER_HEIGHT - 12)
				.setHeightVariation(5.0F)
				.setWaterColor(0x1b3944)
				.setTemperature(0.8F)
				.setRainfall(0.9F));
		
		this.setWeight(12);
		this.getBiomeGenerator().setDecorator(new BiomeDecoratorDeepWaters(this))
		.addFeature(new DeepWatersFeature())
		.addFeature(new AlgaeFeature())
		.addFeature(new CragSpiresFeature());
		this.setFoliageColors(0xE5F745, 0xE5F745);
	}

	@Override
	public void addTypes() {
		BiomeDictionary.addTypes(this, Type.SWAMP, Type.WATER);
	}

	@Override
	protected void addSpawnEntries() {
		super.addSpawnEntries();

		this.blSpawnEntries.add(new SurfaceSpawnEntry(0, EntityFirefly.class, (short) 10).setSpawnCheckRadius(32.0D));
		this.blSpawnEntries.add(new SporelingSpawnEntry(1, EntitySporeling.class, (short) 80).setGroupSize(2, 5).setSpawnCheckRadius(32.0D));
		this.blSpawnEntries.add(new CaveSpawnEntry(2, EntityBlindCaveFish.class, (short) 30).setCanSpawnInWater(true).setGroupSize(3, 5).setSpawnCheckRadius(32.0D));

		this.blSpawnEntries.add(new SurfaceSpawnEntry(3, EntityLurker.class, (short) 35).setCanSpawnInWater(true).setHostile(true).setSpawnCheckRadius(16.0D));
		this.blSpawnEntries.add(new SurfaceSpawnEntry(4, EntityAngler.class, (short) 45).setCanSpawnInWater(true).setHostile(true).setGroupSize(1, 3));
		this.blSpawnEntries.add(new CaveSpawnEntry(5, EntityAngler.class, (short) 35).setCanSpawnInWater(true).setHostile(true).setGroupSize(1, 3));
		this.blSpawnEntries.add(new SwampHagCaveSpawnEntry(6, (short) 120).setHostile(true).setSpawnCheckRadius(24.0D).setGroupSize(1, 3));
		this.blSpawnEntries.add(new CaveSpawnEntry(7, EntityWight.class, (short) 18).setHostile(true).setSpawnCheckRadius(64.0D));
		this.blSpawnEntries.add(new CaveSpawnEntry(8, EntityChiromaw.class, (short) 60).setHostile(true).setSpawnCheckRadius(20.0D).setGroupSize(1, 3));
	}
}
