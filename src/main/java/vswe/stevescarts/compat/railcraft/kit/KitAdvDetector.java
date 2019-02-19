package vswe.stevescarts.compat.railcraft.kit;

import mods.railcraft.api.tracks.TrackKit;
import mods.railcraft.api.tracks.TrackKitInstance;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import vswe.stevescarts.Constants;
import vswe.stevescarts.blocks.BlockRailAdvDetector;

public class KitAdvDetector extends TrackKitInstance {

    public static final TrackKit KIT = new TrackKit.Builder(new ResourceLocation(Constants.MOD_ID, "advanced_detector"), KitAdvDetector.class)
            .setAllowedOnSlopes(false)
            .setRenderStates(1)
            .build();
    private static BlockRailAdvDetector block;

    @Override
    public TrackKit getTrackKit() {
        return KIT;
    }

    public static void setAdvDetector(BlockRailAdvDetector detector) {
        block = detector;
    }

    public KitAdvDetector() {
    }

    @Override
    public void onMinecartPass(EntityMinecart cart) {
        block.onMinecartPass(theWorldAsserted(), cart, getPos());
    }

    // TODO canConnectRedstone not accomplishable


    @Override public boolean blockActivated(EntityPlayer player, EnumHand hand) {
        return block.onBlockActivated(theWorldAsserted(), getPos(), theWorldAsserted().getBlockState(getPos()), player, hand, EnumFacing.UP, 0, 0, 0);
    }
}
