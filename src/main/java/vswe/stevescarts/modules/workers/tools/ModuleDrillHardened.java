package vswe.stevescarts.modules.workers.tools;

import net.minecraft.item.ItemStack;
import vswe.stevescarts.SCConfig;
import vswe.stevescarts.entitys.EntityMinecartModular;
import vswe.stevescarts.helpers.ComponentTypes;

import javax.annotation.Nonnull;

public class ModuleDrillHardened extends ModuleDrill {
	public ModuleDrillHardened(final EntityMinecartModular cart) {
		super(cart);
	}

	@Override
	protected int blocksOnTop() {
		return 5;
	}

	@Override
	protected int blocksOnSide() {
		return 2;
	}

	@Override
	protected float getTimeMult() {
		return 4.0f;
	}

	@Override
	public int getMaxDurability() {
		return 1000000;
	}

	@Override
	public String getRepairItemName() {
		return SCConfig.hardenedRepairName.isEmpty() ? ComponentTypes.REINFORCED_METAL.getLocalizedName(): SCConfig.hardenedRepairName;
	}

	@Override
	public int getRepairItemUnits(@Nonnull ItemStack item) {
		if (!item.isEmpty() && SCConfig.isValidRepairItem(item, "hardened")) {
			return 450000;
		}
		return 0;
	}

	@Override
	public int getRepairSpeed() {
		return 200;
	}

	@Override
	public boolean useDurability() {
		return true;
	}
}
