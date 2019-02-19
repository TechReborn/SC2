package vswe.stevescarts.entitys;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockRailBase.EnumRailDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.client.audio.MovingSoundMinecart;
import net.minecraft.client.audio.MovingSoundMinecartRiding;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.minecart.MinecartInteractEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import vswe.stevescarts.StevesCarts;
import vswe.stevescarts.blocks.ModBlocks;
import vswe.stevescarts.blocks.tileentities.TileEntityCartAssembler;
import vswe.stevescarts.containers.ContainerMinecart;
import vswe.stevescarts.guis.GuiMinecart;
import vswe.stevescarts.helpers.*;
import vswe.stevescarts.helpers.storages.TransferHandler;
import vswe.stevescarts.items.ModItems;
import vswe.stevescarts.models.ModelCartbase;
import vswe.stevescarts.modules.IActivatorModule;
import vswe.stevescarts.modules.ModuleBase;
import vswe.stevescarts.modules.addons.ModuleCreativeSupplies;
import vswe.stevescarts.modules.data.ModuleData;
import vswe.stevescarts.modules.engines.ModuleEngine;
import vswe.stevescarts.modules.storages.chests.ModuleChest;
import vswe.stevescarts.modules.storages.tanks.ModuleTank;
import vswe.stevescarts.modules.workers.CompWorkModule;
import vswe.stevescarts.modules.workers.ModuleWorker;
import vswe.stevescarts.modules.workers.tools.ModuleTool;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

public class EntityMinecartModular extends EntityMinecart implements IInventory, IEntityAdditionalSpawnData, IFluidHandler {

	public BlockPos disabledPos;
	protected boolean wasDisabled;
	public double pushX;
	public double pushZ;
	public double temppushX;
	public double temppushZ;
	protected boolean engineFlag;
	private int motorRotation;
	public boolean cornerFlip;
	private EnumRailDirection fixedRailDirection;
	private BlockPos fixedRailPos;
	private byte[] moduleLoadingData;
	private ForgeChunkManager.Ticket cartTicket;
	private int wrongRender;
	private boolean oldRender;
	private float lastRenderYaw;
	private double lastMotionX;
	private double lastMotionZ;
	private int workingTime;
	private ModuleWorker workingComponent;
	public TileEntityCartAssembler placeholderAsssembler;
	public boolean isPlaceholder;
	public int keepAlive;
	public static final int MODULAR_SPACE_WIDTH = 443;
	public static final int MODULAR_SPACE_HEIGHT = 168;
	public int modularSpaceHeight;
	public boolean canScrollModules;
	private ArrayList<ModuleCountPair> moduleCounts;
	public static final int[][][] railDirectionCoordinates;
	private ArrayList<ModuleBase> modules;
	private ArrayList<ModuleWorker> workModules;
	private ArrayList<ModuleEngine> engineModules;
	private ArrayList<ModuleTank> tankModules;
	private ModuleCreativeSupplies creativeSupplies;
	public Random random;
	protected String name;
	public byte cartVersion;
	private int scrollY;
	@SideOnly(Side.CLIENT)
	private MovingSound sound;
	@SideOnly(Side.CLIENT)
	private MovingSound soundRiding;
	@SideOnly(Side.CLIENT)
	private int keepSilent;

	private static final DataParameter<Boolean> IS_BURNING = EntityDataManager.createKey(EntityMinecartModular.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Boolean> IS_DISANABLED = EntityDataManager.createKey(EntityMinecartModular.class, DataSerializers.BOOLEAN);

	public ArrayList<ModuleBase> getModules() {
		return modules;
	}

	public boolean hasModule(Class<? extends ModuleBase> module) {
		for (ModuleBase moduleBase : getModules()) {
			if (moduleBase.getClass().equals(module)) {
				return true;
			}
		}
		return false;
	}

	public ArrayList<ModuleWorker> getWorkers() {
		return workModules;
	}

	public ArrayList<ModuleEngine> getEngines() {
		return engineModules;
	}

	public ArrayList<ModuleTank> getTanks() {
		return tankModules;
	}

	public ArrayList<ModuleCountPair> getModuleCounts() {
		return moduleCounts;
	}

	public EntityMinecartModular(final World world, final double x, final double y, final double z, final NBTTagCompound info, final String name) {
		super(world, x, y, z);
		engineFlag = false;
		fixedRailDirection = null;
		random = new Random();
		cartVersion = info.getByte("CartVersion");
		loadModules(info);
		this.name = name;
		for (int i = 0; i < modules.size(); ++i) {
			if (modules.get(i).hasExtraData() && info.hasKey("Data" + i)) {
				modules.get(i).setExtraData(info.getByte("Data" + i));
			}
		}
	}

	public EntityMinecartModular(final World world) {
		super(world);
		engineFlag = false;
		fixedRailDirection = null;
		random = new Random();
	}

	public EntityMinecartModular(final World world, final TileEntityCartAssembler assembler, final byte[] data) {
		this(world);
		setPlaceholder(assembler);
		loadPlaceholderModules(data);
	}

	private void overrideDatawatcher() {
		dataManager = new EntityDataManagerLockable(this);
	}

	private void loadPlaceholderModules(final byte[] data) {
		if (modules == null) {
			modules = new ArrayList<>();
			doLoadModules(data);
		} else {
			final ArrayList<Byte> modulesToAdd = new ArrayList<>();
			final ArrayList<Byte> oldModules = new ArrayList<>();
			for (int i = 0; i < moduleLoadingData.length; ++i) {
				oldModules.add(moduleLoadingData[i]);
			}
			for (int i = 0; i < data.length; ++i) {
				boolean found = false;
				for (int j = 0; j < oldModules.size(); ++j) {
					if (data[i] == oldModules.get(j)) {
						found = true;
						oldModules.remove(j);
						break;
					}
				}
				if (!found) {
					modulesToAdd.add(data[i]);
				}
			}
			for (final byte id : oldModules) {
				for (int k = 0; k < modules.size(); ++k) {
					if (id == modules.get(k).getModuleId()) {
						modules.remove(k);
						break;
					}
				}
			}
			final byte[] newModuleData = new byte[modulesToAdd.size()];
			for (int l = 0; l < modulesToAdd.size(); ++l) {
				newModuleData[l] = modulesToAdd.get(l);
			}
			doLoadModules(newModuleData);
		}
		initModules();
		moduleLoadingData = data;
	}

	private void loadModules(final NBTTagCompound info) {
		final NBTTagByteArray moduleIDTag = (NBTTagByteArray) info.getTag("Modules");
		if (moduleIDTag == null) {
			return;
		}
		if (world.isRemote) {
			moduleLoadingData = moduleIDTag.getByteArray();
		} else {
			moduleLoadingData = CartVersion.updateCart(this, moduleIDTag.getByteArray());
		}
		loadModules(moduleLoadingData);
	}

	public void updateSimulationModules(final byte[] bytes) {
		if (!isPlaceholder) {
			System.out.println("You're stupid! This is not a placeholder cart.");
		} else {
			loadPlaceholderModules(bytes);
		}
	}

	protected void loadModules(final byte[] bytes) {
		modules = new ArrayList<>();
		doLoadModules(bytes);
		initModules();
	}

	private void doLoadModules(final byte[] bytes) {
		for (final byte id : bytes) {
			try {
				final Class<? extends ModuleBase> moduleClass = ModuleData.getList().get(id).getModuleClass();
				final Constructor moduleConstructor = moduleClass.getConstructor(EntityMinecartModular.class);
				final Object moduleObject = moduleConstructor.newInstance(this);
				final ModuleBase module = (ModuleBase) moduleObject;
				module.setModuleId(id);
				modules.add(module);
			} catch (Exception e) {
				System.out.println("Failed to load module with ID " + id + "! More info below.");
				e.printStackTrace();
			}
		}
	}

	private void initModules() {
		moduleCounts = new ArrayList<>();
		for (final ModuleBase module : modules) {
			final ModuleData data = ModuleData.getList().get(module.getModuleId());
			boolean found = false;
			for (final ModuleCountPair count : moduleCounts) {
				if (count.isContainingData(data)) {
					count.increase();
					found = true;
					break;
				}
			}
			if (!found) {
				moduleCounts.add(new ModuleCountPair(data));
			}
		}
		for (final ModuleBase module : modules) {
			module.preInit();
		}
		workModules = new ArrayList<>();
		engineModules = new ArrayList<>();
		tankModules = new ArrayList<>();
		final int x = 0;
		final int y = 0;
		final int maxH = 0;
		int guidata = 0;
		int packets = 0;
		if (world.isRemote) {
			generateModels();
		}
		for (final ModuleBase module2 : modules) {
			if (module2 instanceof ModuleWorker) {
				workModules.add((ModuleWorker) module2);
			} else if (module2 instanceof ModuleEngine) {
				engineModules.add((ModuleEngine) module2);
			} else if (module2 instanceof ModuleTank) {
				tankModules.add((ModuleTank) module2);
			} else {
				if (!(module2 instanceof ModuleCreativeSupplies)) {
					continue;
				}
				creativeSupplies = (ModuleCreativeSupplies) module2;
			}
		}
		final CompWorkModule sorter = new CompWorkModule();
		workModules.sort(sorter);
		if (!isPlaceholder) {
			final ArrayList<GuiAllocationHelper> lines = new ArrayList<>();
			int slots = 0;
			for (final ModuleBase module3 : modules) {
				if (module3.hasGui()) {
					boolean foundLine = false;
					for (final GuiAllocationHelper line : lines) {
						if (line.width + module3.guiWidth() <= MODULAR_SPACE_WIDTH) {
							module3.setX(line.width);
							final GuiAllocationHelper guiAllocationHelper = line;
							guiAllocationHelper.width += module3.guiWidth();
							line.maxHeight = Math.max(line.maxHeight, module3.guiHeight());
							line.modules.add(module3);
							foundLine = true;
							break;
						}
					}
					if (!foundLine) {
						final GuiAllocationHelper line2 = new GuiAllocationHelper();
						module3.setX(0);
						line2.width = module3.guiWidth();
						line2.maxHeight = module3.guiHeight();
						line2.modules.add(module3);
						lines.add(line2);
					}
					module3.setGuiDataStart(guidata);
					guidata += module3.numberOfGuiData();
					if (module3.hasSlots()) {
						slots = module3.generateSlots(slots);
					}
				}
				if (module3.numberOfDataWatchers() > 0) {
					module3.initDw();
				}
				module3.setPacketStart(packets);
				packets += module3.totalNumberOfPackets();
			}
			int currentY = 0;
			for (final GuiAllocationHelper line3 : lines) {
				for (final ModuleBase module4 : line3.modules) {
					module4.setY(currentY);
				}
				currentY += line3.maxHeight;
			}
			if (currentY > MODULAR_SPACE_HEIGHT) {
				canScrollModules = true;
			}
			modularSpaceHeight = currentY;
		}
		for (final ModuleBase module5 : modules) {
			module5.init();
		}
	}

	@Override
	public void setDead() {
		if (world.isRemote) {
			for (int var1 = 0; var1 < getSizeInventory(); ++var1) {
				setInventorySlotContents(var1, ItemStack.EMPTY);
			}
		}
		super.setDead();
		if (modules != null) {
			for (final ModuleBase module : modules) {
				module.onDeath();
			}
		}
		dropChunkLoading();
	}

	@SideOnly(Side.CLIENT)
	public void renderOverlay(final Minecraft minecraft) {
		if (modules != null) {
			for (final ModuleBase module : modules) {
				module.renderOverlay(minecraft);
			}
		}
	}

	@Override
	protected void entityInit() {
		if (world.isRemote && !(dataManager instanceof EntityDataManagerLockable)) {
			overrideDatawatcher();
		}
		super.entityInit();
		dataManager.register(IS_BURNING, false);
		dataManager.register(IS_DISANABLED, false);
	}

	public void updateFuel() {
		final int consumption = getConsumption();
		if (consumption > 0) {
			final ModuleEngine engine = getCurrentEngine();
			if (engine != null) {
				engine.consumeFuel(consumption);
				if (!isPlaceholder && world.isRemote && hasFuel() && !isDisabled()) {
					engine.smoke();
				}
			}
		}
		if (hasFuel()) {
			if (!engineFlag) {
				pushX = temppushX;
				pushZ = temppushZ;
			}
		} else if (engineFlag) {
			temppushX = pushX;
			temppushZ = pushZ;
			final double n = 0.0;
			pushZ = n;
			pushX = n;
		}
		setEngineBurning(hasFuel() && !isDisabled());
	}

	public boolean isEngineBurning() {
		return dataManager.get(IS_BURNING);
	}

	public void setEngineBurning(final boolean on) {
		dataManager.set(IS_BURNING, on);
	}

	private ModuleEngine getCurrentEngine() {
		if (modules == null) {
			return null;
		}
		for (final ModuleBase module : modules) {
			if (module.stopEngines()) {
				return null;
			}
		}
		final int consumption = getConsumption(true);
		final ArrayList<ModuleEngine> priority = new ArrayList<>();
		int mostImportant = -1;
		for (final ModuleEngine engine : engineModules) {
			if (engine.hasFuel(consumption) && (mostImportant == -1 || mostImportant >= engine.getPriority())) {
				if (engine.getPriority() < mostImportant) {
					priority.clear();
				}
				mostImportant = engine.getPriority();
				priority.add(engine);
			}
		}
		if (priority.size() > 0) {
			if (motorRotation >= priority.size()) {
				motorRotation = 0;
			}
			motorRotation = (motorRotation + 1) % priority.size();
			return priority.get(motorRotation);
		}
		return null;
	}

	public int getConsumption() {
		return getConsumption(!isDisabled() && isEngineBurning());
	}

	public int getConsumption(final boolean isMoving) {
		int consumption = isMoving ? 1 : 0;
		if (modules != null && !isPlaceholder) {
			for (final ModuleBase module : modules) {
				consumption += module.getConsumption(isMoving);
			}
		}
		return consumption;
	}

	@Override
	public float getEyeHeight() {
		return 0.9f;
	}

	@Override
	public double getMountedYOffset() {
		if (modules != null && !getPassengers().isEmpty()) {
			for (final ModuleBase module : modules) {
				final float offset = module.mountedOffset(getPassengers().get(0));
				if (offset != 0.0f) {
					return offset;
				}
			}
		}
		return super.getMountedYOffset();
	}

	@Override
	@Nonnull
	public ItemStack getCartItem() {
		if (modules != null) {
			@Nonnull
			ItemStack cart = ModuleData.createModularCart(this);
			if (name != null && !name.equals("") && !name.equals(ModItems.CARTS.getName())) {
				cart.setStackDisplayName(name);
			}
			return cart;
		}
		return new ItemStack(ModItems.CARTS);
	}

	@Override
	public void killMinecart(final DamageSource dmg) {
		setDead();
		if (dropOnDeath()) {
			entityDropItem(getCartItem(), 0.0f);
			for (int i = 0; i < getSizeInventory(); ++i) {
				@Nonnull
				ItemStack itemstack = getStackInSlot(i);
				if (!itemstack.isEmpty()) {
					final float f = random.nextFloat() * 0.8f + 0.1f;
					final float f2 = random.nextFloat() * 0.8f + 0.1f;
					final float f3 = random.nextFloat() * 0.8f + 0.1f;
					while (itemstack.getCount() > 0) {
						int j = random.nextInt(21) + 10;
						if (j > itemstack.getCount()) {
							j = itemstack.getCount();
						}
						@Nonnull
						ItemStack itemStack = itemstack;
						itemStack.shrink(j);
						final EntityItem entityitem = new EntityItem(world, posX + f, posY + f2, posZ + f3, new ItemStack(itemstack.getItem(), j, itemstack.getItemDamage()));
						final float f4 = 0.05f;
						entityitem.motionX = (float) random.nextGaussian() * f4;
						entityitem.motionY = (float) random.nextGaussian() * f4 + 0.2f;
						entityitem.motionZ = (float) random.nextGaussian() * f4;
						world.spawnEntity(entityitem);
					}
				}
			}
		}
	}

	public boolean dropOnDeath() {
		if (isPlaceholder) {
			return false;
		}
		if (modules != null) {
			for (final ModuleBase module : modules) {
				if (!module.dropOnDeath()) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public float getMaxCartSpeedOnRail() {
		float maxSpeed = super.getMaxCartSpeedOnRail();
		if (modules != null) {
			for (final ModuleBase module : modules) {
				final float tempMax = module.getMaxSpeed();
				if (tempMax < maxSpeed) {
					maxSpeed = tempMax;
				}
			}
		}
		return maxSpeed;
	}

	@Override
	public boolean isPoweredCart() {
		return engineModules.size() > 0;
	}

	public int getDefaultDisplayTileData() {
		return -1;
	}

	public int getMinecartType() {
		return -1;
	}

	public float[] getColor() {
		if (modules != null) {
			for (final ModuleBase module : getModules()) {
				final float[] color = module.getColor();
				if (color[0] != 1.0f || color[1] != 1.0f || color[2] != 1.0f) {
					return color;
				}
			}
		}
		return new float[] { 1.0f, 1.0f, 1.0f };
	}

	public int getYTarget() {
		if (modules != null) {
			for (ModuleBase module : getModules()) {
				int yTarget = module.getYTarget();
				if (yTarget != -1) {
					return yTarget;
				}
			}
		}
		return (int) posY;
	}

	public ModuleBase getInterfaceThief() {
		if (modules != null) {
			for (final ModuleBase module : getModules()) {
				if (module.doStealInterface()) {
					return module;
				}
			}
		}
		return null;
	}

	@Override
	public boolean attackEntityFrom(final DamageSource dmg, final float par2) {
		if (isPlaceholder) {
			return false;
		}
		if (modules != null) {
			for (final ModuleBase module : getModules()) {
				if (!module.receiveDamage(dmg, par2)) {
					return false;
				}
			}
		}
		return super.attackEntityFrom(dmg, par2);
	}

	@Override
	public boolean getIsInvulnerable() {
		if (this.modules != null)
		{
			for(ModuleBase module : this.modules) {

				if (!module.receiveDamage(null, 0)) {
					return true;
				}
			}
		}
		return super.getIsInvulnerable();
	}

	@Override
	public void onActivatorRailPass(final int x, final int y, final int z, final boolean active) {
		if (modules != null) {
			for (final ModuleBase module : modules) {
				module.activatedByRail(x, y, z, active);
			}
		}
	}

	@Override
	public void moveMinecartOnRail(BlockPos pos) {
		super.moveMinecartOnRail(pos);
		if (modules != null) {
			for (final ModuleBase module : modules) {
				module.moveMinecartOnRail(pos);
			}
		}
		IBlockState blockState = world.getBlockState(pos);
		IBlockState stateBelow = world.getBlockState(pos.down());
		int metaBelow = stateBelow.getBlock().getMetaFromState(stateBelow);
		EnumRailDirection railDirection = ((BlockRailBase) blockState.getBlock()).getRailDirection(world, pos, blockState, this);
		cornerFlip = ((railDirection == EnumRailDirection.SOUTH_EAST || railDirection == EnumRailDirection.SOUTH_WEST) && motionX < 0.0)
			|| ((railDirection == EnumRailDirection.NORTH_EAST || railDirection == EnumRailDirection.NORTH_WEST) && motionX > 0.0);
		if (ModBlocks.ADVANCED_DETECTOR.isAt(world, pos, blockState) && isDisabled()) {
			releaseCart();
		}
		boolean canBeDisabled = ModBlocks.ADVANCED_DETECTOR.isAt(world, pos, blockState)
			&& (stateBelow.getBlock() != ModBlocks.DETECTOR_UNIT.getBlock() || !DetectorType.getTypeFromSate(stateBelow).canInteractWithCart() || DetectorType.getTypeFromSate(stateBelow).shouldStopCart());
		final boolean forceUnDisable = wasDisabled && disabledPos != null && disabledPos.equals(pos);
		if (!forceUnDisable && wasDisabled) {
			wasDisabled = false;
		}
		canBeDisabled = (!forceUnDisable && canBeDisabled);
		if (canBeDisabled && !isDisabled()) {
			setIsDisabled(true);
			if (pushX != 0.0 || pushZ != 0.0) {
				temppushX = pushX;
				temppushZ = pushZ;
				final double n = 0.0;
				pushZ = n;
				pushX = n;
			}
			disabledPos = new BlockPos(pos);
		}
		if (fixedRailPos != null && !fixedRailPos.equals(pos)) {
			fixedRailDirection = null;
			fixedRailPos = new BlockPos(fixedRailPos.getX(), -1, fixedRailPos.getZ());
		}
	}

	public EnumRailDirection getRailDirection(final BlockPos pos) {
		ModuleBase.RAILDIRECTION dir = ModuleBase.RAILDIRECTION.DEFAULT;
		for (final ModuleBase module : getModules()) {
			dir = module.getSpecialRailDirection(pos);
			if (dir != ModuleBase.RAILDIRECTION.DEFAULT) {
				break;
			}
		}
		if (dir == ModuleBase.RAILDIRECTION.DEFAULT) {
			return null;
		}
		int Yaw = (int) (rotationYaw % 180.0f);
		if (Yaw < 0) {
			Yaw += 180;
		}
		final boolean flag = Yaw >= 45 && Yaw <= 135;
		if (fixedRailDirection == null) {
			switch (dir) {
				case FORWARD: {
					if (flag) {
						fixedRailDirection = EnumRailDirection.NORTH_SOUTH;
						break;
					}
					fixedRailDirection = EnumRailDirection.EAST_WEST;
					break;
				}
				case LEFT: {
					if (flag) {
						if (motionZ > 0.0) {
							fixedRailDirection = EnumRailDirection.NORTH_EAST;
							break;
						}
						if (motionZ <= 0.0) {
							fixedRailDirection = EnumRailDirection.SOUTH_WEST;
							break;
						}
						break;
					} else {
						if (motionX > 0.0) {
							fixedRailDirection = EnumRailDirection.NORTH_WEST;
							break;
						}
						if (motionX < 0.0) {
							fixedRailDirection = EnumRailDirection.SOUTH_EAST;
							break;
						}
						break;
					}
				}
				case RIGHT: {
					if (flag) {
						if (motionZ > 0.0) {
							fixedRailDirection = EnumRailDirection.NORTH_WEST;
							break;
						}
						if (motionZ <= 0.0) {
							fixedRailDirection = EnumRailDirection.SOUTH_EAST;
							break;
						}
						break;
					} else {
						if (motionX > 0.0) {
							fixedRailDirection = EnumRailDirection.SOUTH_WEST;
							break;
						}
						if (motionX < 0.0) {
							fixedRailDirection = EnumRailDirection.NORTH_EAST;
							break;
						}
						break;
					}
				}
				case NORTH: {
					if (flag) {
						if (motionZ > 0.0) {
							fixedRailDirection = EnumRailDirection.NORTH_SOUTH;
							break;
						}
						break;
					} else {
						if (motionX > 0.0) {
							fixedRailDirection = EnumRailDirection.SOUTH_WEST;
							break;
						}
						if (motionX < 0.0) {
							fixedRailDirection = EnumRailDirection.SOUTH_EAST;
							break;
						}
						break;
					}
				}
				default: {
					fixedRailDirection = null;
					break;
				}
			}
			if (fixedRailDirection == null) {
				return null;
			}
			fixedRailPos = new BlockPos(pos);
		}
		return fixedRailDirection;
	}

	public void resetRailDirection() {
		fixedRailDirection = null;
	}

	public void turnback() {
		pushX *= -1.0;
		pushZ *= -1.0;
		temppushX *= -1.0;
		temppushZ *= -1.0;
		motionX *= -1.0;
		motionY *= -1.0;
		motionZ *= -1.0;
	}

	public void releaseCart() {
		wasDisabled = true;
		setIsDisabled(false);
		pushX = temppushX;
		pushZ = temppushZ;
	}

	@Override
	public void markDirty() {
		if (modules != null) {
			for (final ModuleBase module : modules) {
				module.onInventoryChanged();
			}
		}
	}

	@Override
	public int getSizeInventory() {
		int slotCount = 0;
		if (modules != null) {
			for (final ModuleBase module : modules) {
				slotCount += module.getInventorySize();
			}
		}
		return slotCount;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	protected void moveAlongTrack(BlockPos pos, IBlockState state) {
		if (!getPassengers().isEmpty()) {
			Entity riddenByEntity = getPassengers().get(0);
			if (riddenByEntity instanceof EntityLivingBase) {
				final float move = ((EntityLivingBase) riddenByEntity).moveForward;
				((EntityLivingBase) riddenByEntity).moveForward = 0.0f;
				super.moveAlongTrack(pos, state);
				((EntityLivingBase) riddenByEntity).moveForward = move;
			} else {
				super.moveAlongTrack(pos, state);
			}
		} else {
			super.moveAlongTrack(pos, state);
		}
		double d2 = pushX * pushX + pushZ * pushZ;
		if (d2 > 1.0E-4 && motionX * motionX + motionZ * motionZ > 0.001) {
			d2 = MathHelper.sqrt(d2);
			pushX /= d2;
			pushZ /= d2;
			if (pushX * motionX + pushZ * motionZ < 0.0) {
				pushX = 0.0;
				pushZ = 0.0;
			} else {
				pushX = motionX;
				pushZ = motionZ;
			}
		}
	}

	@Override
	protected void applyDrag() {
		double d0 = pushX * pushX + pushZ * pushZ;
		engineFlag = (d0 > 1.0E-4);
		if (isDisabled()) {
			motionX = 0.0;
			motionY = 0.0;
			motionZ = 0.0;
		} else if (engineFlag) {
			d0 = MathHelper.sqrt(d0);
			pushX /= d0;
			pushZ /= d0;
			final double d2 = getPushFactor();
			motionX *= 0.800000011920929;
			motionY *= 0.0;
			motionZ *= 0.800000011920929;
			motionX += pushX * d2;
			motionZ += pushZ * d2;
		} else {
			motionX *= 0.9800000190734863;
			motionY *= 0.0;
			motionZ *= 0.9800000190734863;
		}
		super.applyDrag();
	}

	protected double getPushFactor() {
		if (modules != null) {
			for (final ModuleBase module : modules) {
				final double factor = module.getPushFactor();
				if (factor >= 0.0) {
					return factor;
				}
			}
		}
		return 0.05;
	}

	@Override
	public NBTTagCompound writeToNBT(final NBTTagCompound tagCompound) {
		super.writeToNBT(tagCompound);
		tagCompound.setString("cartName", name);
		tagCompound.setDouble("pushX", pushX);
		tagCompound.setDouble("pushZ", pushZ);
		tagCompound.setDouble("temppushX", temppushX);
		tagCompound.setDouble("temppushZ", temppushZ);
		tagCompound.setShort("workingTime", (short) workingTime);
		tagCompound.setByteArray("Modules", moduleLoadingData);
		tagCompound.setByte("CartVersion", cartVersion);
		if (modules != null) {
			for (int i = 0; i < modules.size(); ++i) {
				final ModuleBase module = modules.get(i);
				module.writeToNBT(tagCompound, i);
			}
		}
		return tagCompound;
	}

	@Override
	public void readFromNBT(final NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);
		name = tagCompound.getString("cartName");
		pushX = tagCompound.getDouble("pushX");
		pushZ = tagCompound.getDouble("pushZ");
		temppushX = tagCompound.getDouble("temppushX");
		temppushZ = tagCompound.getDouble("temppushZ");
		workingTime = tagCompound.getShort("workingTime");
		cartVersion = tagCompound.getByte("CartVersion");
		final int oldVersion = cartVersion;
		loadModules(tagCompound);
		if (modules != null) {
			for (int i = 0; i < modules.size(); ++i) {
				final ModuleBase module = modules.get(i);
				module.readFromNBT(tagCompound, i);
			}
		}
		if (oldVersion < 2) {
			int newSlot = -1;
			int slotCount = 0;
			for (final ModuleBase module2 : modules) {
				if (module2 instanceof ModuleTool) {
					newSlot = slotCount;
					break;
				}
				slotCount += module2.getInventorySize();
			}
			if (newSlot != -1) {
				@Nonnull
				ItemStack lastitem = ItemStack.EMPTY;
				for (int j = newSlot; j < getSizeInventory(); ++j) {
					@Nonnull
					ItemStack thisitem = getStackInSlot(j);
					setInventorySlotContents(j, lastitem);
					lastitem = thisitem;
				}
			}
		}
	}

	public boolean isDisabled() {
		return dataManager.get(IS_DISANABLED);
	}

	public void setIsDisabled(final boolean disabled) {
		dataManager.set(IS_DISANABLED, disabled);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		onCartUpdate();
		if (world.isRemote) {
			updateSounds();
		}
	}

	public void onCartUpdate() {
		if (modules != null) {
			updateFuel();
			for (final ModuleBase module : modules) {
				module.update();
			}
			for (final ModuleBase module : modules) {
				module.postUpdate();
			}
			work();
			setCurrentCartSpeedCapOnRail(getMaxCartSpeedOnRail());
		}
		if (isPlaceholder && keepAlive++ > 20) {
			setDead();
			placeholderAsssembler.resetPlaceholder();
		}
	}

	public boolean hasFuel() {
		if (isDisabled()) {
			return false;
		}
		if (modules != null) {
			for (final ModuleBase module : modules) {
				if (module.stopEngines()) {
					return false;
				}
			}
		}
		return hasFuelForModule();
	}

	public boolean hasFuelForModule() {
		if (isPlaceholder) {
			return true;
		}
		final int consumption = getConsumption(true);
		if (modules != null) {
			for (final ModuleBase module : modules) {
				if (module.hasFuel(consumption)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean isUsableByPlayer(final EntityPlayer entityplayer) {
		return entityplayer.getDistanceSq(x(), y(), z()) <= 64.0;
	}

	@Override
	public EnumActionResult applyPlayerInteraction(EntityPlayer entityplayer,
	                                               Vec3d vec,
	                                               EnumHand hand) {
		if (MinecraftForge.EVENT_BUS.post(new MinecartInteractEvent(this, entityplayer, hand))) {
			return EnumActionResult.SUCCESS;
		}
		if (isPlaceholder) {
			return EnumActionResult.FAIL;
		}
		if (modules != null && !entityplayer.isSneaking()) {
			boolean interupt = false;
			for (final ModuleBase module : modules) {
				if (module.onInteractFirst(entityplayer)) {
					interupt = true;
				}
			}
			if (interupt) {
				return EnumActionResult.SUCCESS;
			}
		}
		if (!world.isRemote) {
			if (!isDisabled() && !isPassenger(entityplayer)) {
				temppushX = posX - entityplayer.posX;
				temppushZ = posZ - entityplayer.posZ;
			}
			if (!isDisabled() && hasFuel() && pushX == 0.0 && pushZ == 0.0) {
				pushX = temppushX;
				pushZ = temppushZ;
			}
			FMLNetworkHandler.openGui(entityplayer, StevesCarts.instance, 0, world, getEntityId(), 0, 0);
			openInventory(entityplayer);
		}
		return EnumActionResult.SUCCESS;
	}

	public void loadChunks() {
		loadChunks(cartTicket, x() >> 4, z() >> 4);
	}

	public void loadChunks(final int chunkX, final int chunkZ) {
		loadChunks(cartTicket, chunkX, chunkZ);
	}

	public void loadChunks(final ForgeChunkManager.Ticket ticket) {
		loadChunks(ticket, x() >> 4, z() >> 4);
	}

	public void loadChunks(final ForgeChunkManager.Ticket ticket, final int chunkX, final int chunkZ) {
		if (world.isRemote || ticket == null)
			return;
		if (cartTicket == null)
			cartTicket = ticket;
		Set<ChunkPos> loadedChunks = ticket.getChunkList();

		ArrayList<ChunkPos> newChunks = new ArrayList<>();
		for (int i = -1; i <= 1; ++i)
			for (int j = -1; j <= 1; ++j)
				newChunks.add(new ChunkPos(chunkX + i, chunkZ + j));

		for (ChunkPos pos: loadedChunks)
			if (!newChunks.contains(pos))
				ForgeChunkManager.unforceChunk(cartTicket, pos);
		for (ChunkPos pos: newChunks)
			if (!loadedChunks.contains(pos))
				ForgeChunkManager.forceChunk(cartTicket, pos);
	}

	public void initChunkLoading() {
		if (world.isRemote || cartTicket != null) {
			return;
		}
		cartTicket = ForgeChunkManager.requestTicket(StevesCarts.instance, world, ForgeChunkManager.Type.ENTITY);
		if (cartTicket != null) {
			cartTicket.bindEntity(this);
			cartTicket.setChunkListDepth(9);
			loadChunks();
		}
	}

	public void dropChunkLoading() {
		if (world.isRemote) {
			return;
		}
		if (cartTicket != null) {
			ForgeChunkManager.releaseTicket(cartTicket);
			cartTicket = null;
		}
	}

	public void setWorker(final ModuleWorker worker) {
		if (workingComponent != null && worker != null) {
			workingComponent.stopWorking();
		}
		if ((workingComponent = worker) == null) {
			setWorkingTime(0);
		}
	}

	public ModuleWorker getWorker() {
		return workingComponent;
	}

	public void setWorkingTime(final int val) {
		workingTime = val;
	}

	private void work() {
		if (isPlaceholder) {
			return;
		}
		if (!world.isRemote && hasFuel()) {
			if (workingTime <= 0) {
				final ModuleWorker oldComponent = workingComponent;
				if (workingComponent != null) {
					final boolean result = workingComponent.work();
					if (workingComponent != null && oldComponent == workingComponent && workingTime <= 0 && !workingComponent.preventAutoShutdown()) {
						workingComponent.stopWorking();
					}
					if (result) {
						work();
						return;
					}
				}
				if (workModules != null) {
					for (final ModuleWorker module : workModules) {
						if (module.work()) {
							return;
						}
					}
				}
			} else {
				--workingTime;
			}
		}
	}

	public void handleActivator(final ActivatorOption option, final boolean isOrange) {
		for (final ModuleBase module : modules) {
			if (module instanceof IActivatorModule && option.getModule().isAssignableFrom(module.getClass())) {
				final IActivatorModule iactivator = (IActivatorModule) module;
				if (option.shouldActivate(isOrange)) {
					iactivator.doActivate(option.getId());
				} else if (option.shouldDeactivate(isOrange)) {
					iactivator.doDeActivate(option.getId());
				} else {
					if (!option.shouldToggle()) {
						continue;
					}
					if (iactivator.isActive(option.getId())) {
						iactivator.doDeActivate(option.getId());
					} else {
						iactivator.doActivate(option.getId());
					}
				}
			}
		}
	}

	public boolean getRenderFlippedYaw(float yaw) {
		yaw %= 360.0f;
		if (yaw < 0.0f) {
			yaw += 360.0f;
		}
		if (!oldRender || Math.abs(yaw - lastRenderYaw) < 90.0f || Math.abs(yaw - lastRenderYaw) > 270.0f || (motionX > 0.0 && lastMotionX < 0.0) || (motionZ > 0.0 && lastMotionZ < 0.0)
			|| (motionX < 0.0 && lastMotionX > 0.0) || (motionZ < 0.0 && lastMotionZ > 0.0) || wrongRender >= 50) {
			lastMotionX = motionX;
			lastMotionZ = motionZ;
			lastRenderYaw = yaw;
			oldRender = true;
			wrongRender = 0;
			return false;
		}
		++wrongRender;
		return true;
	}

	public ArrayList<String> getLabel() {
		final ArrayList<String> label = new ArrayList<>();
		if (getModules() != null) {
			for (final ModuleBase module : getModules()) {
				module.addToLabel(label);
			}
		}
		return label;
	}

	public int x() {
		return MathHelper.floor(posX);
	}

	public int y() {
		return MathHelper.floor(posY);
	}

	public int z() {
		return MathHelper.floor(posZ);
	}

	public BlockPos getExactPosition() {
		return new BlockPos(x(), y(), z());
	}

	public void addItemToChest(@Nonnull ItemStack iStack) {
		TransferHandler.TransferItem(iStack, this, getCon(null), Slot.class, null, -1);
	}

	public void addItemToChest(@Nonnull ItemStack iStack, final int start, final int end) {
		TransferHandler.TransferItem(iStack, this, start, end, getCon(null), Slot.class, null, -1);
	}

	public void addItemToChest(@Nonnull ItemStack iStack, final Class validSlot, final Class invalidSlot) {
		TransferHandler.TransferItem(iStack, this, getCon(null), validSlot, invalidSlot, -1);
	}

	@Override
	@Nonnull
	public ItemStack removeStackFromSlot(int index) {
		if (!getStackInSlot(index).isEmpty()) {
			@Nonnull
			ItemStack var2 = getStackInSlot(index);
			setInventorySlotContents(index, ItemStack.EMPTY);
			return var2;
		}
		return ItemStack.EMPTY;
	}

	@Override
	@Nonnull
	public ItemStack getStackInSlot(int i) {
		if (modules != null) {
			for (final ModuleBase module : modules) {
				if (i < module.getInventorySize()) {
					return module.getStack(i);
				}
				i -= module.getInventorySize();
			}
		}
		return ItemStack.EMPTY;
	}

	@Override
	public void setInventorySlotContents(int i, @Nonnull ItemStack item) {
		if (modules != null) {
			for (final ModuleBase module : modules) {
				if (i < module.getInventorySize()) {
					module.setStack(i, item);
					break;
				}
				i -= module.getInventorySize();
			}
		}
	}

	@Override
	@Nonnull
	public ItemStack decrStackSize(final int i, final int n) {
		if (modules == null) {
			return ItemStack.EMPTY;
		}
		if (getStackInSlot(i).isEmpty()) {
			return ItemStack.EMPTY;
		}
		if (getStackInSlot(i).getCount() <= n) {
			@Nonnull
			ItemStack item = getStackInSlot(i);
			setInventorySlotContents(i, ItemStack.EMPTY);
			return item;
		}
		@Nonnull
		ItemStack item = getStackInSlot(i).splitStack(n);
		if (getStackInSlot(i).getCount() == 0) {
			setInventorySlotContents(i, ItemStack.EMPTY);
		}
		return item;
	}

	public Container getCon(final InventoryPlayer player) {
		return new ContainerMinecart(player, this);
	}

	@Override
	public void openInventory(EntityPlayer player) {
		if (modules != null) {
			for (final ModuleBase module : modules) {
				if (module instanceof ModuleChest) {
					((ModuleChest) module).openChest();
				}
			}
		}
	}

	@Override
	public void closeInventory(EntityPlayer player) {
		if (modules != null) {
			for (final ModuleBase module : modules) {
				if (module instanceof ModuleChest) {
					((ModuleChest) module).closeChest();
				}
			}
		}
	}

	public void setPlaceholder(final TileEntityCartAssembler assembler) {
		isPlaceholder = true;
		placeholderAsssembler = assembler;
	}

	@Override
	public AxisAlignedBB getEntityBoundingBox() {
		if (isPlaceholder) {
			return null;
		}
		return super.getEntityBoundingBox();
	}

	@Override
	public boolean canBeCollidedWith() {
		return !isPlaceholder && super.canBeCollidedWith();
	}

	@Override
	public boolean canBePushed() {
		return !isPlaceholder && super.canBePushed();
	}

	@SideOnly(Side.CLIENT)
	private void generateModels() {
		if (modules != null) {
			final ArrayList<String> invalid = new ArrayList<>();
			for (final ModuleBase module : modules) {
				final ModuleData data = module.getData();
				if (data.haveRemovedModels()) {
					for (final String remove : data.getRemovedModels()) {
						invalid.add(remove);
					}
				}
			}
			for (int i = modules.size() - 1; i >= 0; --i) {
				final ModuleBase module = modules.get(i);
				final ModuleData data = module.getData();
				if (data != null && data.haveModels(isPlaceholder)) {
					final ArrayList<ModelCartbase> models = new ArrayList<>();
					for (final String str : data.getModels(isPlaceholder).keySet()) {
						if (!invalid.contains(str)) {
							models.add(data.getModels(isPlaceholder).get(str));
							invalid.add(str);
						}
					}
					if (models.size() > 0) {
						module.setModels(models);
					}
				}
			}
		}
	}

	@SideOnly(Side.CLIENT)
	public GuiScreen getGui(final EntityPlayer player) {
		return new GuiMinecart(player.inventory, this);
	}

	@Override
	public void writeSpawnData(final ByteBuf data) {
		data.writeByte(moduleLoadingData.length);
		for (final byte b : moduleLoadingData) {
			data.writeByte(b);
		}
		data.writeByte(name.getBytes().length);
		for (final byte b : name.getBytes()) {
			data.writeByte(b);
		}
	}

	@Override
	public void readSpawnData(final ByteBuf data) {
		final byte length = data.readByte();
		final byte[] bytes = new byte[length];
		data.readBytes(bytes);
		loadModules(bytes);
		final int nameLength = data.readByte();
		final byte[] nameBytes = new byte[nameLength];
		for (int i = 0; i < nameLength; ++i) {
			nameBytes[i] = data.readByte();
		}
		name = new String(nameBytes);
		if (getDataManager() instanceof EntityDataManagerLockable) {
			((EntityDataManagerLockable) getDataManager()).release();
		}
	}

	public void setScrollY(final int val) {
		if (canScrollModules) {
			scrollY = val;
		}
	}

	public int getScrollY() {
		if (getInterfaceThief() != null) {
			return 0;
		}
		return scrollY;
	}

	public int getRealScrollY() {
		return (int) ((modularSpaceHeight - MODULAR_SPACE_HEIGHT) / 198.0f * getScrollY());
	}

	@Override
	public int fill(final FluidStack resource, final boolean doFill) {
		int amount = 0;
		if (resource != null && resource.amount > 0) {
			final FluidStack fluid = resource.copy();
			for (int i = 0; i < tankModules.size(); ++i) {
				final int tempAmount = tankModules.get(i).fill(fluid, doFill);
				amount += tempAmount;
				final FluidStack fluidStack = fluid;
				fluidStack.amount -= tempAmount;
				if (fluid.amount <= 0) {
					break;
				}
			}
		}
		return amount;
	}

	@Override
	public FluidStack drain(final int maxDrain, final boolean doDrain) {
		return drain(maxDrain, doDrain);
	}

	@Override
	public FluidStack drain(final FluidStack resource, final boolean doDrain) {
		return drain(resource, (resource == null) ? 0 : resource.amount, doDrain);
	}

	private FluidStack drain(final FluidStack resource, int maxDrain, final boolean doDrain) {
		FluidStack ret = resource;
		if (ret != null) {
			ret = ret.copy();
			ret.amount = 0;
		}
		for (int i = 0; i < tankModules.size(); ++i) {
			FluidStack temp = null;
			temp = tankModules.get(i).drain(maxDrain, doDrain);
			if (temp != null && (ret == null || ret.isFluidEqual(temp))) {
				if (ret == null) {
					ret = temp;
				} else {
					final FluidStack fluidStack = ret;
					fluidStack.amount += temp.amount;
				}
				maxDrain -= temp.amount;
				if (maxDrain <= 0) {
					break;
				}
			}
		}
		if (ret != null && ret.amount == 0) {
			return null;
		}
		return ret;
	}

	public int drain(final Fluid type, int maxDrain, final boolean doDrain) {
		int amount = 0;
		if (type != null && maxDrain > 0) {
			for (final ModuleTank tank : tankModules) {
				final FluidStack drained = tank.drain(maxDrain, false);
				if (drained != null && type.equals(drained.getFluid())) {
					amount += drained.amount;
					maxDrain -= drained.amount;
					if (doDrain) {
						tank.drain(drained.amount, true);
					}
					if (maxDrain <= 0) {
						break;
					}
					continue;
				}
			}
		}
		return amount;
	}

	@Override
	public IFluidTankProperties[] getTankProperties() {
		IFluidTankProperties[] ret = new IFluidTankProperties[tankModules.size()];
		for (int i = 0; i < ret.length; ++i) {
			ret[i] = new FluidTankProperties(tankModules.get(i).getFluid(), tankModules.get(i).getCapacity());
		}
		return ret;
	}

	@Override
	public boolean isItemValidForSlot(int slot,
	                                  @Nonnull
		                                  ItemStack item) {
		if (modules != null) {
			for (final ModuleBase module : modules) {
				if (slot < module.getInventorySize()) {
					return module.getSlots().get(slot).isItemValid(item);
				}
				slot -= module.getInventorySize();
			}
		}
		return false;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	public String getInventoryName() {
		return "container.modularcart";
	}

	@Override
	public String getName() {
		return getCartName();
	}

	public String getCartName() {
		if (name == null || name.length() == 0) {
			return "Modular Cart";
		}
		return name;
	}

	public boolean hasCreativeSupplies() {
		return creativeSupplies != null;
	}

	@Override
	public boolean canRiderInteract() {
		return true;
	}

	@SideOnly(Side.CLIENT)
	public void setSound(final MovingSound sound, final boolean riding) {
		if (riding) {
			soundRiding = sound;
		} else {
			this.sound = sound;
		}
	}

	@SideOnly(Side.CLIENT)
	public void silent() {
		keepSilent = 6;
	}

	@SideOnly(Side.CLIENT)
	private void updateSounds() {
		if (keepSilent > 1) {
			--keepSilent;
			stopSound(sound);
			stopSound(soundRiding);
			sound = null;
			soundRiding = null;
		} else if (keepSilent == 1) {
			keepSilent = 0;
			Minecraft.getMinecraft().getSoundHandler().playSound(new MovingSoundMinecart(this));
			Minecraft.getMinecraft().getSoundHandler().playSound(new MovingSoundMinecartRiding(Minecraft.getMinecraft().player, this));
		}
	}

	@SideOnly(Side.CLIENT)
	private void stopSound(final MovingSound sound) {
		if (sound != null) {
			ReflectionHelper.setPrivateValue(MovingSound.class, sound, true, 0);
		}
	}

	static {
		railDirectionCoordinates = new int[][][]
		{
				{ {  0,  0, -1 }, {  0,  0, 1 } },
				{ { -1,  0,  0 }, {  1,  0, 0 } },
				{ { -1, -1,  0 }, {  1,  0, 0 } },
				{ { -1,  0,  0 }, {  1, -1, 0 } },
				{ {  0,  0, -1 }, {  0, -1, 1 } },
				{ {  0, -1, -1 }, {  0,  0, 1 } },
				{ {  0,  0,  1 }, {  1,  0, 0 } },
				{ {  0,  0,  1 }, { -1,  0, 0 } },
				{ {  0,  0, -1 }, { -1,  0, 0 } },
				{ {  0,  0, -1 }, {  1,  0, 0 } }
		};
	}

	@Override
	public int getField(int id) {
		return 0;
	}

	@Override
	public void setField(int id, int value) {
	}

	@Override
	public int getFieldCount() {
		return 0;
	}

	@Override
	public void clear() {
	}

	@Override
	public Type getType() {
		return null;
	}

	public Entity getCartRider() {
		return getPassengers().isEmpty() ? null : getPassengers().get(0);
	}

	@Nullable
	@Override
	public Entity getControllingPassenger() {
		return null; //Works when returning null, not sure why
	}

	public EntityDataManager getDataManager() {
		return dataManager;
	}

	int base = 0;

	public int getNextDataWatcher() {
		base++;
		return getDataManager().getAll().size() + base + 1;
	}
}
