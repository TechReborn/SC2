package vswe.stevescarts.compat.railcraft;

import mods.railcraft.api.tracks.IOutfittedTrackTile;
import mods.railcraft.api.tracks.TrackToolsAPI;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import vswe.stevescarts.compat.railcraft.kit.KitAdvDetector;

public class RailcraftHook {

    private static RailcraftHook instance = new RailcraftHook();

    public static RailcraftHook getInstance() {
        return instance;
    }

    static void init() {
        instance = new Exist();
    }

    RailcraftHook() {
    }

    public boolean isAdvDetector(World world, BlockPos pos, IBlockState state) {
        return false;
    }

    public boolean isJunction(World world, BlockPos pos, IBlockState state) {
        return false;
    }

    static final class Exist extends RailcraftHook {

        @Override
        public boolean isAdvDetector(World world, BlockPos pos, IBlockState state) {
            if (state.getBlock() != TrackToolsAPI.blockTrackOutfitted) {
                return false;
            }
            TileEntity te = world.getTileEntity(pos);
            if (!(te instanceof IOutfittedTrackTile)) {
                return false;
            }
            return ((IOutfittedTrackTile) te).getTrackKitInstance().getTrackKit() == KitAdvDetector.KIT;
        }

        @Override
        public boolean isJunction(World world, BlockPos pos, IBlockState state) {
            if (state.getBlock() != TrackToolsAPI.blockTrackOutfitted) {
                return false;
            }
            TileEntity te = world.getTileEntity(pos);
            if (!(te instanceof IOutfittedTrackTile)) {
                return false;
            }
            return ((IOutfittedTrackTile) te).getTrackKitInstance().getTrackKit() == KitAdvDetector.KIT;
        }
    }
}
