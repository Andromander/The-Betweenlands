package thebetweenlands.world.biomes;

import thebetweenlands.blocks.BLBlockRegistry;
import thebetweenlands.entities.mobs.EntityAngler;
import thebetweenlands.entities.mobs.EntityBlindCaveFish;
import thebetweenlands.entities.mobs.EntityDragonFly;
import thebetweenlands.entities.mobs.EntityFirefly;
import thebetweenlands.entities.mobs.EntityGecko;
import thebetweenlands.entities.mobs.EntityLurker;
import thebetweenlands.entities.mobs.EntitySporeling;
import thebetweenlands.entities.mobs.EntitySwampHag;
import thebetweenlands.entities.mobs.EntityWight;
import thebetweenlands.world.WorldProviderBetweenlands;
import thebetweenlands.world.biomes.base.BiomeGenBaseBetweenlands;
import thebetweenlands.world.biomes.decorators.BiomeDecoratorCoarseIslands;
import thebetweenlands.world.biomes.decorators.base.BiomeDecoratorBaseBetweenlands;
import thebetweenlands.world.biomes.feature.AlgaeNoiseFeature;
import thebetweenlands.world.biomes.feature.CoarseIslandNoiseFeature;
import thebetweenlands.world.biomes.feature.SiltNoiseFeature;

public class BiomeCoarseIslands
extends BiomeGenBaseBetweenlands
{
	public BiomeCoarseIslands(int biomeID) {
		this(biomeID, new BiomeDecoratorCoarseIslands());
	}

	public BiomeCoarseIslands(int biomeID, BiomeDecoratorBaseBetweenlands decorator) {
		super(biomeID, decorator);
		this.setFogColor((byte)10, (byte)30, (byte)12);
		setColors(0x314D31, 0x314D31);
		setWeight(20);
		this.setHeightAndVariation(WorldProviderBetweenlands.CAVE_START, 0);
		this.setBiomeName("Coarse Islands");
		this.setBlocks(BLBlockRegistry.betweenstone, BLBlockRegistry.swampDirt, BLBlockRegistry.swampGrass, BLBlockRegistry.mud, BLBlockRegistry.betweenlandsBedrock);
		this.setFillerBlockHeight((byte)1);
		this.addFeature(new CoarseIslandNoiseFeature())
		.addFeature(new SiltNoiseFeature())
		.addFeature(new AlgaeNoiseFeature());
		this.waterColorMultiplier = 0x1b3944;

		spawnableCreatureList.add(new SpawnListEntry(EntityDragonFly.class, 35, 2, 4));
		spawnableCreatureList.add(new SpawnListEntry(EntityFirefly.class, 25, 1, 3));
		spawnableCreatureList.add(new SpawnListEntry(EntityGecko.class, 35, 1, 3));

		spawnableWaterCreatureList.add(new SpawnListEntry(EntityLurker.class, 10, 1, 1));
		spawnableWaterCreatureList.add(new SpawnListEntry(EntityAngler.class, 25, 1, 2));
		spawnableWaterCreatureList.add(new SpawnListEntry(EntityBlindCaveFish.class, 500, 2, 5));

		spawnableCaveCreatureList.add(new SpawnListEntry(EntitySporeling.class, 200, 5, 8));

		spawnableMonsterList.add(new SpawnListEntry(EntitySwampHag.class, 15, 1, 1));
		spawnableMonsterList.add(new SpawnListEntry(EntityWight.class, 5, -1, -1));
	}

	/*private NoiseGeneratorPerlin islandNoiseGen;
	private double[] islandNoise = new double[256];

	@Override
	protected void initializeNoiseGenBiome(Random rng) { 
		this.islandNoiseGen = new NoiseGeneratorPerlin(rng, 4);
	}

	@Override
	protected void generateNoiseBiome(int chunkX, int chunkZ) { 
		this.islandNoise = this.islandNoiseGen.func_151599_a(this.islandNoise, (double) (chunkX * 16), (double) (chunkZ * 16), 16, 16, 0.08D * 2.0D, 0.08D * 2.0D, 1.0D);
	}

	@Override
	public int getRootHeight(int x, int z) {
		return WorldProviderBetweenlands.LAYER_HEIGHT + 10;
	}

	@Override
	public int getHeightVariation(int x, int z) {
		int cx = x % 16;
		if(cx < 0) {
			cx = (16 + cx);
		}
		int cz = z % 16;
		if(cz < 0) {
			cz = (16 + cz);
		}

		//System.out.println(cx + " " + cz);

		double noise = this.islandNoise[cx * 16 + cz] / 1.4f + 1.8f;
		int layerHeight = WorldProviderBetweenlands.LAYER_HEIGHT;
		if(noise <= 0) {
			return 80;
		}
		return 0;
	}*/
}
