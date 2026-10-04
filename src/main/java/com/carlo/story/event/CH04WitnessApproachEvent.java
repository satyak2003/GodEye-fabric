package com.carlo.story.event;

import com.carlo.entity.WitnessEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import com.carlo.network.CinematicLockPayload;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

public class CH04WitnessApproachEvent implements StoryEvent {
    @Override
    public String getEventType() {
        return "ch04_witness_approach";
    }

    @Override
    public void execute(MinecraftServer server, ScheduledEvent eventData, ServerPlayerEntity player) {
        if (player == null) return;
        ServerWorld world = (ServerWorld) player.getEntityWorld();
        try {
            String[] mainParts = eventData.payload().split("\\|");
            String witnessUuid = mainParts[0];
            String data = mainParts.length > 1 ? mainParts[1] : "";
            
            Entity entity = world.getEntity(UUID.fromString(witnessUuid));
            if (entity instanceof WitnessEntity witness) {
                double dist = player.distanceTo(witness);
                
                int ticksActive = 0;
                int stallCount = 0;
                double lastX = witness.getX(), lastY = witness.getY(), lastZ = witness.getZ();
                
                if (data != null && data.contains(";")) {
                    String[] parts = data.split(";");
                    if (parts.length >= 5) {
                        ticksActive = Integer.parseInt(parts[0]);
                        stallCount = Integer.parseInt(parts[1]);
                        lastX = Double.parseDouble(parts[2]);
                        lastY = Double.parseDouble(parts[3]);
                        lastZ = Double.parseDouble(parts[4]);
                    }
                }
                
                ticksActive++;
                
                if (ticksActive % 20 == 0) {
                    double moveDist = Math.sqrt(Math.pow(witness.getX() - lastX, 2) + Math.pow(witness.getY() - lastY, 2) + Math.pow(witness.getZ() - lastZ, 2));
                    if (moveDist < 0.2) {
                        stallCount++;
                    } else {
                        stallCount = 0; // reset if moving
                    }
                    lastX = witness.getX();
                    lastY = witness.getY();
                    lastZ = witness.getZ();
                }

                double dx = player.getX() - witness.getX();
                double dz = player.getZ() - witness.getZ();
                float yaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
                witness.setYaw(yaw); witness.setBodyYaw(yaw); witness.setHeadYaw(yaw);
                
                // 35 sec * 20 = 700 ticks timeout. Stalled 3 times (3 seconds) -> failsafe.
                if (dist <= 3.0 || ticksActive > 700 || stallCount >= 3) {
                    EventScheduler.schedule(server, new ScheduledEvent("ch04_wit_dark_" + player.getUuidAsString(), "ch04_witness_darkness", world.getTime(), "ch04", Optional.of(player.getUuid()), witnessUuid, false));
                } else {
                    net.minecraft.util.math.Vec3d forward = net.minecraft.util.math.Vec3d.fromPolar(0, yaw).multiply(0.05);
                    double newX = witness.getX() + forward.x;
                    double newZ = witness.getZ() + forward.z;
                    double newY = witness.getY();
                    
                    BlockPos checkPos = BlockPos.ofFloored(newX, newY, newZ);
                    boolean foundGround = false;
                    for (int yOffset = 2; yOffset >= -2; yOffset--) {
                        BlockPos pos = checkPos.up(yOffset);
                        if (world.getBlockState(pos).isSolidBlock(world, pos) && !world.getBlockState(pos.up()).isSolidBlock(world, pos.up())) {
                            newY = pos.getY() + 1.0;
                            foundGround = true;
                            break;
                        }
                    }
                    
                    double targetY = foundGround ? newY : witness.getY();
                    if (witness.getY() < targetY) {
                        newY = Math.min(targetY, witness.getY() + 0.15); // Step up
                    } else if (witness.getY() > targetY) {
                        newY = Math.max(targetY, witness.getY() - 0.15); // Step down
                    } else {
                        newY = witness.getY();
                    }
                    witness.setPosition(newX, newY, newZ);
                    
                    int minX = net.minecraft.util.math.MathHelper.floor(witness.getX() - 1.0);
                    int maxX = net.minecraft.util.math.MathHelper.ceil(witness.getX() + 1.0);
                    int minY = net.minecraft.util.math.MathHelper.floor(witness.getY());
                    int maxY = net.minecraft.util.math.MathHelper.ceil(witness.getY() + 2.5);
                    int minZ = net.minecraft.util.math.MathHelper.floor(witness.getZ() - 1.0);
                    int maxZ = net.minecraft.util.math.MathHelper.ceil(witness.getZ() + 1.0);
                    
                    for (int bx = minX; bx <= maxX; bx++) {
                        for (int by = minY; by <= maxY; by++) {
                            for (int bz = minZ; bz <= maxZ; bz++) {
                                BlockPos p = new BlockPos(bx, by, bz);
                                net.minecraft.block.BlockState st = world.getBlockState(p);
                                if (!st.isAir() && st.getHardness(world, p) >= 0.0f) {
                                    if (!st.isIn(net.minecraft.registry.tag.BlockTags.WITHER_IMMUNE) && !st.isOf(net.minecraft.block.Blocks.COMMAND_BLOCK)) {
                                        world.setBlockState(p, net.minecraft.block.Blocks.AIR.getDefaultState(), 3);
                                    }
                                }
                            }
                        }
                    }

                    String newData = ticksActive + ";" + stallCount + ";" + lastX + ";" + lastY + ";" + lastZ;
                    String newPayload = witnessUuid + "|" + newData;
                    EventScheduler.schedule(server, new ScheduledEvent("ch04_wit_app_" + player.getUuidAsString(), "ch04_witness_approach", world.getTime() + 1, "ch04", Optional.of(player.getUuid()), newPayload, false));
                }
            } else {
                player.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.StopSoundS2CPacket(net.minecraft.util.Identifier.of("godeye", "witness_encounter_1"), net.minecraft.sound.SoundCategory.HOSTILE));
                player.removeStatusEffect(net.minecraft.entity.effect.StatusEffects.DARKNESS);
                net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, new com.carlo.network.CameraLockPayload(false, 0));
                net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, new com.carlo.network.ControlLockPayload(false));
                net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, new CinematicLockPayload(false));
            }
        } catch(Exception ignored){}
    }
}
