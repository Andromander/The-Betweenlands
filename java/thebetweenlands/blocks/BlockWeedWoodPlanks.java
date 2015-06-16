package thebetweenlands.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import thebetweenlands.creativetabs.ModCreativeTabs;

public class BlockWeedWoodPlanks extends Block {

	public BlockWeedWoodPlanks() {
		super(Material.wood);
		setHardness(2.0F);
		setResistance(5.0F);
		setStepSound(soundTypeWood);
		setBlockName("thebetweenlands.weedwoodPlanks");
		setBlockTextureName("thebetweenlands:weedwoodPlanks");
		setCreativeTab(ModCreativeTabs.blocks);
		setHardness(2.0F);
		setResistance(5.0F);
	}

}