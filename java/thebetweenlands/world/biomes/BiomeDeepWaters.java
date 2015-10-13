package thebetweenlands.world.biomes;

import thebetweenlands.blocks.BLBlockRegistry;
import thebetweenlands.entities.mobs.*;
import thebetweenlands.world.WorldProviderBetweenlands;
import thebetweenlands.world.biomes.base.BiomeGenBaseBetweenlands;
import thebetweenlands.world.biomes.decorators.BiomeDecoratorDeepWaters;
import thebetweenlands.world.biomes.decorators.base.BiomeDecoratorBaseBetweenlands;
import thebetweenlands.world.biomes.feature.AlgaeNoiseFeature;
import thebetweenlands.world.biomes.feature.CragSpiresNoiseFeature;
import thebetweenlands.world.biomes.feature.DeepWaterNoiseFeature;
import thebetweenlands.world.biomes.feature.SiltNoiseFeature;

public class BiomeDeepWaters
extends BiomeGenBaseBetweenlands
{
	public BiomeDeepWaters(int biomeID) {
		this(biomeID, new BiomeDecoratorDeepWaters());
	}
	
	public BiomeDeepWaters(int biomeID, BiomeDecoratorBaseBetweenlands decorator) {
		super(biomeID, decorator);
		this.setFogColor((byte)10, (byte)30, (byte)12);
		setColors(0x314D31, 0x314D31);
		setWeight(20);
		this.setHeightAndVariation(WorldProviderBetweenlands.LAYER_HEIGHT - 20, 10);
		this.setBiomeName("Deep Waters");
		this.setBlocks(BLBlockRegistry.betweenstone, BLBlockRegistry.swampDirt, BLBlockRegistry.swampGrass, BLBlockRegistry.mud, BLBlockRegistry.betweenlandsBedrock);
		this.setFillerBlockHeight((byte) 5);
		this.waterColorMultiplier = 0x1b3944;
		this.addFeature(new DeepWaterNoiseFeature())
		.addFeature(new SiltNoiseFeature())
		.addFeature(new AlgaeNoiseFeature())
		.addFeature(new CragSpiresNoiseFeature());

		spawnableCreatureList.add(new SpawnListEntry(EntityFirefly.class, 25, 1, 3));
		spawnableMonsterList.add(new SpawnListEntry(EntitySwampHag.class, 20, 1, 1));
		spawnableMonsterList.add(new SpawnListEntry(EntityWight.class, 5, -1, -1));
		spawnableWaterCreatureList.add(new SpawnListEntry(EntityAngler.class, 20, 1, 2));
		spawnableWaterCreatureList.add(new SpawnListEntry(EntityLurker.class, 5, 1, 1));
		spawnableCreatureList.add(new SpawnListEntry(EntitySporeling.class, 100, 5, 8));
	}
}
