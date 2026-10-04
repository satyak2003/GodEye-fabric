package com.carlo.story.event;

import com.carlo.story.PlayerStoryState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import java.util.LinkedList;
import java.util.Queue;
import java.util.HashSet;
import java.util.Set;

public class LeafDecayStoryEvent implements StoryEvent {
    @Override
    public String getEventType() {
        return "leaf_decay";
    }

    @Override
    public void execute(MinecraftServer server, ScheduledEvent eventData, ServerPlayerEntity player) {
        String activeChap = com.carlo.story.GlobalWorldState.getServerState(server).getActiveChapterId();
        if (activeChap != null && activeChap.compareTo("ch04") >= 0) return;
        if (player == null) return;
        
        ServerWorld world = (ServerWorld) player.getEntityWorld();
        BlockPos center = player.getBlockPos().up();
        
        BlockPos startLeaf = null;
        int checked = 0;
        
        for (int r = 1; r <= 15; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dy = -5; dy <= 10; dy++) {
                    for (int dz = -r; dz <= r; dz++) {
                        if (Math.abs(dx) != r && Math.abs(dz) != r) continue;
                        BlockPos test = center.add(dx, dy, dz);
                        checked++;
                        if (world.getBlockState(test).isIn(BlockTags.LEAVES)) {
                            startLeaf = test;
                            break;
                        }
                    }
                    if (startLeaf != null) break;
                }
                if (startLeaf != null) break;
            }
            if (startLeaf != null) break;
        }
        
        if (startLeaf == null) {
            if (eventData == null) {
                player.sendMessage(net.minecraft.text.Text.literal("LeafDecay: candidates checked = " + checked + ", leaves removed = 0 (No leaves found)"), false);
            }
            return;
        }
        
        Queue<BlockPos> queue = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();
        queue.add(startLeaf);
        visited.add(startLeaf);
        
                int maxDecay = 10 + world.random.nextInt(10); // ch01 default
        String chapter = eventData != null ? eventData.payload() : "";
        if (chapter.equals("ch02")) {
            maxDecay = 25 + world.random.nextInt(20);
        } else if (chapter.equals("ch03")) {
            maxDecay = 50 + world.random.nextInt(30);
        }
        
        int removed = 0;
        while (!queue.isEmpty() && removed < maxDecay) {
            BlockPos curr = queue.poll();
            if (world.getBlockState(curr).isIn(BlockTags.LEAVES)) {
                world.setBlockState(curr, Blocks.AIR.getDefaultState());
                removed++;
                
                for (BlockPos neighbor : new BlockPos[]{curr.up(), curr.down(), curr.north(), curr.south(), curr.east(), curr.west()}) {
                    if (visited.add(neighbor)) {
                        queue.add(neighbor);
                    }
                }
            }
        }
        
        if (removed > 0) {
            PlayerStoryState state = PlayerStoryState.getState(player);
            state.setFlag("ch01_leaf_anomaly_seen", true);
        }
        
        if (eventData == null) {
            player.sendMessage(net.minecraft.text.Text.literal("LeafDecay: candidates checked = " + checked + ", leaves removed = " + removed), false);
        }
    }
}


