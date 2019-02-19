package vswe.stevescarts.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;
import vswe.stevescarts.Constants;
import vswe.stevescarts.StevesCarts;
import vswe.stevescarts.blocks.tileentities.*;
import vswe.stevescarts.compat.railcraft.RailcraftHook;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public enum ModBlocks {
	CART_ASSEMBLER("BlockCartAssembler", BlockCartAssembler.class, TileEntityCartAssembler.class, "assembler"),
	CARGO_MANAGER("BlockCargoManager", BlockCargoManager.class, TileEntityCargo.class, "cargo"),
	LIQUID_MANAGER("BlockLiquidManager", BlockLiquidManager.class, TileEntityLiquid.class, "liquid"),
	EXTERNAL_DISTRIBUTOR("BlockDistributor", BlockDistributor.class, TileEntityDistributor.class, "distributor"),
	MODULE_TOGGLER("BlockActivator", BlockActivator.class, TileEntityActivator.class, "activator"),
	DETECTOR_UNIT("BlockDetector", BlockDetector.class, TileEntityDetector.class, "detector"),
	UPGRADE("upgrade", BlockUpgrade.class, TileEntityUpgrade.class, "upgrade"),
	JUNCTION("BlockJunction", BlockRailJunction.class) {
		@Override
		public boolean isAt(World world, BlockPos pos, IBlockState state) {
			if (super.isAt(world, pos, state))
				return true;
			return RailcraftHook.getInstance().isJunction(world, pos, state);
		}
	},
	ADVANCED_DETECTOR("BlockAdvDetector", BlockRailAdvDetector.class) {
		@Override
		public boolean isAt(World world, BlockPos pos, IBlockState state) {
			if (super.isAt(world, pos, state))
				return true;
			return RailcraftHook.getInstance().isAdvDetector(world, pos, state);
		}
	},
	STORAGE("BlockMetalStorage", BlockMetalStorage.class);

	public static final List<ItemBlock> ITEM_BLOCKS = new ArrayList<>();

	private final String name;
	private final Class<? extends Block> clazz;
	private final Class<? extends TileEntity> tileEntityClazz;
	private final String tileEntityName;
	private Block block;

	ModBlocks(final String name, final Class<? extends Block> clazz) {
		this(name, clazz, null, null);
	}

	ModBlocks(final String name, final Class<? extends Block> clazz, final Class<? extends TileEntity> tileEntityClazz, final String tileEntityName) {
		this.name = name;
		this.clazz = clazz;
		this.tileEntityClazz = tileEntityClazz;
		this.tileEntityName = tileEntityName;
	}

	public static void preInit() {
		for (final ModBlocks info : values()) {
			try {
				if (Block.class.isAssignableFrom(info.clazz)) {
					final Block block = info.clazz.getConstructor().newInstance();
					block.setHardness(2.0f);
					block.setRegistryName(Constants.MOD_ID, info.name.toLowerCase()).setUnlocalizedName("SC2:" + info.name);
					info.block = block;

					ItemBlock item;
					if (block instanceof ICustomItemBlock)
						item = ((ICustomItemBlock) block).getItemBlock();
					else
						item = new ItemBlock(block);

					ITEM_BLOCKS.add(item);
					item.setRegistryName(Constants.MOD_ID + ":" + info.name).setUnlocalizedName(Constants.MOD_ID + ":" + info.name);

					if (info.tileEntityClazz != null) {
						GameRegistry.registerTileEntity(info.tileEntityClazz, info.tileEntityName);
					}
				} else {
					StevesCarts.logger.error("This is not a block (" + info.name + ")");
				}
			} catch (Exception e) {
				StevesCarts.logger.error("Failed to create block (" + info.name + ")");
				StevesCarts.logger.throwing(e);
			}
		}
	}

	@SubscribeEvent
	public static void registerBlocks(final RegistryEvent.Register<Block> event) {
		final IForgeRegistry<Block> registry = event.getRegistry();
		for (final ModBlocks info: values())
			registry.register(info.block);
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public static void registerBlockRenderers(ModelRegistryEvent event) {
		for (final ModBlocks info: values()) {
			Block block = info.block;
			if (block instanceof IStateMappedBlock) {
				StateMap.Builder builder = new StateMap.Builder();
				((IStateMappedBlock) block).setStateMapper(builder);
				ModelLoader.setCustomStateMapper(block, builder.build());
			}
			if (block instanceof ICustomItemBlock) {
				ICustomItemBlock customItemBlock = (ICustomItemBlock) block;
				ItemStack renderedItem = customItemBlock.getRenderedItem();
				if (!renderedItem.isEmpty()) {
					Map<Integer, ResourceLocation> map = StevesCarts.proxy.getItemModelMap(renderedItem.getItem());
					ModelResourceLocation model = (ModelResourceLocation) map.get(renderedItem.getMetadata());
					ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), 0, model);
					continue;
				}
			}
			ResourceLocation name = block.getRegistryName();
			if (block instanceof ISubtypeItemBlockModelDefinition) {
				ISubtypeItemBlockModelDefinition subtypeBlock = (ISubtypeItemBlockModelDefinition) block;
				for (int i = 0; i < subtypeBlock.getSubtypeNumber(); i++) {
					int meta = subtypeBlock.getSubtypeMeta(i);
					ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), meta, new ModelResourceLocation(name.getResourceDomain() + ":" + String.format(subtypeBlock.getSubtypeName(meta), name.getResourcePath()), "inventory"));
				}
			} else {
				ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), 0, new ModelResourceLocation(name, "inventory"));
			}
		}
	}

	public Block getBlock() {
		return block;
	}

	public boolean isAt(World world, BlockPos pos, IBlockState state) {
		return state.getBlock() == block;
	}

	public interface ISubtypeItemBlockModelDefinition {
		/**
		 * Returns the amount of subtypes
		 *
		 * @return
		 */
		int getSubtypeNumber();

		/**
		 * Returns the name of this subtype.
		 * String is formatted, use %s for the normal registry name.
		 *
		 * @param meta
		 * @return
		 */
		String getSubtypeName(int meta);

		/**
		 * Returns the metadata for the specified subtype
		 *
		 * @param subtype
		 * @return
		 */
		default int getSubtypeMeta(int subtype) {
			return subtype;
		}
	}

	public interface IStateMappedBlock {
		@SideOnly(Side.CLIENT)
		void setStateMapper(StateMap.Builder builder);
	}

	public interface ICustomItemBlock {
		ItemBlock getItemBlock();

		/**
		 * Returns which item this block should be rendered as
		 * @return
		 */
		@SideOnly(Side.CLIENT)
		default ItemStack getRenderedItem() {
			return ItemStack.EMPTY;
		}
	}
}
