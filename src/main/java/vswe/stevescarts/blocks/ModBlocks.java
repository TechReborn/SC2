package vswe.stevescarts.blocks;

import java.lang.reflect.Constructor;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.registry.GameRegistry;
import vswe.stevescarts.blocks.tileentities.TileEntityActivator;
import vswe.stevescarts.blocks.tileentities.TileEntityCargo;
import vswe.stevescarts.blocks.tileentities.TileEntityCartAssembler;
import vswe.stevescarts.blocks.tileentities.TileEntityDetector;
import vswe.stevescarts.blocks.tileentities.TileEntityDistributor;
import vswe.stevescarts.blocks.tileentities.TileEntityLiquid;
import vswe.stevescarts.blocks.tileentities.TileEntityUpgrade;
import vswe.stevescarts.helpers.ComponentTypes;
import vswe.stevescarts.helpers.RecipeHelper;
import vswe.stevescarts.items.ItemBlockDetector;
import vswe.stevescarts.items.ItemBlockStorage;
import vswe.stevescarts.items.ItemUpgrade;
import vswe.stevescarts.items.ModItems;

public enum ModBlocks {
	CARGO_MANAGER("BlockCargoManager", BlockCargoManager.class, TileEntityCargo.class, "cargo"),
	JUNCTION("BlockJunction", BlockRailJunction.class),
	ADVANCED_DETECTOR("BlockAdvDetector", BlockRailAdvDetector.class),
	CART_ASSEMBLER("BlockCartAssembler", BlockCartAssembler.class, TileEntityCartAssembler.class, "assembler"),
	MODULE_TOGGLER("BlockActivator", BlockActivator.class, TileEntityActivator.class, "activator"),
	EXTERNAL_DISTRIBUTOR("BlockDistributor", BlockDistributor.class, TileEntityDistributor.class, "distributor"),
	DETECTOR_UNIT("BlockDetector", BlockDetector.class, TileEntityDetector.class, "detector", ItemBlockDetector.class),
	UPGRADE("upgrade", BlockUpgrade.class, TileEntityUpgrade.class, "upgrade", ItemUpgrade.class),
	LIQUID_MANAGER("BlockLiquidManager", BlockLiquidManager.class, TileEntityLiquid.class, "liquid"),
	STORAGE("BlockMetalStorage", BlockMetalStorage.class, ItemBlockStorage.class);

	private final String name;
	private final Class<? extends Block> clazz;
	private final Class<? extends TileEntity> tileEntityClazz;
	private final String tileEntityName;
	private final Class<? extends ItemBlock> itemClazz;
	private Block block;

	ModBlocks(final String name, final Class<? extends Block> clazz) {
		this(name, clazz, null, null);
	}

	ModBlocks(final String name, final Class<? extends Block> clazz, final Class<? extends TileEntity> tileEntityClazz, final String tileEntityName) {
		this(name, clazz, tileEntityClazz, tileEntityName, ItemBlock.class);
	}

	ModBlocks(final String name, final Class<? extends Block> clazz, final Class<? extends ItemBlock> itemClazz) {
		this(name, clazz, null, null, itemClazz);
	}

	ModBlocks(final String name,
			final Class<? extends Block> clazz,
			final Class<? extends TileEntity> tileEntityClazz,
			final String tileEntityName,
			final Class<? extends ItemBlock> itemClazz) {
		this.name = name;
		this.clazz = clazz;
		this.tileEntityClazz = tileEntityClazz;
		this.tileEntityName = tileEntityName;
		this.itemClazz = itemClazz;
	}

	public static void init() {
		for (final ModBlocks blockInfo : values()) {
			try {
				if (Block.class.isAssignableFrom(blockInfo.clazz)) {
					final Constructor<? extends Block> blockConstructor = blockInfo.clazz.getConstructor(new Class[0]);
					final Object blockInstance = blockConstructor.newInstance();
					final Block blockBase = (Block) blockInstance;
					final Block block = (Block) blockInstance;
					block.setHardness(2.0f);
					GameRegistry.registerBlock(block, blockInfo.itemClazz, blockInfo.name);
					blockBase.setUnlocalizedName("SC2:" + blockInfo.name);
					blockInfo.block = block;
					if (blockInfo.tileEntityClazz != null) {
						GameRegistry.registerTileEntity(blockInfo.tileEntityClazz, blockInfo.tileEntityName);
					}
					System.out.println("Successfully registered block (" + blockInfo.name + ")");
				} else {
					System.out.println("This is not a block (" + blockInfo.name + ")");
				}
			} catch (Exception e) {
				System.out.println("Failed to create block (" + blockInfo.name + ")");
				e.printStackTrace();
			}
		}
		ModBlocks.STORAGE.block.setHardness(5.0f).setResistance(10.0f);
	}

	public static void addRecipes() {
		final String blue = "dyeBlue";
		final String orange = "dyeOrange";
		RecipeHelper.addRecipe(new ItemStack(ModBlocks.CARGO_MANAGER.block, 1), new Object[][] {
			{ ComponentTypes.LARGE_IRON_PANE.getItemStack(), ComponentTypes.HUGE_IRON_PANE.getItemStack(), ComponentTypes.LARGE_IRON_PANE.getItemStack() },
			{ ComponentTypes.HUGE_IRON_PANE.getItemStack(), ComponentTypes.LARGE_DYNAMIC_PANE.getItemStack(), ComponentTypes.HUGE_IRON_PANE.getItemStack() },
			{ ComponentTypes.LARGE_IRON_PANE.getItemStack(), ComponentTypes.HUGE_IRON_PANE.getItemStack(), ComponentTypes.LARGE_IRON_PANE.getItemStack() } });
		RecipeHelper.addRecipe(new ItemStack(ModBlocks.MODULE_TOGGLER.block, 1), new Object[][] { { orange, Items.GOLD_INGOT, blue }, { Blocks.STONE, Items.IRON_INGOT, Blocks.STONE },
			{ Items.REDSTONE, ComponentTypes.ADVANCED_PCB.getItemStack(), Items.REDSTONE } });
		RecipeHelper.addRecipe(new ItemStack(ModBlocks.EXTERNAL_DISTRIBUTOR.block, 1), new Object[][] { { Blocks.STONE, ComponentTypes.SIMPLE_PCB.getItemStack(), Blocks.STONE },
			{ ComponentTypes.SIMPLE_PCB.getItemStack(), Items.REDSTONE, ComponentTypes.SIMPLE_PCB.getItemStack() }, { Blocks.STONE, ComponentTypes.SIMPLE_PCB.getItemStack(), Blocks.STONE } });
		RecipeHelper.addRecipe(new ItemStack(ModBlocks.CART_ASSEMBLER.block, 1), new Object[][] { { Items.IRON_INGOT, Blocks.STONE, Items.IRON_INGOT },
			{ Blocks.STONE, Items.IRON_INGOT, Blocks.STONE }, { ComponentTypes.SIMPLE_PCB.getItemStack(), Blocks.STONE, ComponentTypes.SIMPLE_PCB.getItemStack() } });
		RecipeHelper.addRecipe(new ItemStack(ModBlocks.JUNCTION.block, 1), new Object[][] { { null, Items.REDSTONE, null }, { Items.REDSTONE, Blocks.RAIL, Items.REDSTONE },
			{ null, Items.REDSTONE, null } });
		RecipeHelper.addRecipe(new ItemStack(ModBlocks.ADVANCED_DETECTOR.block, 2), new Object[][] { { Items.IRON_INGOT, Blocks.STONE_PRESSURE_PLATE, Items.IRON_INGOT },
			{ Items.IRON_INGOT, Items.REDSTONE, Items.IRON_INGOT }, { Items.IRON_INGOT, Blocks.STONE_PRESSURE_PLATE, Items.IRON_INGOT } });
		final ItemStack unit = new ItemStack(ModBlocks.DETECTOR_UNIT.block, 1, 1);
		RecipeHelper.addRecipe(unit, new Object[][] { { Blocks.COBBLESTONE, Blocks.STONE_PRESSURE_PLATE, Blocks.COBBLESTONE },
			{ Items.IRON_INGOT, ComponentTypes.SIMPLE_PCB.getItemStack(), Items.IRON_INGOT }, { Blocks.COBBLESTONE, Items.REDSTONE, Blocks.COBBLESTONE } });
		RecipeHelper.addRecipe(new ItemStack(ModBlocks.DETECTOR_UNIT.block, 1, 0), new Object[][] { { ComponentTypes.SIMPLE_PCB.getItemStack() }, { unit } });
		RecipeHelper.addRecipe(new ItemStack(ModBlocks.DETECTOR_UNIT.block, 1, 2), new Object[][] { { Items.IRON_INGOT, Items.IRON_INGOT, Items.IRON_INGOT }, { null, unit, null },
			{ null, ComponentTypes.SIMPLE_PCB.getItemStack(), null } });
		RecipeHelper.addRecipe(new ItemStack(ModBlocks.DETECTOR_UNIT.block, 1, 3), new Object[][] { { Blocks.REDSTONE_TORCH, null, Blocks.REDSTONE_TORCH }, { Items.REDSTONE, unit, Items.REDSTONE },
			{ null, ComponentTypes.SIMPLE_PCB.getItemStack(), null } });
		RecipeHelper.addRecipe(new ItemStack(ModBlocks.DETECTOR_UNIT.block, 1, 4), new Object[][] { { Items.REDSTONE, Items.REDSTONE, Items.REDSTONE }, { Items.REDSTONE, unit, Items.REDSTONE },
			{ Items.REDSTONE, Items.REDSTONE, Items.REDSTONE } });
		final ItemStack advtank = new ItemStack(ModItems.modules, 1, 66);
		RecipeHelper.addRecipe(new ItemStack(ModBlocks.LIQUID_MANAGER.block, 1), new Object[][] { { advtank, Items.IRON_INGOT, advtank },
			{ Items.IRON_INGOT, ComponentTypes.TANK_VALVE, Items.IRON_INGOT }, { advtank, Items.IRON_INGOT, advtank } });
	}

	public Block getBlock() {
		return this.block;
	}
}
