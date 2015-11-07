package thebetweenlands.inventory.slot;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import thebetweenlands.items.misc.ItemSwampTalisman;
import thebetweenlands.items.misc.ItemSwampTalisman.EnumTalisman;

public class SlotDruidAltar extends Slot {

	public SlotDruidAltar(IInventory inventory, int slotIndex, int x, int y) {
		super(inventory, slotIndex, x, y);
		slotNumber = slotIndex;
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		if(slotNumber == 0) // && stack.getItem() instanceof SwampTalisman && stack.getItemDamage() == TALISMAN.swampTalisman.ordinal()
			//Player should not be able to put the talisman back in
			return false;
		if(slotNumber > 0 && slotNumber <= 4 && stack.getItem() instanceof ItemSwampTalisman && stack.getItemDamage() != EnumTalisman.SWAMP_TALISMAN.ordinal())
			return true;
		return false;
	}

	//Moved
	/*@Override
	public void onPickupFromSlot(EntityPlayer player, ItemStack stack) {
		if (stack != null && slotNumber == 0)
			player.worldObj.playSoundAtEntity(player, "thebetweenlands:druidchant", 1.0F, 1.0F);

		super.onPickupFromSlot(player, stack);
	}*/
}
