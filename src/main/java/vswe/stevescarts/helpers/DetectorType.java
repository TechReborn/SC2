package vswe.stevescarts.helpers;

import java.util.HashMap;

import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import vswe.stevescarts.blocks.BlockRailAdvDetector;
import vswe.stevescarts.blocks.ModBlocks;
import vswe.stevescarts.blocks.tileentities.TileEntityDetector;
import vswe.stevescarts.entitys.EntityMinecartModular;

public enum DetectorType implements IStringSerializable {
	NORMAL(0, true, false, true, new String[] { "detector_manager_bot", "detector_manager_top", "detector_manager_yellow", "detector_manager_blue", "detector_manager_green", "detector_manager_red" }),
	UNIT(1, false, false, false, new String[] { "detector_manager_bot", "detector_manager_bot", "detector_unit_yellow", "detector_unit_blue", "detector_unit_green", "detector_unit_red" }),
	STOP(2, true, true, false, new String[] { "detector_manager_bot", "detector_station_top", "detector_station_yellow", "detector_station_blue", "detector_station_green", "detector_station_red" }) {
		@Override
		public void activate(final TileEntityDetector detector, final EntityMinecartModular cart) {
			cart.releaseCart();
		}
	},
	JUNCTION(3, true, false, false, new String[] { "detector_manager_bot", "detector_junction_top", "detector_junction_yellow", "detector_junction_blue", "detector_junction_green",
	"detector_junction_red" }) {
		@Override
		public void activate(final TileEntityDetector detector, final EntityMinecartModular cart) {
			this.update(detector, true);
		}

		@Override
		public void deactivate(final TileEntityDetector detector) {
			this.update(detector, false);
		}

		private void update(final TileEntityDetector detector, final boolean flag) {
			if (detector.getWorld().getBlockState(detector.getPos()).getBlock() == ModBlocks.ADVANCED_DETECTOR.getBlock()) {
				BlockPos posUp = detector.getPos().up();
				IBlockState stateUp = detector.getWorld().getBlockState(posUp);
				((BlockRailAdvDetector) ModBlocks.ADVANCED_DETECTOR.getBlock()).refreshState(detector.getWorld(), posUp, stateUp, flag);
			}
		}
	},
	REDSTONE(4, false, false, false, new String[] { "detector_redstone_bot", "detector_redstone_bot", "detector_redstone_yellow", "detector_redstone_blue", "detector_redstone_green",
	"detector_redstone_red" }) {
		@Override
		public void initOperators(final HashMap<Byte, OperatorObject> operators) {
			super.initOperators(operators);
			new OperatorObject.OperatorObjectRedstone(operators, 11, Localization.GUI.DETECTOR.REDSTONE, 0, 0, 0);
			new OperatorObject.OperatorObjectRedstone(operators, 12, Localization.GUI.DETECTOR.REDSTONE_TOP, 0, 1, 0);
			new OperatorObject.OperatorObjectRedstone(operators, 13, Localization.GUI.DETECTOR.REDSTONE_BOT, 0, -1, 0);
			new OperatorObject.OperatorObjectRedstone(operators, 14, Localization.GUI.DETECTOR.REDSTONE_NORTH, 0, 0, -1);
			new OperatorObject.OperatorObjectRedstone(operators, 15, Localization.GUI.DETECTOR.REDSTONE_WEST, -1, 0, 0);
			new OperatorObject.OperatorObjectRedstone(operators, 16, Localization.GUI.DETECTOR.REDSTONE_SOUTH, 0, 0, 1);
			new OperatorObject.OperatorObjectRedstone(operators, 17, Localization.GUI.DETECTOR.REDSTONE_EAST, 1, 0, 0);
		}
	};

	private int meta;
	private String[] textures;
	//private IIcon[] icons;
	private boolean acceptCart;
	private boolean stopCart;
	private boolean emitRedstone;
	private HashMap<Byte, OperatorObject> operators;

	private String[] names = new String[] {"DetectorManager", "DetectorUnit", "DetectorStation", "DetectorJunction", "DetectorRedstoneUnit"};

	public static PropertyEnum<DetectorType> SATE = PropertyEnum.create("detectortype", DetectorType.class);

	DetectorType(final int meta, final boolean acceptCart, final boolean stopCart, final boolean emitRedstone, final String[] textures) {
		this.meta = meta;
		this.textures = textures;
		this.acceptCart = acceptCart;
		this.stopCart = stopCart;
		this.emitRedstone = emitRedstone;
	}

	public int getMeta() {
		return this.meta;
	}

	@Override
	public String getName() {
		return names[meta].toLowerCase();
	}

	//	public void registerIcons(final IIconRegister register) {
	//		this.icons = new IIcon[this.textures.length];
	//		for (int i = 0; i < this.textures.length; ++i) {
	//			final IIcon[] icons = this.icons;
	//			final int n = i;
	//			final StringBuilder sb = new StringBuilder();
	//			StevesCarts.instance.getClass();
	//			icons[n] = register.registerIcon(sb.append("stevescarts").append(":").append(this.textures[i]).toString());
	//		}
	//	}
	//
	//	public IIcon getIcon(final int side) {
	//		return this.icons[side];
	//	}

	public boolean canInteractWithCart() {
		return this.acceptCart;
	}

	public boolean shouldStopCart() {
		return this.stopCart;
	}

	public boolean shouldEmitRedstone() {
		return this.emitRedstone;
	}

	public void activate(final TileEntityDetector detector, final EntityMinecartModular cart) {
	}

	public void deactivate(final TileEntityDetector detector) {
	}

	public static DetectorType getTypeFromSate(IBlockState state) {
		return state.getValue(SATE);
	}

	public static DetectorType getTypeFromint(int meta) {
		return DetectorType.values()[meta];
	}

	public void initOperators(final HashMap<Byte, OperatorObject> operators) {
		this.operators = operators;
	}

	public HashMap<Byte, OperatorObject> getOperators() {
		return this.operators;
	}
}
