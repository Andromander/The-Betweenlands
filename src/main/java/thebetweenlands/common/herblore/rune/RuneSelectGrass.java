package thebetweenlands.common.herblore.rune;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import thebetweenlands.api.rune.INodeComposition;
import thebetweenlands.api.rune.INodeConfiguration;
import thebetweenlands.api.rune.impl.AbstractRune;
import thebetweenlands.api.rune.impl.PortNodeConfiguration;
import thebetweenlands.api.rune.impl.RuneStats;
import thebetweenlands.api.rune.impl.PortNodeConfiguration.InputPort;
import thebetweenlands.api.rune.impl.RuneChain.RuneExecutionContext;
import thebetweenlands.common.registries.AspectRegistry;

public final class RuneSelectGrass extends AbstractRune<RuneSelectGrass> {

	public static final class Blueprint extends AbstractRune.Blueprint<RuneSelectGrass> {
		public Blueprint() {
			super(RuneStats.builder()
					.aspect(AspectRegistry.ORDANIIS, 1)
					.duration(0.1f, 0.01f)
					.build());
		}

		private static final INodeConfiguration CONFIGURATION_1;

		private static final InputPort<Entity> IN_ENTITY;
		private static final InputPort<BlockPos> IN_POSITION;

		static {
			PortNodeConfiguration.Builder builder = PortNodeConfiguration.builder();

			IN_POSITION = builder.in(BlockPos.class);
			IN_ENTITY = builder.in(Entity.class);

			CONFIGURATION_1 = builder.build();
		}

		@Override
		public Set<INodeConfiguration> getConfigurations() {
			return ImmutableSet.of(CONFIGURATION_1);
		}

		@Override
		public RuneSelectGrass create(INodeComposition<RuneExecutionContext> composition, INodeConfiguration configuration) {
			return new RuneSelectGrass(this, composition, configuration);
		}

		@Override
		protected void activate(RuneSelectGrass state, RuneExecutionContext context, INodeIO io) {

			if (state.getConfiguration() == CONFIGURATION_1) {
				BlockPos position = IN_POSITION.get(io);
				Entity entity = IN_ENTITY.get(io);

				io.branch();

				Block block = entity.world.getBlockState(position).getBlock();
				if(block != Blocks.GRASS && block != Blocks.DIRT) {
					io.fail();
				}
			}

		}
	}

	private RuneSelectGrass(Blueprint blueprint, INodeComposition<RuneExecutionContext> composition, INodeConfiguration configuration) {
		super(blueprint, composition, configuration);
	}
}