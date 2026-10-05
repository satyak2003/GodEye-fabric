package com.carlo.story;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class Chapter04 implements Chapter {
    @Override
    public String getId() {
        return "ch04";
    }
    @Override
    public void onStart(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            initPlayer(player);
        }
    }

    @Override
    public void onPlayerJoin(ServerPlayerEntity player) {
        initPlayer(player);
        
        PlayerStoryState pState = PlayerStoryState.getState(player);
        if (pState.getFlag("ch04_snow_active")) {
            net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, new com.carlo.network.CH04SnowPayload(true));
        }
        
        // Failsafe cleanup if they disconnected during witness
        if (pState.getFlag("ch04_witness_active")) {
            net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, new com.carlo.network.ControlLockPayload(false));
            net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, new com.carlo.network.CameraLockPayload(false, 0));
            pState.setFlag("cinematic_locked", false);
            net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, new com.carlo.network.CinematicLockPayload(false));
            player.removeStatusEffect(net.minecraft.entity.effect.StatusEffects.DARKNESS);
            
            // Note: Orphaned Witness entities are cleaned up because they don't persist (unless saved).
            // Let's remove any nearby witness entities just to be sure.
            net.minecraft.util.math.Box box = new net.minecraft.util.math.Box(player.getBlockPos()).expand(50);
            java.util.List<com.carlo.entity.WitnessEntity> mobs = ((net.minecraft.server.world.ServerWorld)player.getEntityWorld()).getEntitiesByClass(com.carlo.entity.WitnessEntity.class, box, e -> true);
            for (com.carlo.entity.WitnessEntity witness : mobs) {
                witness.discard();
            }
            
            pState.setFlag("ch04_witness_active", false);
            
            // Queue dialogue so they don't get softlocked out of the story
            if (!pState.getFlag("ch04_completed")) {
                com.carlo.story.event.EventScheduler.schedule(((net.minecraft.server.world.ServerWorld)player.getEntityWorld()).getServer(), new com.carlo.story.event.ScheduledEvent("ch04_dial_1_" + player.getUuidAsString(), "ch04_dialogue", player.getEntityWorld().getTime() + 40, "ch04", java.util.Optional.of(player.getUuid()), "1", false));
            }
        }
    }

    private void initPlayer(ServerPlayerEntity player) {
        PlayerStoryState pState = PlayerStoryState.getState(player);
        if (!pState.getFlag("ch04_started")) {
            pState.setFlag("ch04_started", true);
            
            net.minecraft.server.world.ServerWorld world = (net.minecraft.server.world.ServerWorld) player.getEntityWorld();
            net.minecraft.util.math.BlockPos villageCenter = world.locateStructure(net.minecraft.registry.tag.StructureTags.VILLAGE, player.getBlockPos(), 100, false);
            if (villageCenter == null) {
                villageCenter = player.getBlockPos();
            }
            pState.setCh04VillageCenter(villageCenter);
            
            long currentTime = world.getTime();
            long delay = 20 * (60 + world.random.nextInt(61)); 
            if (pState.getFlag("ch04_debug_fast")) delay = 20 * 10;
            
            com.carlo.story.event.EventScheduler.schedule(world.getServer(), new com.carlo.story.event.ScheduledEvent(
                "ch04_snow_" + player.getUuidAsString(),
                "ch04_snow_start",
                currentTime + delay,
                "ch04",
                java.util.Optional.of(player.getUuid()),
                "",
                false
            ));
        }
    }

        @Override
    public void tick(MinecraftServer server) {
        if (server.getTicks() % 20 != 0) return; 
        
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            PlayerStoryState pState = PlayerStoryState.getState(player);
            if (pState.getFlag("ch04_started") && !pState.getFlag("ch04_completed")) {
                BlockPos center = pState.getCh04VillageCenter();
                if (center != null) {
                    double dx = player.getX() - center.getX();
                    double dz = player.getZ() - center.getZ();
                    double horizontalDistanceSquared = dx * dx + dz * dz;
                    
                    double boundaryRadiusSquared = 50.0 * 50.0;
                    
                    if (horizontalDistanceSquared > boundaryRadiusSquared) {
                        ServerWorld world = (ServerWorld)player.getEntityWorld();
                        BlockPos safe = com.carlo.util.SafeSurfacePositionResolver.resolveSafeSurface(world, center.getX(), center.getZ(), 5);
                        if (safe != null) {
                            // Ensure the return position is actually inside the boundary!
                            double rx = safe.getX() - center.getX();
                            double rz = safe.getZ() - center.getZ();
                            if ((rx * rx + rz * rz) <= boundaryRadiusSquared) {
                                player.teleport(world, safe.getX() + 0.5, safe.getY(), safe.getZ() + 0.5, java.util.Set.of(), player.getYaw(), player.getPitch(), true);
                            } else {
                                // Fallback directly to center if the resolved surface drifted outside
                                safe = com.carlo.util.SafeSurfacePositionResolver.resolveSafeSurface(world, center.getX(), center.getZ(), 0);
                                if (safe != null) {
                                    player.teleport(world, safe.getX() + 0.5, safe.getY(), safe.getZ() + 0.5, java.util.Set.of(), player.getYaw(), player.getPitch(), true);
                                }
                            }
                        }
                    }
                    
if (pState.getFlag("ch04_snow_active")) {
                        ch04EnvironmentalFreeze((ServerWorld)player.getEntityWorld(), player.getBlockPos());
                        
                        // Freeze entities
                        net.minecraft.util.math.Box box = new net.minecraft.util.math.Box(center.getX() - 60, -64, center.getZ() - 60, center.getX() + 60, 320, center.getZ() + 60);
                        java.util.List<net.minecraft.entity.mob.MobEntity> mobs = ((ServerWorld)player.getEntityWorld()).getEntitiesByClass(net.minecraft.entity.mob.MobEntity.class, box, e -> true);
                        for (net.minecraft.entity.mob.MobEntity mob : mobs) {
                            if (!(mob instanceof com.carlo.entity.WitnessEntity) && !(mob instanceof com.carlo.entity.FrostEntity) && !(mob instanceof com.carlo.entity.WatcherEntity)) {
                                if (mob instanceof com.carlo.util.Ch04Freezable f) {
                                    f.setCh04Frozen(true);
                                }
                            }
                        }
                        
                    }
                }
            }
        }
    }

    private void ch04EnvironmentalFreeze(ServerWorld world, BlockPos origin) {
        // Target surface blocks primarily to efficiently find exposed water and ground
        for (int i=0; i<60; i++) {
            int dx = world.random.nextInt(60) - 30;
            int dz = world.random.nextInt(60) - 30;
            int x = origin.getX() + dx;
            int z = origin.getZ() + dz;
            int y = world.getTopY(net.minecraft.world.Heightmap.Type.MOTION_BLOCKING, x, z) - 1;
            
            BlockPos target = new BlockPos(x, y, z);
            net.minecraft.block.BlockState state = world.getBlockState(target);
            
            if (state.isOf(net.minecraft.block.Blocks.WATER) && state.get(net.minecraft.block.FluidBlock.LEVEL) == 0) {
                world.setBlockState(target, net.minecraft.block.Blocks.ICE.getDefaultState());
            } else if (world.getBlockState(target.up()).isAir() && state.isOpaqueFullCube() && !state.isOf(net.minecraft.block.Blocks.ICE) && !state.isOf(net.minecraft.block.Blocks.SNOW)) {
                world.setBlockState(target.up(), net.minecraft.block.Blocks.SNOW.getDefaultState());
            }
        }
    }

    @Override
    public boolean isComplete(MinecraftServer server) {
        return false;
    }

    @Override
    public String getNextChapterId() {
        return "ch05";
    }
}















