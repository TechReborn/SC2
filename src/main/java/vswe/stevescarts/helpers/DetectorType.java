package vswe.stevescarts.helpers;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import vswe.stevescarts.blocks.BlockRailAdvDetector;
import vswe.stevescarts.blocks.ModBlocks;
import vswe.stevescarts.blocks.tileentities.TileEntityDetector;
import vswe.stevescarts.entitys.EntityMinecartModular;

import java.util.HashMap;
import java.util.Locale;

import static vswe.stevescarts.blocks.BlockDetector.SATE;

public enum DetectorType implements IStringSerializable {
	NORMAL(0, true, false, true),
	UNIT(1, false, false, false),
	STATION(2, true, true, false) {
		@Override
		public void activate(TileEntityDetector detector, EntityMinecartModular cart) {
			cart.releaseCart();
		}
	},
	JUNCTION(3, true, false, false) {
		@Override
		public void activate(TileEntityDetector detector, EntityMinecartModular cart) {
			update(detector, true);
		}

		@Override
		public void deactivate(TileEntityDetector detector) {
			update(detector, false);
		}

		private void update(TileEntityDetector detector, boolean flag) {
			BlockPos posUp = detector.getPos().up();
			IBlockState stateUp = detector.getWorld().getBlockState(posUp);
			if (ModBlocks.ADVANCED_DETECTOR.isAt(detector.getWorld(), posUp, stateUp)) {
				((BlockRailAdvDetector) ModBlocks.ADVANCED_DETECTOR.getBlock()).refreshState(detector.getWorld(), posUp, stateUp, flag);
			}
		}
	},
	REDSTONE(4, false, false, false) {
		@Override
		public void initOperators(HashMap<Byte, OperatorObject> operators) {
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
	private boolean acceptCart;
	private boolean stopCart;
	private boolean emitRedstone;
	private HashMap<Byte, OperatorObject> operators;

	DetectorType(int meta, boolean acceptCart, boolean stopCart, boolean emitRedstone) {
		this.meta = meta;
		this.acceptCart = acceptCart;
		this.stopCart = stopCart;
		this.emitRedstone = emitRedstone;
	}

	public int getMeta() {
		return meta;
	}

	public String getTranslatedName() {
		StringBuilder builder = new StringBuilder();
		return I18n.format(builder.append("item.SC2:BlockDetector").append(meta).append(".name").toString());
	}

	@Override
	public String getName() {
		return name().toLowerCase(Locale.ENGLISH);
	}

	public boolean canInteractWithCart() {
		return acceptCart;
	}

	public boolean shouldStopCart() {
		return stopCart;
	}

	public boolean shouldEmitRedstone() {
		return emitRedstone;
	}

	public void activate(TileEntityDetector detector, EntityMinecartModular cart) {}

	public void deactivate(TileEntityDetector detector) {}

	public static DetectorType getTypeFromSate(IBlockState state) {
		return state.getValue(SATE);
	}

	public static DetectorType getTypeFromint(int meta) {
		return DetectorType.values()[meta];
	}

	public void initOperators(HashMap<Byte, OperatorObject> operators) {
		this.operators = operators;
	}

	public HashMap<Byte, OperatorObject> getOperators() {
		return operators;
	}
}
