package vswe.stevescarts.Containers;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import vswe.stevescarts.Slots.SlotCargo;
import vswe.stevescarts.TileEntities.TileEntityCargo;

import java.util.ArrayList;

public class ContainerCargo extends ContainerManager {
	public short lastTarget;

	public ContainerCargo(final IInventory invPlayer, final TileEntityCargo cargo) {
		super(cargo);
		cargo.cargoSlots = new ArrayList<SlotCargo>();
		cargo.lastLayout = -1;
		for (int i = 0; i < 60; ++i) {
			final SlotCargo slot = new SlotCargo(cargo, i);
			this.addSlotToContainer(slot);
			cargo.cargoSlots.add(slot);
		}
		this.addPlayer(invPlayer);
	}

	@Override
	protected int offsetX() {
		return 73;
	}
}