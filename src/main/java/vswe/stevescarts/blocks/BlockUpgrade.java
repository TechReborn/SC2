package vswe.stevescarts.blocks;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.common.property.Properties;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import vswe.stevescarts.StevesCarts;
import vswe.stevescarts.blocks.tileentities.TileEntityUpgrade;
import vswe.stevescarts.helpers.PropertyString;
import vswe.stevescarts.items.ModItems;
import vswe.stevescarts.upgrades.AssemblerUpgrade;

public class BlockUpgrade extends BlockContainerBase {

	public static PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
	public static final IUnlistedProperty<Integer> TYPE = Properties.toUnlisted(PropertyInteger.create("type", 0, AssemblerUpgrade.getUpgradesList().size()));

	public BlockUpgrade() {
		super(Material.ROCK);
		this.setCreativeTab(StevesCarts.tabsSC2Blocks);
		this.setDefaultState(
			this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
	}

	protected BlockStateContainer createBlockState() {

		FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
		return new ExtendedBlockState(this, new IProperty[] { FACING },
			new IUnlistedProperty[] { TYPE });
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return getSideFromEnum(state.getValue(FACING));
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(FACING, getSideFromint(meta));
	}

	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		TileEntityUpgrade upgrade = (TileEntityUpgrade) world.getTileEntity(pos);
		IExtendedBlockState extendedBlockState = (IExtendedBlockState) state;
		return extendedBlockState.withProperty(TYPE, upgrade.getType()).withProperty(FACING, upgrade.getSide());
	}

	public EnumFacing getSideFromint(int i) {
		if (i == 0) {
			return EnumFacing.NORTH;
		} else if (i == 1) {
			return EnumFacing.SOUTH;
		} else if (i == 2) {
			return EnumFacing.EAST;
		} else if (i == 3) {
			return EnumFacing.WEST;
		}
		return EnumFacing.NORTH;
	}

	public int getSideFromEnum(EnumFacing facing) {
		if (facing == EnumFacing.NORTH) {
			return 0;
		} else if (facing == EnumFacing.SOUTH) {
			return 1;
		} else if (facing == EnumFacing.EAST) {
			return 2;
		} else if (facing == EnumFacing.WEST) {
			return 3;
		}
		return 0;
	}

	@Override
	public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune) {
	}

	@Override
	public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof TileEntityUpgrade) {
			TileEntityUpgrade upgrade = (TileEntityUpgrade) tile;
			return side != EnumFacing.UP && upgrade.getType() == 13;
		}
		return false;
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		TileEntity tile = worldIn.getTileEntity(pos);
		if (tile instanceof TileEntityUpgrade) {
			TileEntityUpgrade upgrade = (TileEntityUpgrade) tile;
			upgrade.setType(stack.getItemDamage());
		}
	}

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		super.onBlockAdded(world, pos, state);
		((BlockCartAssembler) ModBlocks.CART_ASSEMBLER.getBlock()).addUpgrade(world, pos);
	}

	@Override
	public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
		if (player.capabilities.isCreativeMode) {
			TileEntity tile = worldIn.getTileEntity(pos);
			if (tile instanceof TileEntityUpgrade) {
				TileEntityUpgrade upgrade = (TileEntityUpgrade) tile;
				upgrade.setType(1);
			}
		}
		super.onBlockHarvested(worldIn, pos, state, player);
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		final TileEntity tile = world.getTileEntity(pos);
		if (tile != null && tile instanceof TileEntityUpgrade) {
			final TileEntityUpgrade upgrade = (TileEntityUpgrade) tile;
			if (upgrade.getUpgrade() != null) {
				upgrade.getUpgrade().removed(upgrade);
			}
			if (upgrade.getType() != 1) {
				spawnAsEntity(world, pos, new ItemStack(ModItems.upgrades, 1, upgrade.getType()));
			}
			if (upgrade.hasInventory()) {
				for (int var8 = 0; var8 < upgrade.getSizeInventory(); ++var8) {
					final ItemStack var9 = upgrade.getStackInSlotOnClosing(var8);
					if (var9 != null) {
						final float var10 = world.rand.nextFloat() * 0.8f + 0.1f;
						final float var11 = world.rand.nextFloat() * 0.8f + 0.1f;
						final float var12 = world.rand.nextFloat() * 0.8f + 0.1f;
						while (var9.stackSize > 0) {
							int var13 = world.rand.nextInt(21) + 10;
							if (var13 > var9.stackSize) {
								var13 = var9.stackSize;
							}
							final ItemStack itemStack = var9;
							itemStack.stackSize -= var13;
							final EntityItem var14 = new EntityItem(world, pos.getX() + var10, pos.getY() + var11, pos.getZ() + var12, new ItemStack(var9.getItem(), var13, var9.getItemDamage()));
							final float var15 = 0.05f;
							var14.motionX = (float) world.rand.nextGaussian() * var15;
							var14.motionY = (float) world.rand.nextGaussian() * var15 + 0.2f;
							var14.motionZ = (float) world.rand.nextGaussian() * var15;
							if (var9.hasTagCompound()) {
								var14.getEntityItem().setTagCompound(var9.getTagCompound().copy());
							}
							world.spawnEntityInWorld(var14);
						}
					}
				}
			}
		}
		super.breakBlock(world, pos, state);
		((BlockCartAssembler) ModBlocks.CART_ASSEMBLER.getBlock()).removeUpgrade(world, pos);
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isNormalCube(IBlockState state) {
		return false;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT_MIPPED;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return getUpgradeBounds(source, pos, state);
	}

	public final AxisAlignedBB getUpgradeBounds(IBlockAccess world, BlockPos pos, IBlockState state) {
		EnumFacing side = state.getValue(FACING).getOpposite();
		final float margin = 0.1875f;
		final float width = 0.125f;
		if (side == EnumFacing.DOWN) {
			return new AxisAlignedBB(margin, 0.0f, margin, 1.0f - margin, width, 1.0f - margin);
		} else if (side == EnumFacing.UP) {
			return new AxisAlignedBB(margin, 1.0f - width, margin, 1.0f - margin, 1.0f, 1.0f - margin);
		} else if (side == EnumFacing.WEST) {
			return new AxisAlignedBB(0.0f, margin, margin, width, 1.0f - margin, 1.0f - margin);
		} else if (side == EnumFacing.EAST) {
			return new AxisAlignedBB(1.0f - width, margin, margin, 1.0f, 1.0f - margin, 1.0f - margin);
		} else if (side == EnumFacing.NORTH) {
			return new AxisAlignedBB(margin, margin, 0.0f, 1.0f - margin, 1.0f - margin, width);
		} else if (side == EnumFacing.SOUTH) {
			return new AxisAlignedBB(margin, margin, 1.0f - width, 1.0f - margin, 1.0f - margin, 1.0f);
		}
		return FULL_BLOCK_AABB;
	}

	public AxisAlignedBB getIdleBlockBounds() {
		final float margin = 0.1875f;
		final float width = 0.125f;
		return new AxisAlignedBB(margin, width, margin, 1.0f - margin, 1.0f - width, 1.0f - margin);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (player.isSneaking()) {
			return false;
		}
		final TileEntity tile = world.getTileEntity(pos);
		if (tile != null && tile instanceof TileEntityUpgrade) {
			final TileEntityUpgrade upgrade = (TileEntityUpgrade) tile;
			if (upgrade.getMaster() == null) {
				return false;
			}
			if (world.isRemote) {
				return true;
			}
			if (upgrade.getUpgrade().useStandardInterface()) {
				BlockPos masterPos = upgrade.getMaster().getPos();
				FMLNetworkHandler.openGui(player, StevesCarts.instance, 3, world, masterPos.getX(), masterPos.getY(), masterPos.getZ());
				return true;
			}
			FMLNetworkHandler.openGui(player, StevesCarts.instance, 7, world, pos.getX(), pos.getY(), pos.getZ());
		}
		return true;
	}

	@Override
	public TileEntity createNewTileEntity(final World world, final int var2) {
		return new TileEntityUpgrade();
	}

}
