package vswe.stevescarts.compat.railcraft.kit;

import mods.railcraft.api.tracks.TrackKit;
import mods.railcraft.api.tracks.TrackKitInstance;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.util.ResourceLocation;
import vswe.stevescarts.Constants;
import vswe.stevescarts.blocks.BlockRailJunction;
import vswe.stevescarts.entitys.EntityMinecartModular;

public class KitJunction extends TrackKitInstance {

    public static final TrackKit KIT = new TrackKit.Builder(new ResourceLocation(Constants.MOD_ID, "junction"), KitJunction.class)
            .setAllowedOnSlopes(false)
            .setRenderer(TrackKit.Renderer.UNIFIED)
            .setRenderStates(6) //TODO
            .build();

    @Override
    public TrackKit getTrackKit() {
        return KIT;
    }

    public KitJunction() {
    }

    @Override
    public BlockRailBase.EnumRailDirection getRailDirection(IBlockState state, EntityMinecart cart) {
        if (cart instanceof EntityMinecartModular) {
            final EntityMinecartModular modularCart = (EntityMinecartModular) cart;
            BlockRailBase.EnumRailDirection direction = modularCart.getRailDirection(getPos());
            if (direction != null) {
                return direction;
            }
        }
        return super.getRailDirection(state, cart);
    }
}
