package com.carlo.util;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.Heightmap;

public class SafeSurfacePositionResolver {

    public static BlockPos resolveSafeSurface(ServerWorld world, int targetX, int targetZ, int searchRadius) {
        for (int r = 0; r <= searchRadius; r += 2) {
            for (int dx = -r; dx <= r; dx += 2) {
                for (int dz = -r; dz <= r; dz += 2) {
                    if (Math.abs(dx) != r && Math.abs(dz) != r && r != 0) continue;
                    
                    int x = targetX + dx;
                    int z = targetZ + dz;
                    
                    world.getChunk(x >> 4, z >> 4, ChunkStatus.FULL, true);
                    
                    int y = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
                    
                    BlockPos safePos = findSafeYAt(world, x, z, y);
                    if (safePos != null) {
                        return safePos;
                    }
                }
            }
        }
        return null;
    }

    private static BlockPos findSafeYAt(ServerWorld world, int x, int z, int startY) {
        for (int y = Math.min(319, startY + 30); y > 40; y--) {
            BlockPos footPos = new BlockPos(x, y, z);
            BlockPos groundPos = footPos.down();
            
            BlockState groundState = world.getBlockState(groundPos);
            BlockState footState = world.getBlockState(footPos);
            BlockState bodyState = world.getBlockState(footPos.up());
            BlockState headState = world.getBlockState(footPos.up(2));
            
            if (isSafeGround(world, groundPos, groundState) && 
                isSafeAir(world, footPos, footState) && 
                isSafeAir(world, footPos.up(), bodyState) && 
                isSafeAir(world, footPos.up(2), headState)) {
                
                // NEW: Validate open sky (Surface / Outdoor check)
                if (world.isSkyVisible(footPos) || world.isSkyVisible(footPos.up()) || world.isSkyVisible(footPos.up(2))) {
                    return footPos;
                }
            }
        }
        return null;
    }

    private static boolean isSafeGround(ServerWorld world, BlockPos pos, BlockState state) {
        if (state.isAir()) return false;
        if (!state.isOpaqueFullCube()) {
            if (state.isOf(Blocks.DIRT_PATH) || state.isOf(Blocks.FARMLAND) || state.isIn(net.minecraft.registry.tag.BlockTags.DIRT)) {
                return true;
            }
            return false;
        }
        if (state.isOf(Blocks.MAGMA_BLOCK) || state.isOf(Blocks.CACTUS) || state.isOf(Blocks.CAMPFIRE) || state.isOf(Blocks.SOUL_CAMPFIRE)) {
            return false;
        }
        if (!state.getFluidState().isEmpty()) {
            return false;
        }
        if (state.isIn(net.minecraft.registry.tag.BlockTags.LEAVES) || state.isIn(net.minecraft.registry.tag.BlockTags.LOGS)) {
            return false;
        }
        return true;
    }

    private static boolean isSafeAir(ServerWorld world, BlockPos pos, BlockState state) {
        if (state.isAir()) return true;
        if (!state.getFluidState().isEmpty()) return false;
        
        if (state.isIn(net.minecraft.registry.tag.BlockTags.FLOWERS) || state.isOf(Blocks.SHORT_GRASS) || state.isOf(Blocks.TALL_GRASS) || state.isOf(Blocks.FERN) || state.isOf(Blocks.LARGE_FERN)) {
            return true;
        }
        return state.getCollisionShape(world, pos).isEmpty();
    }
}
