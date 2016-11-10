package vswe.stevescarts.blocks.tileentities;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import vswe.stevescarts.blocks.BlockCartAssembler;
import vswe.stevescarts.blocks.BlockUpgrade;
import vswe.stevescarts.blocks.ModBlocks;
import vswe.stevescarts.containers.ContainerBase;
import vswe.stevescarts.containers.ContainerUpgrade;
import vswe.stevescarts.guis.GuiBase;
import vswe.stevescarts.guis.GuiUpgrade;
import vswe.stevescarts.helpers.NBTHelper;
import vswe.stevescarts.helpers.storages.ITankHolder;
import vswe.stevescarts.helpers.storages.Tank;
import vswe.stevescarts.helpers.storages.TransferHandler;
import vswe.stevescarts.upgrades.AssemblerUpgrade;
import vswe.stevescarts.upgrades.InterfaceEffect;
import vswe.stevescarts.upgrades.InventoryEffect;

public class TileEntityUpgrade extends TileEntityBase implements IInventory, ISidedInventory, IFluidHandler, IFluidTank, ITankHolder {
	public Tank tank;
	private TileEntityCartAssembler master;
	private int type;
	private boolean initialized;
	private NBTTagCompound comp;
	ItemStack[] inventoryStacks;
	private int[] slotsForSide;
	private EnumFacing side;

	@SideOnly(Side.CLIENT)
	@Override
	public GuiBase getGui(final InventoryPlayer inv) {
		return new GuiUpgrade(inv, this);
	}

	@Override
	public ContainerBase getContainer(final InventoryPlayer inv) {
		return new ContainerUpgrade(inv, this);
	}

	public void setMaster(final TileEntityCartAssembler master, EnumFacing side) {
		this.master = master;
		if(side == null){
			side = EnumFacing.NORTH; //TODO unconnected
		}
		this.side = side;
		BlockUpgrade blockUpgrade = (BlockUpgrade) ModBlocks.UPGRADE.getBlock();
		worldObj.setBlockState(pos, blockUpgrade.getDefaultState().withProperty(BlockUpgrade.FACING, side));
	}

	public EnumFacing getSide() {
		return side;
	}

	public TileEntityCartAssembler getMaster() {
		return this.master;
	}

	public void setType(final int type) {
		this.type = type;
		if (!this.initialized) {
			this.initialized = true;
			final AssemblerUpgrade upgrade = this.getUpgrade();
			if (upgrade != null) {
				this.comp = new NBTTagCompound();
				this.slotsForSide = new int[upgrade.getInventorySize()];
				upgrade.init(this);
				if (upgrade.getInventorySize() > 0) {
					this.inventoryStacks = new ItemStack[upgrade.getInventorySize()];
					for (int i = 0; i < this.slotsForSide.length; ++i) {
						this.slotsForSide[i] = i;
					}
				}
			} else {
				this.inventoryStacks = null;
			}
		}
	}

	public int getType() {
		return this.type;
	}

	public NBTTagCompound getCompound() {
		return this.comp;
	}

	public Packet getDescriptionPacket() {
		final NBTTagCompound var1 = new NBTTagCompound();
		this.writeToNBT(var1);
		return new SPacketUpdateTileEntity(this.pos, 1, var1);
	}

	@Override
	public void onDataPacket(final NetworkManager net, final SPacketUpdateTileEntity pkt) {
		this.readFromNBT(pkt.getNbtCompound());
	}

	public AssemblerUpgrade getUpgrade() {
		return AssemblerUpgrade.getUpgrade(this.type);
	}

	//	@SideOnly(Side.CLIENT)
	//	public IIcon getTexture(final boolean outside) {
	//		if (this.getUpgrade() == null) {
	//			return null;
	//		}
	//		return outside ? this.getUpgrade().getMainTexture() : this.getUpgrade().getSideTexture();
	//	}

	public boolean hasInventory() {
		return this.inventoryStacks != null;
	}

	@Override
	public void readFromNBT(final NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);
		this.setType(tagCompound.getByte("Type"));
		final NBTTagList items = tagCompound.getTagList("Items", NBTHelper.COMPOUND.getId());
		for (int i = 0; i < items.tagCount(); ++i) {
			final NBTTagCompound item = items.getCompoundTagAt(i);
			final int slot = item.getByte("Slot") & 0xFF;
			final ItemStack iStack = ItemStack.loadItemStackFromNBT(item);
			if (slot >= 0 && slot < this.getSizeInventory()) {
				this.setInventorySlotContents(slot, iStack);
			}
		}
		if(tagCompound.hasKey("Side")){
			side = EnumFacing.values()[tagCompound.getInteger("Side")];
			BlockPos sidePos = pos.offset(side);
			if(worldObj.getBlockState(sidePos).getBlock() == ModBlocks.CART_ASSEMBLER.getBlock()){
				((BlockCartAssembler)ModBlocks.CART_ASSEMBLER.getBlock()).updateMultiBlock(worldObj, sidePos);
			}
		}
		final AssemblerUpgrade upgrade = this.getUpgrade();
		if (upgrade != null) {
			upgrade.load(this, tagCompound);
		}
	}

	@Override
	public NBTTagCompound writeToNBT(final NBTTagCompound tagCompound) {
		super.writeToNBT(tagCompound);
		final NBTTagList items = new NBTTagList();
		if (this.inventoryStacks != null) {
			for (int i = 0; i < this.inventoryStacks.length; ++i) {
				final ItemStack iStack = this.inventoryStacks[i];
				if (iStack != null) {
					final NBTTagCompound item = new NBTTagCompound();
					item.setByte("Slot", (byte) i);
					iStack.writeToNBT(item);
					items.appendTag(item);
				}
			}
		}
		tagCompound.setTag("Items", items);
		tagCompound.setByte("Type", (byte) this.type);
		if(side != null){
			tagCompound.setInteger("Side", side.ordinal());
		}
		final AssemblerUpgrade upgrade = this.getUpgrade();
		if (upgrade != null) {
			upgrade.save(this, tagCompound);
		}
		return tagCompound;
	}

	@Override
	public boolean isUseableByPlayer(final EntityPlayer entityplayer) {
		return this.worldObj.getTileEntity(this.pos) == this && entityplayer.getDistanceSqToCenter(pos) <= 64.0;
	}

	@Override
	public void openInventory(EntityPlayer player)
	{

	}

	@Override
	public void closeInventory(EntityPlayer player)
	{

	}

	@Override
	public void updateEntity() {
		if (this.getUpgrade() != null && this.getMaster() != null) {
			this.getUpgrade().update(this);
		}
	}

	@Override
	public void initGuiData(final Container con, final IContainerListener crafting) {
		if (this.getUpgrade() != null) {
			final InterfaceEffect gui = this.getUpgrade().getInterfaceEffect();
			if (gui != null) {
				gui.checkGuiData(this, (ContainerUpgrade) con, crafting, true);
			}
		}
	}

	@Override
	public void checkGuiData(final Container con, final IContainerListener crafting) {
		if (this.getUpgrade() != null) {
			final InterfaceEffect gui = this.getUpgrade().getInterfaceEffect();
			if (gui != null) {
				gui.checkGuiData(this, (ContainerUpgrade) con, crafting, false);
			}
		}
	}

	@Override
	public void receiveGuiData(final int id, final short data) {
		if (this.getUpgrade() != null) {
			final InterfaceEffect gui = this.getUpgrade().getInterfaceEffect();
			if (gui != null) {
				gui.receiveGuiData(this, id, data);
			}
		}
	}

	@Override
	public int getSizeInventory() {
		if (this.inventoryStacks != null) {
			return this.inventoryStacks.length;
		}
		if (this.master == null) {
			return 0;
		}
		return this.master.getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot(final int i) {
		if (this.inventoryStacks == null) {
			if (this.master == null) {
				return null;
			}
			return this.master.getStackInSlot(i);
		} else {
			if (i < 0 || i >= this.getSizeInventory()) {
				return null;
			}
			return this.inventoryStacks[i];
		}
	}

	@Override
	public ItemStack decrStackSize(final int i, final int j) {
		if (this.inventoryStacks == null) {
			if (this.master == null) {
				return null;
			}
			return this.master.decrStackSize(i, j);
		} else {
			if (i < 0 || i >= this.getSizeInventory()) {
				return null;
			}
			if (this.inventoryStacks[i] == null) {
				return null;
			}
			if (this.inventoryStacks[i].stackSize <= j) {
				final ItemStack itemstack = this.inventoryStacks[i];
				this.inventoryStacks[i] = null;
				this.markDirty();
				return itemstack;
			}
			final ItemStack itemstack2 = this.inventoryStacks[i].splitStack(j);
			if (this.inventoryStacks[i].stackSize == 0) {
				this.inventoryStacks[i] = null;
			}
			this.markDirty();
			return itemstack2;
		}
	}

	@Nullable
	@Override
	public ItemStack removeStackFromSlot(int index)
	{
		return null;
	}

	@Override
	public void setInventorySlotContents(final int i, final ItemStack itemstack) {
		if (this.inventoryStacks == null) {
			if (this.master != null) {
				this.master.setInventorySlotContents(i, itemstack);
			}
		} else {
			if (i < 0 || i >= this.getSizeInventory()) {
				return;
			}
			this.inventoryStacks[i] = itemstack;
			if (itemstack != null && itemstack.stackSize > this.getInventoryStackLimit()) {
				itemstack.stackSize = this.getInventoryStackLimit();
			}
			this.markDirty();
		}
	}

	public String getInventoryName() {
		return "container.assemblerupgrade";
	}

	public boolean hasCustomInventoryName() {
		return false;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	public void closeInventory() {
	}

	public void openInventory() {
	}

	public ItemStack getStackInSlotOnClosing(final int i) {
		if (this.inventoryStacks == null) {
			if (this.master == null) {
				return null;
			}
			return this.master.getStackInSlot(i);
		} else {
			final ItemStack item = this.getStackInSlot(i);
			if (item != null) {
				this.setInventorySlotContents(i, null);
				return item;
			}
			return null;
		}
	}

	@Override
	public void markDirty() {
		if (this.getUpgrade() != null) {
			final InventoryEffect inv = this.getUpgrade().getInventoryEffect();
			if (inv != null) {
				inv.onInventoryChanged(this);
			}
		}
	}

	@Override
	public boolean isItemValidForSlot(final int slot, final ItemStack item) {
		if (this.getUpgrade() != null) {
			final InventoryEffect inv = this.getUpgrade().getInventoryEffect();
			if (inv != null) {
				return inv.isItemValid(slot, item);
			}
		}
		return this.getMaster() != null && this.getMaster().isItemValidForSlot(slot, item);
	}

	@Override
	public int getField(int id)
	{
		return 0;
	}

	@Override
	public void setField(int id, int value)
	{

	}

	@Override
	public int getFieldCount()
	{
		return 0;
	}

	@Override
	public void clear()
	{

	}

	@Override
	public boolean canInsertItem(final int slot, final ItemStack item, EnumFacing side) {
		if (this.getUpgrade() != null) {
			final InventoryEffect inv = this.getUpgrade().getInventoryEffect();
			if (inv != null) {
				return this.isItemValidForSlot(slot, item);
			}
		}
		return this.getMaster() != null && this.getMaster().canInsertItem(slot, item, side);
	}

	@Override
	public boolean canExtractItem(final int slot, final ItemStack item, EnumFacing side) {
		if (this.getUpgrade() != null) {
			final InventoryEffect inv = this.getUpgrade().getInventoryEffect();
			if (inv != null) {
				return true;
			}
		}
		return this.getMaster() != null && this.getMaster().canExtractItem(slot, item, side);
	}


	@Override
	public int fill(final EnumFacing from, final FluidStack resource, final boolean doFill) {
		return this.fill(resource, doFill);
	}

	@Override
	public FluidStack drain(final EnumFacing from, final FluidStack resource, final boolean doDrain) {
		if (resource != null && resource.isFluidEqual(this.getFluid())) {
			return this.drain(from, resource.amount, doDrain);
		}
		return null;
	}

	@Override
	public FluidStack drain(final EnumFacing from, final int maxDrain, final boolean doDrain) {
		return this.drain(maxDrain, doDrain);
	}

	@Override
	public FluidStack getFluid() {
		if (this.tank == null) {
			return null;
		}
		return this.tank.getFluid();
	}

	@Override
	public int getCapacity() {
		if (this.tank == null) {
			return 0;
		}
		return this.tank.getCapacity();
	}

	@Override
	public int fill(final FluidStack resource, final boolean doFill) {
		if (this.tank == null) {
			return 0;
		}
		final int result = this.tank.fill(resource, doFill);
		return result;
	}

	@Override
	public FluidStack drain(final int maxDrain, final boolean doDrain) {
		if (this.tank == null) {
			return null;
		}
		final FluidStack result = this.tank.drain(maxDrain, doDrain);
		return result;
	}

	@Override
	public ItemStack getInputContainer(final int tankid) {
		return this.getStackInSlot(0);
	}

	@Override
	public void clearInputContainer(final int tankid) {
		this.setInventorySlotContents(0, null);
	}

	@Override
	public void addToOutputContainer(final int tankid, final ItemStack item) {
		TransferHandler.TransferItem(item, this, 1, 1, new ContainerUpgrade(null, this), Slot.class, null, -1);
	}

	@Override
	public void onFluidUpdated(final int tankid) {
	}

	@Override
	public void drawImage(int p0, GuiBase p1, int p3, int p4, int p5, int p6, int p7, int p8)
	{

	}

	//	@SideOnly(Side.CLIENT)
	//	public void drawImage(final int tankid, final GuiBase gui, final IIcon icon, final int targetX, final int targetY, final int srcX, final int srcY, final int sizeX, final int sizeY) {
	//		gui.drawIcon(icon, gui.getGuiLeft() + targetX, gui.getGuiTop() + targetY, sizeX / 16.0f, sizeY / 16.0f, srcX / 16.0f, srcY / 16.0f);
	//	}

	@Override
	public int getFluidAmount() {
		return (this.tank == null) ? 0 : this.tank.getFluidAmount();
	}

	@Override
	public FluidTankInfo getInfo() {
		return (this.tank == null) ? null : this.tank.getInfo();
	}

	@Override
	public boolean canFill(final EnumFacing from, final Fluid fluid) {
		return true;
	}

	@Override
	public boolean canDrain(final EnumFacing from, final Fluid fluid) {
		return true;
	}

	@Override
	public FluidTankInfo[] getTankInfo(final EnumFacing from) {
		return new FluidTankInfo[] { this.getInfo() };
	}

	@Override
	public int[] getSlotsForFace(EnumFacing side){
		if (this.getUpgrade() != null) {
			final InventoryEffect inv = this.getUpgrade().getInventoryEffect();
			if (inv != null) {
				return this.slotsForSide;
			}
		}
		if (this.getMaster() != null) {
			return this.getMaster().getSlotsForFace(side);
		}
		return new int[0];
	}

	@Override
	public String getName()
	{
		return null;
	}

	@Override
	public boolean hasCustomName()
	{
		return false;
	}
}
