package vswe.stevescarts.Modules.Addons;

import net.minecraft.item.ItemStack;
import vswe.stevescarts.Carts.MinecartModular;

public class ModuleCreativeIncinerator extends ModuleIncinerator {
	public ModuleCreativeIncinerator(final MinecartModular cart) {
		super(cart);
	}

	@Override
	protected int getIncinerationCost() {
		return 0;
	}

	@Override
	protected boolean isItemValid(final ItemStack item) {
		return item != null;
	}

	@Override
	public boolean hasGui() {
		return false;
	}
}