package vswe.stevescarts.compat.railcraft;

import mods.railcraft.api.core.IRailcraftModule;
import mods.railcraft.api.core.RailcraftModule;
import mods.railcraft.api.tracks.TrackRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import vswe.stevescarts.Constants;
import vswe.stevescarts.SCConfig;
import vswe.stevescarts.blocks.BlockRailAdvDetector;
import vswe.stevescarts.blocks.BlockRailJunction;
import vswe.stevescarts.blocks.ModBlocks;
import vswe.stevescarts.compat.railcraft.kit.KitAdvDetector;
import vswe.stevescarts.compat.railcraft.kit.KitJunction;

@RailcraftModule(value = Constants.MOD_ID + ":railcraft_compat", description = "Steves Carts Reborn Railcraft compatibility")
public class CompatRailcraft implements IRailcraftModule {

    public static final String MOD_ID = "railcraft";
    private static final ModuleEventHandler DISABLED = new ModuleEventHandler() {
    };

    @Override
    public void checkPrerequisites() {
    }

    @Override
    public ModuleEventHandler getModuleEventHandler(boolean enabled) {
        return !SCConfig.railcraftCompat ? DISABLED : new ModuleEventHandler() {
            @Override
            public void construction() {
                RailcraftHook.init();
            }

            @Override
            public void preInit() {
                TrackRegistry.TRACK_KIT.register(KitAdvDetector.KIT);
                TrackRegistry.TRACK_KIT.register(KitJunction.KIT);
            }

            @Override
            public void init() {
                KitAdvDetector.setAdvDetector((BlockRailAdvDetector) ModBlocks.ADVANCED_DETECTOR.getBlock());
                Item partsItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(MOD_ID, "track_parts"));
                Ingredient parts = CraftingHelper.getIngredient(partsItem == null ? "ingotIron" : new ItemStack(partsItem));
                Ingredient plank = CraftingHelper.getIngredient("plankWood");
                Ingredient redstone = CraftingHelper.getIngredient("dustRedstone");
                Ingredient stonePlate = CraftingHelper.getIngredient(Blocks.STONE_PRESSURE_PLATE);

                ItemStack advDetectorKit = KitAdvDetector.KIT.getTrackKitItem();
                GameRegistry.addShapelessRecipe(new ResourceLocation(Constants.MOD_ID, "advanced_detector_track_kit"), null, advDetectorKit, parts,
                        plank, redstone, stonePlate, stonePlate);

                ItemStack junctionKit = KitJunction.KIT.getTrackKitItem();
                GameRegistry.addShapelessRecipe(new ResourceLocation(Constants.MOD_ID, "junction_track_kit"), null, junctionKit, parts, plank,
                        redstone, redstone, redstone);
            }

            @Override
            public void postInit() {

            }
        };
    }
}
