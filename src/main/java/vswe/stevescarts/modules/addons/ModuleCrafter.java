package vswe.stevescarts.modules.addons;

import net.minecraft.item.ItemStack;
import vswe.stevescarts.containers.slots.SlotCartCrafter;
import vswe.stevescarts.containers.slots.SlotCartCrafterResult;
import vswe.stevescarts.entitys.EntityMinecartModular;
import vswe.stevescarts.guis.GuiMinecart;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public class ModuleCrafter extends ModuleRecipe {
	private CraftingDummy dummy;
	private int cooldown;

	public ModuleCrafter(final EntityMinecartModular cart) {
		super(cart);
		this.cooldown = 0;
		this.dummy = new CraftingDummy(this);
	}

	@Override
	public void update() {
		if (this.cooldown <= 0) {
			if (!this.getCart().world.isRemote && this.getValidSlot() != null) {
				@Nonnull
				ItemStack result = this.dummy.getResult();
				if (!result.isEmpty() && this.getCart().getModules() != null) {
					if (result.getCount() == 0) {
						result.setCount(1);
					}
					this.prepareLists();
					if (this.canCraftMoreOfResult(result)) {
						final ArrayList<ItemStack> originals = new ArrayList<>();
						for (int i = 0; i < this.allTheSlots.size(); ++i) {
							@Nonnull
							ItemStack item = this.allTheSlots.get(i).getStack();
							originals.add((item == null) ? null : item.copy());
						}
						final ArrayList<ItemStack> containers = new ArrayList<>();
						boolean valid = true;
						boolean edited = false;
						for (int j = 0; j < 9; ++j) {
							@Nonnull
							ItemStack recipe = this.getStack(j);
							if (!recipe.isEmpty()) {
								valid = false;
								for (int k = 0; k < this.inputSlots.size(); ++k) {
									@Nonnull
									ItemStack item2 = this.inputSlots.get(k).getStack();
									if (!item2.isEmpty() && item2.isItemEqual(recipe) && ItemStack.areItemStackTagsEqual(item2, recipe)) {
										edited = true;
										if (item2.getItem().hasContainerItem(item2)) {
											containers.add(item2.getItem().getContainerItem(item2));
										}
										@Nonnull
										ItemStack itemStack = item2;
										itemStack.shrink(1);
										if (item2.getCount() <= 0) {
											this.inputSlots.get(k).putStack(ItemStack.EMPTY);
										}
										valid = true;
										break;
									}
								}
								if (!valid) {
									break;
								}
							}
						}
						if (valid) {
							this.getCart().addItemToChest(result, this.getValidSlot(), null);
							if (result.getCount() > 0) {
								valid = false;
							} else {
								edited = true;
								for (int j = 0; j < containers.size(); ++j) {
									@Nonnull
									ItemStack container = containers.get(j);
									if (container != null) {
										this.getCart().addItemToChest(container, this.getValidSlot(), null);
										if (container.getCount() > 0) {
											valid = false;
											break;
										}
									}
								}
							}
						}
						if (!valid && edited) {
							for (int j = 0; j < this.allTheSlots.size(); ++j) {
								this.allTheSlots.get(j).putStack(originals.get(j));
							}
						}
					}
				}
			}
			this.cooldown = 40;
		} else {
			--this.cooldown;
		}
	}

	@Override
	protected int[] getArea() {
		return new int[] { 68, 44, 16, 16 };
	}

	@Override
	public boolean hasGui() {
		return true;
	}

	@Override
	public int getInventorySize() {
		return 10;
	}

	@Override
	public int generateSlots(int slotCount) {
		this.slotGlobalStart = slotCount;
		this.slotList = new ArrayList<>();
		for (int y = 0; y < 3; ++y) {
			for (int x = 0; x < 3; ++x) {
				this.slotList.add(new SlotCartCrafter(this.getCart(), slotCount++, 10 + 18 * x, 15 + 18 * y));
			}
		}
		this.slotList.add(new SlotCartCrafterResult(this.getCart(), slotCount++, 67, this.canUseAdvancedFeatures() ? 20 : 33));
		return slotCount;
	}

	@Override
	public void onInventoryChanged() {
		if (this.getCart().world.isRemote) {
			this.dummy.update();
		}
	}

	@Override
	public void drawForeground(final GuiMinecart gui) {
		super.drawForeground(gui);
		this.drawString(gui, this.getModuleName(), 8, 6, 4210752);
	}

	@Override
	public int guiWidth() {
		return this.canUseAdvancedFeatures() ? 120 : 95;
	}

	@Override
	public int guiHeight() {
		return 75;
	}

	@Override
	protected boolean canUseAdvancedFeatures() {
		return false;
	}

	@Override
	protected int getLimitStartX() {
		return 90;
	}

	@Override
	protected int getLimitStartY() {
		return 23;
	}
}
