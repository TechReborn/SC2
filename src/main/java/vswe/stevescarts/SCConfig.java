package vswe.stevescarts;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;
import vswe.stevescarts.modules.data.ModuleData;

import java.util.Collection;
import java.util.HashMap;

/**
 * Created by modmuss50 on 31/01/2017.
 * Modified by GoryMoon 09/01/2018
 */
@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public class SCConfig {

	public static final SCConfig INSTANCE = new SCConfig();

	public static boolean disableTimedCrafting = false;
	public static int maxDynamites = 50;
	public static boolean useArcadeSounds = true;
	public static boolean useArcadeMobSounds = true;
	public static int drillSize = 4;
	public static boolean enableEaster = true;
	public static boolean enableHalloween = true;
	public static boolean enableChristmas = true;
	public static boolean railcraftCompat = true;

	private static Multimap<String,String> ironTierRepair;
	private static Multimap<String,String> diamondTierRepair;
	private static Multimap<String,String> hardenedTierRepair;
	public static String ironRepairName;
	public static String diamondRepairName;
	public static String hardenedRepairName;

	public static HashMap<Byte, Boolean> validModules = new HashMap<>();

	public Configuration config;

	public void load(FMLPreInitializationEvent event) {
		config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
		sync();
	}

	public void save() {
		if (config.hasChanged())
			config.save();
	}

	public void sync() {
		for (final ModuleData module : ModuleData.getList().values()) {
			if (!module.getIsLocked()) {
				String name = module.getName().replace(" ", "").replace(":", "_");
				validModules.put(module.getID(), config.get("EnabledModules", name, module.getEnabledByDefault()).setRequiresMcRestart(true).getBoolean(true));
			}
		}

		disableTimedCrafting = config.getBoolean("DisableCartAssemberTime", "Settings", false, "Set to true to disable the timer in the cart assember, it will still require fuel");
		maxDynamites =  config.getInt("MaximumNumberOfDynamites", "Settings", 50, 0, maxDynamites, "The max amount of dynamite you can put into a dynamite carrier");
		useArcadeSounds = config.getBoolean("useArcadeSounds", "Settings", true, "If the sounds in the arcade should be enabled");
		useArcadeMobSounds = config.getBoolean("useTetrisMobSounds", "Settings", true, "If mob sounds should be used in the tetris arcade game");
		drillSize = config.getInt("maxDrillWidth", "Settings", 4, 1, 4, "The max width beside the cart that the drills can mine, the drill diameter will be (width * 2 + 1)\nMin: 1\nMax: 4");

        String[] ironRepair = config.getStringList("Iron Tier Repair Items", "Repair", new String[] {"minecraft:iron_ingot"}, "A list of items that can repair an iron tier tool");
        String[] diamondRepair = config.getStringList("Diamond Tier Repair Items", "Repair", new String[] {"ore:gemDiamond"}, "A list of items that can repair an diamond tier tool");
        String[] hardenedRepair = config.getStringList("Hardened Tier Repair Items", "Repair", new String[] {"stevescarts:modulecomponents:22"}, "A list of items that can repair an hardened tier tool");
		parseRepairMaterial(ironRepair, ironTierRepair = HashMultimap.create(), "iron");
		parseRepairMaterial(diamondRepair, diamondTierRepair = HashMultimap.create(), "diamond");
		parseRepairMaterial(hardenedRepair, hardenedTierRepair = HashMultimap.create(), "hardened");
		ironRepairName = config.getString("Iron Tier Name", "Repair", "", "Set name of what is needed to repair iron tier tool, blank uses default text");
		diamondRepairName = config.getString("Diamond Tier Name", "Repair", "", "Set name of what is needed to repair diamond tier tool, blank uses default text");
		hardenedRepairName = config.getString("Hardened Tier Name", "Repair", "", "Set name of what is needed to repair hardened tier tool, blank uses default text");

		config.setCategoryComment("Events", "Whether to let the events happen during their time frames of not");
		enableEaster = config.getBoolean("Enable Easter", "Events", true, "If the easter event can occur");
		enableHalloween = config.getBoolean("Enable Halloween", "Events", true, "If the halloween event can occur");
		enableChristmas = config.getBoolean("Enable Christmas", "Events", true, "If the christmas event can occur");

		railcraftCompat = config.getBoolean("Enable Railcraft Compatibility", "Compatibility", true, "If Railcraft compatibility content would be "
				+ "added when Railcraft is present");
        save();
	}

	private static void parseRepairMaterial(String[] repairItem, Multimap<String, String> map, String type) {
		for(String whitelisted : repairItem) {
			try {
				String[] data = whitelisted.split(":");
				String item = data[0] + ":" + data[1];
				String meta = null;
				if(data.length <= 2) {
					meta = "0";
				} else {
					try {
						meta = String.valueOf(Integer.parseInt(data[2]));
					} catch(NumberFormatException ex) {}
					if("*".equals(data[2])) {
						meta = data[2];
					}
				}
				if(meta == null) {
					StevesCarts.logger.error("Failed to parse " + type  + " repair item: " + whitelisted + ". Invalid metadata: " + data[2]);
				} else {
					map.put(item, meta);
				}
			} catch (Exception e) {
				StevesCarts.logger.error("Failed to parse " + type  + " repair item: " + whitelisted);
			}
		}
	}

	public static boolean isValidRepairItem(ItemStack stack, String type) {
		Multimap<String, String> map = "iron".equals(type) ? ironTierRepair: "diamond".equals(type) ? diamondTierRepair: hardenedTierRepair;
		if (stack.isEmpty())
			return false;

		String stackMeta = String.valueOf(stack.getMetadata());
		ResourceLocation name = stack.getItem().getRegistryName();
		Collection<String> metas = map.get(name.toString());
		for(String meta : metas) {
			if("*".equals(meta)) {
				return true;
			} else if(meta.equals(stackMeta)) {
				return true;
			}
		}
		for (int id: OreDictionary.getOreIDs(stack)) {
			String ore = "ore:" + OreDictionary.getOreName(id);
			if (map.get(ore).size() > 0)
				return true;
		}

		return false;
	}

	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
		if (event.getModID().equals(Constants.MOD_ID)) {
			sync();
		}
	}

}
