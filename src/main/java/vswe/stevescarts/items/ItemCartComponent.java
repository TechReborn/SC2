package vswe.stevescarts.items;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import vswe.stevescarts.Constants;
import vswe.stevescarts.StevesCarts;
import vswe.stevescarts.entitys.EntityEasterEgg;
import vswe.stevescarts.helpers.ComponentTypes;
import vswe.stevescarts.renders.model.ItemModelManager;
import vswe.stevescarts.renders.model.TexturedItem;


public class ItemCartComponent extends Item  implements TexturedItem {
	//	private IIcon[] icons;
	//	private IIcon unknownIcon;

	public static int size() {
		return ComponentTypes.values().length;
	}

	public ItemCartComponent() {
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
		this.setCreativeTab(StevesCarts.tabsSC2Components);
		ItemModelManager.registerItem(this);
	}

	private String getName(final int dmg) {
		return ComponentTypes.values()[dmg].getName();
	}

	public String getName(final ItemStack par1ItemStack) {
		if (par1ItemStack == null || par1ItemStack.getItemDamage() < 0 || par1ItemStack.getItemDamage() >= size() || this.getName(par1ItemStack.getItemDamage()) == null) {
			return "Unknown SC2 Component";
		}
		return this.getName(par1ItemStack.getItemDamage());
	}

	//	@SideOnly(Side.CLIENT)
	//	public IIcon getIconFromDamage(final int dmg) {
	//		if (dmg < 0 || dmg >= this.icons.length || this.icons[dmg] == null) {
	//			return this.unknownIcon;
	//		}
	//		return this.icons[dmg];
	//	}
	//
	private String getRawName(final int i) {
		if(getName(i) == null){
			return null;
		}
		return this.getName(i).replace(":", "").replace(" ", "_").toLowerCase();
	}
	//
	//	@SideOnly(Side.CLIENT)
	//	public void registerIcons(final IIconRegister register) {
	//		this.icons = new IIcon[size()];
	//		for (int i = 0; i < this.icons.length; ++i) {
	//			if (this.getName(i) != null) {
	//				final IIcon[] icons = this.icons;
	//				final int n = i;
	//				final StringBuilder sb = new StringBuilder();
	//				StevesCarts.instance.getClass();
	//				icons[n] = register.registerIcon(sb.append("stevescarts").append(":").append(this.getRawName(i)).append("_icon").toString());
	//			}
	//		}
	//		final StringBuilder sb2 = new StringBuilder();
	//		StevesCarts.instance.getClass();
	//		this.unknownIcon = register.registerIcon(sb2.append("stevescarts").append(":").append("unknown_icon").toString());
	//	}

	@Override
	public String getUnlocalizedName(final ItemStack item) {
		if (item == null || item.getItemDamage() < 0 || item.getItemDamage() >= size() || this.getName(item.getItemDamage()) == null) {
			return this.getUnlocalizedName();
		}
		return "item.SC2:" + this.getRawName(item.getItemDamage());
	}

	@Override
	public String getUnlocalizedName() {
		return "item.SC2:unknowncomponent";
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(final ItemStack par1ItemStack, final EntityPlayer par2EntityPlayer, final List par3List, final boolean par4) {
		if (par1ItemStack == null || par1ItemStack.getItemDamage() < 0 || par1ItemStack.getItemDamage() >= size() || this.getName(par1ItemStack.getItemDamage()) == null) {
			if (par1ItemStack != null && par1ItemStack.getItem() instanceof ItemCartComponent) {
				par3List.add("Component id " + par1ItemStack.getItemDamage());
			} else {
				par3List.add("Unknown component id");
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(final Item par1, final CreativeTabs par2CreativeTabs, final List par3List) {
		for (int i = 0; i < size(); ++i) {
			final ItemStack iStack = new ItemStack(par1, 1, i);
			if (this.isValid(iStack)) {
				par3List.add(iStack);
			}
		}
	}

	public boolean isValid(final ItemStack item) {
		if (item == null || !(item.getItem() instanceof ItemCartComponent) || this.getName(item.getItemDamage()) == null) {
			return false;
		}
		if (item.getItemDamage() >= 50 && item.getItemDamage() < 58) {
			return Constants.isChristmas;
		}
		if (item.getItemDamage() >= 66 && item.getItemDamage() < 72) {
			return Constants.isEaster;
		}
		return item.getItemDamage() < 72 || item.getItemDamage() >= 80;
	}

	public static ItemStack getWood(final int type, final boolean isLog) {
		return getWood(type, isLog, 1);
	}

	public static ItemStack getWood(final int type, final boolean isLog, final int count) {
		return new ItemStack(ModItems.component, count, 72 + type * 2 + (isLog ? 0 : 1));
	}

	public static boolean isWoodLog(final ItemStack item) {
		return item != null && item.getItemDamage() >= 72 && item.getItemDamage() < 80 && (item.getItemDamage() - 72) % 2 == 0;
	}

	public static boolean isWoodTwig(final ItemStack item) {
		return item != null && item.getItemDamage() >= 72 && item.getItemDamage() < 80 && (item.getItemDamage() - 72) % 2 == 1;
	}

	private boolean isEdibleEgg(final ItemStack item) {
		return item != null && item.getItemDamage() >= 66 && item.getItemDamage() < 70;
	}

	private boolean isThrowableEgg(final ItemStack item) {
		return item != null && item.getItemDamage() == 70;
	}

	@Override
	public ItemStack onItemUseFinish(ItemStack item, World world, EntityLivingBase entity) {
		if (entity instanceof EntityPlayer && this.isEdibleEgg(item)) {
			EntityPlayer player = (EntityPlayer) entity;
			if (item.getItemDamage() == 66) {
				world.createExplosion(null, entity.posX, entity.posY, entity.posZ, 0.1f, false);
			} else if (item.getItemDamage() == 67) {
				entity.setFire(5);
				if (!world.isRemote) {
					entity.addPotionEffect(new PotionEffect(MobEffects.WATER_BREATHING, 600, 0));
				}
			} else if (item.getItemDamage() == 68) {
				if (!world.isRemote) {
					entity.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 50, 2));
				}
			} else if (item.getItemDamage() == 69) {
				if (!world.isRemote) {
					entity.addPotionEffect(new PotionEffect(MobEffects.SPEED, 300, 4));
				}
			} else if (item.getItemDamage() == 70) {}
			if (!player.capabilities.isCreativeMode) {
				--item.stackSize;
			}
			world.playSound((EntityPlayer)entity, entity.posX, entity.posY, entity.posZ, SoundEvents.ENTITY_PLAYER_BURP, SoundCategory.PLAYERS, 0.5F, world.rand.nextFloat() * 0.1F + 0.9F);
			player.getFoodStats().addStats(2, 0.0f);
			return item;
		}
		return  super.onItemUseFinish(item, world, entity);
	}

	@Override
	public int getMaxItemUseDuration(final ItemStack item) {
		return this.isEdibleEgg(item) ? 32 : super.getMaxItemUseDuration(item);
	}

	@Override
	public EnumAction getItemUseAction(final ItemStack item) {
		return this.isEdibleEgg(item) ? EnumAction.EAT : super.getItemUseAction(item);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack item, World world, EntityPlayer player, EnumHand hand) {
		if (this.isEdibleEgg(item)) {
			player.setActiveHand(hand);
			return ActionResult.newResult(EnumActionResult.SUCCESS, item);
		}
		if (this.isThrowableEgg(item)) {
			if (!player.capabilities.isCreativeMode) {
				--item.stackSize;
			}
			world.playSound((EntityPlayer)null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.NEUTRAL, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
			if (!world.isRemote) {
				world.spawnEntityInWorld(new EntityEasterEgg(world, player));
			}
			return ActionResult.newResult(EnumActionResult.SUCCESS, item);
		}
		return super.onItemRightClick(item, world, player, hand);
	}

	@Override
	public String getTextureName(int damage) {
		if(getRawName(damage) == null){
			return "stevescarts:items/unknown_icon";
		}
		return "stevescarts:items/" + getRawName(damage) + "_icon";
	}

	@Override
	public int getMaxMeta() {
		return size();
	}
}
