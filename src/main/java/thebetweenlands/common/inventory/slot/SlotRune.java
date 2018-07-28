package thebetweenlands.common.inventory.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import thebetweenlands.common.inventory.container.ContainerRuneChainAltar;
import thebetweenlands.common.tile.TileEntityRuneChainAltar;

public class SlotRune extends Slot {
	private final TileEntityRuneChainAltar altar;
	private final ContainerRuneChainAltar.Page page;

	public SlotRune(TileEntityRuneChainAltar altar, int index, int xPosition, int yPosition, ContainerRuneChainAltar.Page page) {
		super(altar, index, xPosition, yPosition);
		this.altar = altar;
		this.page = page;
	}

	public ContainerRuneChainAltar.Page getPage() {
		return this.page;
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		return this.altar.isItemValidForSlot(this.slotNumber, stack) && this.page.isInteractable() && this.getSlotIndex() - TileEntityRuneChainAltar.NON_INPUT_SLOTS < this.altar.getChainLength() + 1;
	}

	@Override
	public boolean canTakeStack(EntityPlayer playerIn) {
		return this.page.isInteractable() && this.getSlotIndex() - TileEntityRuneChainAltar.NON_INPUT_SLOTS < this.altar.getChainLength() + 1;
	}

	@Override
	public boolean isHere(IInventory inv, int slotIn) {
		return super.isHere(inv, slotIn) && this.page.isInteractable() && this.getSlotIndex() - TileEntityRuneChainAltar.NON_INPUT_SLOTS < this.altar.getChainLength() + 1;
	}

	@Override
	public boolean isEnabled() {
		return this.page.isCurrent() && this.getSlotIndex() - TileEntityRuneChainAltar.NON_INPUT_SLOTS < this.altar.getChainLength() + 1;
	}
}
