package com.carlo.command;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;

import com.carlo.story.ChapterManager;
import com.carlo.story.ChapterRegistry;
import com.carlo.story.GlobalWorldState;
import com.carlo.story.PlayerStoryState;
import com.carlo.story.event.ScheduledEvent;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class GodeyeCommands {
    public static ServerPlayerEntity getTargetPlayer(CommandContext<ServerCommandSource> context) {
        try {
            ServerPlayerEntity p = context.getSource().getPlayer();
            if (p != null) return p;
        } catch (Exception ignored) {}
        java.util.List<ServerPlayerEntity> list = context.getSource().getServer().getPlayerManager().getPlayerList();
        return list.isEmpty() ? null : list.get(0);
    }
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("godeye")
                .requires(net.minecraft.server.command.CommandManager.requirePermissionLevel(net.minecraft.server.command.CommandManager.GAMEMASTERS_CHECK))
                .then(CommandManager.literal("debug")
                    .then(CommandManager.literal("reset")
                        .then(CommandManager.literal("confirm")
                            .executes(context -> {
                                MinecraftServer server = context.getSource().getServer();
                                GlobalWorldState state = GlobalWorldState.getServerState(server);
                                state.getGlobalFlags().clear();
                                state.getScheduledEvents().clear();
                                state.markDirty();
                                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                                    PlayerStoryState pState = PlayerStoryState.getState(player);
                                    pState.getFlags().clear();
                                    pState.setObservationLevel(0);
                                    pState.setTeleportsRemaining(0);
                                    net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, new com.carlo.network.ControlLockPayload(false));
                                }
                                ChapterManager.startNormalTransition(server, "ch00");
                                context.getSource().sendMessage(Text.literal("GodEye progression completely reset to CH00."));
                                return 1;
                            })
                        )
                    )
                    .then(CommandManager.literal("death").executes(context -> {
                        ServerPlayerEntity p = getTargetPlayer(context);
                        if (p != null) {
                            com.carlo.story.system.DeathSystem.onPlayerDeath(p);
                        }
                        return 1;
                    }))
                    .then(CommandManager.literal("ch02_start").executes(context -> {
                        ServerPlayerEntity p = getTargetPlayer(context);
                        if(p != null) {
                            PlayerStoryState state = PlayerStoryState.getState(p);
                            state.setFlag("ch02_started", false);
                            state.setFlag("ch02_responded", false);
                            state.setFlag("ch02_completed", false);
                            state.setFlag("ch02_response_window_active", false);
                            new com.carlo.story.Chapter02().onPlayerJoin(p);
                        }
                        context.getSource().sendMessage(Text.literal("CH02 started for player."));
                        return 1;
                    }))
                    .then(CommandManager.literal("ch02_respond").executes(context -> {
                        ServerPlayerEntity p = getTargetPlayer(context);
                        if(p != null) {
                            new com.carlo.story.event.CH02ResponseLogicStoryEvent().execute(context.getSource().getServer(), null, p);
                        }
                        context.getSource().sendMessage(Text.literal("Forced CH02 response logic."));
                        return 1;
                    }))
                    .then(CommandManager.literal("ch02_timeout").executes(context -> {
                        ServerPlayerEntity p = getTargetPlayer(context);
                        if(p != null) {
                            PlayerStoryState state = PlayerStoryState.getState(p);
                            state.setFlag("ch02_debug_fast", true);
                            context.getSource().sendMessage(Text.literal("Enabled fast mode for timeouts (2s instead of 20s)."));
                        }
                        return 1;
                    }))
                    .then(CommandManager.literal("ch02_jumpscare").executes(context -> {
                        ServerPlayerEntity p = getTargetPlayer(context);
                        if(p != null) {
                            new com.carlo.story.event.CH02JumpscareStoryEvent().execute(context.getSource().getServer(), null, p);
                        }
                        context.getSource().sendMessage(Text.literal("Triggered Frost CH02 jumpscare."));
                        return 1;
                    }))
                                          .then(CommandManager.literal("ch02_reset").executes(context -> {
                          ServerPlayerEntity p = getTargetPlayer(context);
                          if(p != null) {
                              new com.carlo.story.event.CH02DebugEvents.CH02ResetTriggerEvent().execute(context.getSource().getServer(), null, p);
                          }
                          context.getSource().sendMessage(Text.literal("CH02 flags and jumpscare reset for player."));
                          return 1;
                      }))
                                        .then(CommandManager.literal("status")
                        .executes(context -> {
                            MinecraftServer server = context.getSource().getServer();
                            ServerPlayerEntity player = getTargetPlayer(context);
                            GlobalWorldState state = GlobalWorldState.getServerState(server);
                            StringBuilder sb = new StringBuilder();
                            sb.append("\u00A7a=== GodEye Status ===\u00A7r\n");
                            sb.append("Active Chapter: ").append(state.getActiveChapterId()).append("\n");
                            sb.append("Scheduled Events: ").append(state.getScheduledEvents().size()).append("\n");
                            for (ScheduledEvent ev : state.getScheduledEvents()) {
                                sb.append(" - [").append(ev.chapterId()).append("] ").append(ev.eventType()).append(" (target: ").append(ev.targetTick()).append(")\n");
                            }
                            if (player != null) {
                                PlayerStoryState pState = PlayerStoryState.getState(player);
                                sb.append("\u00A7e--- Player State ---\u00A7r\n");
                                for (String flag : pState.getFlags().keySet()) {
                                    sb.append(" - ").append(flag).append(": ").append(pState.getFlags().get(flag)).append("\n");
                                }
                            }
                            context.getSource().sendMessage(Text.literal(sb.toString()));
                            return 1;
                        })
                    )
                                        .then(CommandManager.literal("watcher").executes(context -> {
                        ServerPlayerEntity p = getTargetPlayer(context);
                        if (p != null) {
                            com.carlo.entity.WatcherEntity watcher = com.carlo.story.system.watcher.WatcherEncounterSystem.spawnWatcher(p, 12.0, 20.0);
                            if (watcher != null) {
                                double dist = p.distanceTo(watcher);
                                context.getSource().sendMessage(net.minecraft.text.Text.literal(
                                    String.format("Watcher spawned at:\nX: %d\nY: %d\nZ: %d\nDistance: %.1f blocks",
                                    (int)watcher.getX(), (int)watcher.getY(), (int)watcher.getZ(), dist)
                                ));
                            } else {
                                context.getSource().sendMessage(net.minecraft.text.Text.literal("Failed to find safe spawn for Watcher."));
                            }
                        }
                        return 1;
                    }))
                    .then(CommandManager.literal("watcher_seen").executes(context -> {
                        ServerPlayerEntity p = getTargetPlayer(context);
                        if(p != null) {
                            PlayerStoryState.getState(p).setFlag("ch03_watcher_seen", true);
                            context.getSource().sendMessage(Text.literal("Forced CH03 Watcher seen flag."));
                        }
                        return 1;
                    }))
                    .then(CommandManager.literal("watcher_relocate").executes(context -> {
                        ServerPlayerEntity p = getTargetPlayer(context);
                        if(p != null) {
                            String uuidStr = "";
                            for(ScheduledEvent ev : GlobalWorldState.getServerState(context.getSource().getServer()).getScheduledEvents()) {
                                if (ev.eventType().equals("ch03_observation_tick") && ev.playerUuid().isPresent() && ev.playerUuid().get().equals(p.getUuid())) {
                                    uuidStr = ev.payload();
                                    break;
                                }
                            }
                            new com.carlo.story.event.CH03WatcherRelocateEvent().execute(context.getSource().getServer(), new ScheduledEvent("dbg", "dbg", 0, "ch03", java.util.Optional.of(p.getUuid()), uuidStr, false), p);
                            context.getSource().sendMessage(Text.literal("Triggered CH03WatcherRelocateEvent."));
                        }
                        return 1;
                    }))
                    .then(CommandManager.literal("watcher_vanish").executes(context -> {
                        ServerPlayerEntity p = getTargetPlayer(context);
                        if(p != null) {
                            String uuidStr = "";
                            for(ScheduledEvent ev : GlobalWorldState.getServerState(context.getSource().getServer()).getScheduledEvents()) {
                                if (ev.eventType().equals("ch03_observation_tick") && ev.playerUuid().isPresent() && ev.playerUuid().get().equals(p.getUuid())) {
                                    uuidStr = ev.payload();
                                    break;
                                }
                            }
                            new com.carlo.story.event.CH03WatcherApproachEvent().execute(context.getSource().getServer(), new ScheduledEvent("dbg", "dbg", 0, "ch03", java.util.Optional.of(p.getUuid()), uuidStr, false), p);
                            context.getSource().sendMessage(Text.literal("Triggered CH03WatcherApproachEvent."));
                        }
                        return 1;
                    }))
                                        .then(CommandManager.literal("skip_timers").executes(context -> {
                        MinecraftServer server = context.getSource().getServer();
                        GlobalWorldState state = GlobalWorldState.getServerState(server);
                        long currentTick = server.getWorld(net.minecraft.world.World.OVERWORLD).getTime();
                        for (int i = 0; i < state.getScheduledEvents().size(); i++) {
                            ScheduledEvent e = state.getScheduledEvents().get(i);
                            state.getScheduledEvents().set(i, new ScheduledEvent(e.eventId(), e.eventType(), currentTick, e.chapterId(), e.playerUuid(), e.payload(), e.isCompleted()));
                        }
                        state.markDirty();
                        context.getSource().sendMessage(Text.literal("Fast-forwarded all scheduled events to execute immediately."));
                        return 1;
                    }))
                    .then(CommandManager.literal("frost").executes(context -> {
                        ServerPlayerEntity p = getTargetPlayer(context);
                        if (p != null) {
                            com.carlo.entity.FrostEntity frost = new com.carlo.entity.FrostEntity(com.carlo.Godeye.FROST, ((net.minecraft.server.world.ServerWorld)p.getEntityWorld()));
                            frost.refreshPositionAndAngles(p.getX() + 5, p.getY(), p.getZ(), 0, 0);
                            ((net.minecraft.server.world.ServerWorld)p.getEntityWorld()).spawnEntity(frost);
                            context.getSource().sendMessage(Text.literal("Spawned Frost for debug."));
                        }
                        return 1;
                    }))
                    .then(CommandManager.literal("trigger")
                        .then(CommandManager.argument("event_id", StringArgumentType.string())
                            .executes(context -> {
                                String eventId = StringArgumentType.getString(context, "event_id");
                                com.carlo.story.event.StoryEvent logic = com.carlo.story.event.EventRegistry.get(eventId);
                                if (logic != null) {
                                    logic.execute(context.getSource().getServer(), null, getTargetPlayer(context));
                                    context.getSource().sendMessage(Text.literal("Triggered event: " + eventId));
                                } else {
                                    context.getSource().sendMessage(Text.literal("Event logic not found for: " + eventId));
                                }
                                return 1;
                            })
                        )
                    )
                    .then(CommandManager.literal("observation")
                        .then(CommandManager.argument("value", com.mojang.brigadier.arguments.IntegerArgumentType.integer())
                            .executes(context -> {
                                int val = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "value");
                                ServerPlayerEntity p = getTargetPlayer(context);
                                if (p != null) {
                                    PlayerStoryState.getState(p).setObservationLevel(val);
                                    context.getSource().sendMessage(Text.literal("Set observation level to " + val));
                                }
                                return 1;
                            })
                        )
                    )
                                          .then(CommandManager.literal("ch03_encounter").executes(context -> {
                          ServerPlayerEntity p = getTargetPlayer(context);
                          if(p != null) {
                              new com.carlo.story.event.CH03EncounterCompleteEvent().execute(context.getSource().getServer(), new com.carlo.story.event.ScheduledEvent("dbg", "ch03_encounter_complete", 0, "ch03", java.util.Optional.empty(), "dbg", false), p);
                              context.getSource().sendMessage(net.minecraft.text.Text.literal("Forced CH03 encounter to complete."));
                          }
                          return 1;
                      }))
                      .then(CommandManager.literal("ch03_finale").executes(context -> {
                          ServerPlayerEntity p = getTargetPlayer(context);
                          if(p != null) {
                              new com.carlo.story.event.CH03FinaleStartEvent().execute(context.getSource().getServer(), null, p);
                              context.getSource().sendMessage(net.minecraft.text.Text.literal("Forced CH03 finale start."));
                          }
                          return 1;
                      }))
                      .then(CommandManager.literal("ch03_state").executes(context -> {
                          ServerPlayerEntity p = getTargetPlayer(context);
                          if(p != null) {
                              com.carlo.story.PlayerStoryState state = com.carlo.story.PlayerStoryState.getState(p);
                              int count = state.getCh03EncounterCount();
                              int relocs = state.getCh03RelocationCount();
                              boolean started = state.getFlag("ch03_started");
                              boolean completed = state.getFlag("ch03_completed");
                              context.getSource().sendMessage(net.minecraft.text.Text.literal(String.format("CH03 State:\nEncounters: %d\nRelocs: %d\nStarted: %b\nCompleted: %b", count, relocs, started, completed)));
                          }
                          return 1;
                      }))
                                          .then(CommandManager.literal("ch04_start").executes(context -> {
                        ServerPlayerEntity p = getTargetPlayer(context);
                        if(p != null) {
                            com.carlo.story.ChapterManager.debugForceChapter(context.getSource().getServer(), "ch04");
                            context.getSource().sendMessage(net.minecraft.text.Text.literal("Started CH04."));
                        }
                        return 1;
                    }))
                    .then(CommandManager.literal("ch04_witness").executes(context -> {
                        ServerPlayerEntity p = getTargetPlayer(context);
                        if(p != null) {
                            com.carlo.story.event.EventScheduler.schedule(context.getSource().getServer(), new com.carlo.story.event.ScheduledEvent("ch04_witness_" + p.getUuidAsString(), "ch04_witness_start", context.getSource().getServer().getOverworld().getTime() + 20, "ch04", java.util.Optional.of(p.getUuid()), "", false));
                            context.getSource().sendMessage(net.minecraft.text.Text.literal("Triggering CH04 Witness Sequence."));
                        }
                        return 1;
                    }))
                                        .then(CommandManager.literal("ch04_state").executes(context -> {
                        ServerPlayerEntity p = getTargetPlayer(context);
                        if (p != null) {
                            com.carlo.story.GlobalWorldState gState = com.carlo.story.GlobalWorldState.getServerState(context.getSource().getServer());
                            com.carlo.story.PlayerStoryState state = com.carlo.story.PlayerStoryState.getState(p);
                            
                            java.util.List<com.carlo.story.event.ScheduledEvent> allEvents = gState.getScheduledEvents();
                            java.util.List<com.carlo.story.event.ScheduledEvent> ch04Events = new java.util.ArrayList<>();
                            for (com.carlo.story.event.ScheduledEvent e : allEvents) {
                                if ("ch04".equals(e.chapterId()) || e.eventId().startsWith("ch04")) {
                                    ch04Events.add(e);
                                }
                            }
                            
                                                                                    net.minecraft.util.math.BlockPos center = state.getCh04VillageCenter();
                            double radius = 50.0;
                            double hDist = 0.0;
                            boolean inside = false;
                            if (center != null) {
                                double dx = p.getX() - center.getX();
                                double dz = p.getZ() - center.getZ();
                                hDist = Math.sqrt(dx * dx + dz * dz);
                                inside = hDist <= radius;
                            }
                            
                            context.getSource().sendMessage(net.minecraft.text.Text.literal(
                                "\u00A7b=== CH04 DEBUG STATE ===\n" +
                                "\u00A77Active Chapter: \u00A7f" + gState.getActiveChapterId() + "\n" +
                                "\u00A77Player Started: \u00A7f" + state.getFlag("ch04_started") + "\n" +
                                "\u00A77Snow Active: \u00A7f" + state.getFlag("ch04_snow_active") + "\n" +
                                "\u00A77Witness Active: \u00A7f" + state.getFlag("ch04_witness_active") + "\n" +
                                "\u00A77Player Completed: \u00A7f" + state.getFlag("ch04_completed") + "\n" +
                                "\u00A77Scheduled CH04 Events: \u00A7f" + ch04Events.size() + "\n" +
                                "\u00A7e--- BOUNDARY INFO ---\n" +
                                "\u00A77Village Center: \u00A7f" + (center != null ? "X:" + center.getX() + " Y:" + center.getY() + " Z:" + center.getZ() : "NULL") + "\n" +
                                "\u00A77Boundary Radius: \u00A7f" + radius + "\n" +
                                "\u00A77Player Position: \u00A7fX:" + Math.round(p.getX()) + " Y:" + Math.round(p.getY()) + " Z:" + Math.round(p.getZ()) + "\n" +
                                "\u00A77Horizontal Dist: \u00A7f" + String.format("%.2f", hDist) + "\n" +
                                "\u00A77Inside Boundary: \u00A7f" + inside
                            ));
                            
                            for (com.carlo.story.event.ScheduledEvent e : ch04Events) {
                                context.getSource().sendMessage(net.minecraft.text.Text.literal(
                                    "  \u00A7e- ID: \u00A7f" + e.eventId() + " \u00A77Type: \u00A7f" + e.eventType() + " \u00A77Target: \u00A7f" + (e.playerUuid().isPresent() ? e.playerUuid().get().toString() : "GLOBAL")
                                ));
                            }
                        }
                        return 1;
                    }))
                    .then(CommandManager.literal("ch04_freeze").executes(context -> {
                        ServerPlayerEntity p = getTargetPlayer(context);
                        if (p != null) {
                            com.carlo.story.event.EventScheduler.schedule(context.getSource().getServer(), new com.carlo.story.event.ScheduledEvent("ch04_pl_freeze_" + p.getUuidAsString(), "ch04_player_freeze", context.getSource().getServer().getOverworld().getTime() + 20, "ch04", java.util.Optional.of(p.getUuid()), "", false));
                            context.getSource().sendMessage(net.minecraft.text.Text.literal("Triggering CH04 Player Freeze."));
                        }
                        return 1;
                    }))
                    .then(CommandManager.literal("ch04_snow").executes(context -> {
                        ServerPlayerEntity p = getTargetPlayer(context);
                        if (p != null) {
                            com.carlo.story.event.EventScheduler.schedule(context.getSource().getServer(), new com.carlo.story.event.ScheduledEvent("ch04_snow_" + p.getUuidAsString(), "ch04_snow_start", context.getSource().getServer().getOverworld().getTime() + 20, "ch04", java.util.Optional.of(p.getUuid()), "", false));
                            context.getSource().sendMessage(net.minecraft.text.Text.literal("Triggering CH04 Snow Phase."));
                        }
                        return 1;
                    }))
                    .then(CommandManager.literal("ch04_night").executes(context -> {
                        ServerPlayerEntity p = getTargetPlayer(context);
                        if (p != null) {
                            com.carlo.story.event.EventScheduler.schedule(context.getSource().getServer(), new com.carlo.story.event.ScheduledEvent("ch04_night_" + p.getUuidAsString(), "ch04_night_start", context.getSource().getServer().getOverworld().getTime() + 20, "ch04", java.util.Optional.of(p.getUuid()), "", false));
                            context.getSource().sendMessage(net.minecraft.text.Text.literal("Triggering CH04 Night."));
                        }
                        return 1;
                    }))
                    .then(CommandManager.literal("ch04_reset").executes(context -> {
                        ServerPlayerEntity p = getTargetPlayer(context);
                        if (p != null) {
                            com.carlo.story.PlayerStoryState state = com.carlo.story.PlayerStoryState.getState(p);
                            state.setFlag("ch04_started", false);
                            state.setFlag("ch04_snow_active", false);
                            state.setFlag("ch04_completed", false);
                            context.getSource().sendMessage(net.minecraft.text.Text.literal("Reset CH04 flags for player."));
                        }
                        return 1;
                    }))

                    .then(CommandManager.literal("ch03_start").executes(context -> {
                        ServerPlayerEntity p = getTargetPlayer(context);
                        if(p != null) {
                            ChapterManager.debugForceChapter(context.getSource().getServer(), "ch03");
                            context.getSource().sendMessage(Text.literal("Started CH03."));
                        }
                        return 1;
                    }))
                    .then(CommandManager.literal("ch03_complete").executes(context -> {
                        ServerPlayerEntity p = getTargetPlayer(context);
                        if(p != null) {
                            new com.carlo.story.event.CH03EndEvent().execute(context.getSource().getServer(), null, p);
                            context.getSource().sendMessage(Text.literal("Completed CH03."));
                        }
                        return 1;
                    }))
                    .then(CommandManager.literal("watcher_finale").executes(context -> {
                        ServerPlayerEntity p = getTargetPlayer(context);
                        if(p != null) {
                            new com.carlo.story.event.CH03FinaleStartEvent().execute(context.getSource().getServer(), null, p);
                            context.getSource().sendMessage(net.minecraft.text.Text.literal("Started Watcher Finale."));
                        }
                        return 1;
                    }))
                    .then(CommandManager.literal("watcher_finale_watchers").executes(context -> {
                        ServerPlayerEntity p = getTargetPlayer(context);
                        if(p != null) {
                            // Only spawn the 7-9 watchers
                            net.minecraft.server.world.ServerWorld world = (net.minecraft.server.world.ServerWorld) p.getEntityWorld();
                            int count = 7 + world.random.nextInt(3);
                            for (int i = 0; i < count; i++) {
                                double distance = 6.0 + world.random.nextDouble() * 6.0;
                                float angle = p.getYaw() + (world.random.nextFloat() - 0.5f) * 120.0f;
                                net.minecraft.util.math.Vec3d offset = net.minecraft.util.math.Vec3d.fromPolar(0, angle).multiply(distance);
                                double targetX = p.getX() + offset.x;
                                double targetZ = p.getZ() + offset.z;
                                net.minecraft.util.math.BlockPos testPos = new net.minecraft.util.math.BlockPos((int)targetX, (int)p.getY(), (int)targetZ);
                                int[] yOffsets = {0, 1, -1, 2, -2};
                                net.minecraft.util.math.BlockPos safePos = null;
                                for (int dy : yOffsets) {
                                    net.minecraft.util.math.BlockPos pos = testPos.up(dy);
                                    if (world.getBlockState(pos.down()).isSolid() && world.getBlockState(pos).isAir() && world.getBlockState(pos.up()).isAir() && world.getFluidState(pos).isEmpty()) {
                                        safePos = pos;
                                        break;
                                    }
                                }
                                if (safePos != null) {
                                    com.carlo.entity.WatcherEntity watcher = new com.carlo.entity.WatcherEntity(com.carlo.Godeye.GODEYE_WATCHER, world);
                                    watcher.setPosition(safePos.getX() + 0.5, safePos.getY(), safePos.getZ() + 0.5);
                                    watcher.setLifespan(400);
                                    world.spawnEntity(watcher);
                                }
                            }
                            context.getSource().sendMessage(net.minecraft.text.Text.literal("Spawned Watcher Finale Watchers."));
                        }
                        return 1;
                    }))
                    .then(CommandManager.literal("watcher_finale_cinematic").executes(context -> {
                        ServerPlayerEntity p = getTargetPlayer(context);
                        if(p != null) {
                            long startTick = context.getSource().getServer().getOverworld().getTime();
                            com.carlo.story.event.EventScheduler.schedule(context.getSource().getServer(), new com.carlo.story.event.ScheduledEvent("dbg_fin_1", "ch03_finale_cinematic", startTick + 20, "ch03", java.util.Optional.of(p.getUuid()), "blackscreen", false));
                            com.carlo.story.event.EventScheduler.schedule(context.getSource().getServer(), new com.carlo.story.event.ScheduledEvent("dbg_fin_2", "ch03_finale_cinematic", startTick + 60, "ch03", java.util.Optional.of(p.getUuid()), "text1", false));
                            com.carlo.story.event.EventScheduler.schedule(context.getSource().getServer(), new com.carlo.story.event.ScheduledEvent("dbg_fin_3", "ch03_finale_cinematic", startTick + 160, "ch03", java.util.Optional.of(p.getUuid()), "text2", false));
                            com.carlo.story.event.EventScheduler.schedule(context.getSource().getServer(), new com.carlo.story.event.ScheduledEvent("dbg_fin_4", "ch03_finale_cinematic", startTick + 260, "ch03", java.util.Optional.of(p.getUuid()), "text3", false));
                            com.carlo.story.event.EventScheduler.schedule(context.getSource().getServer(), new com.carlo.story.event.ScheduledEvent("dbg_fin_5", "ch03_finale_cinematic", startTick + 360, "ch03", java.util.Optional.of(p.getUuid()), "teleport", false));
                            context.getSource().sendMessage(net.minecraft.text.Text.literal("Started Watcher Finale Cinematic."));
                        }
                        return 1;
                    }))
                    .then(CommandManager.literal("chapter")
                        .then(CommandManager.argument("chapter_id", StringArgumentType.string())
                            .executes(context -> {
                                String chapterId = StringArgumentType.getString(context, "chapter_id");
                                MinecraftServer server = context.getSource().getServer();
                                if (ChapterRegistry.get(chapterId) == null) {
                                    context.getSource().sendMessage(Text.literal("Chapter not found: " + chapterId));
                                    return 0;
                                }
                                ChapterManager.debugForceChapter(server, chapterId);
                                context.getSource().sendMessage(Text.literal("Transitioned to chapter: " + chapterId));
                                return 1;
                            })
                            .then(CommandManager.literal("fast")
                                .executes(context -> {
                                    String chapterId = StringArgumentType.getString(context, "chapter_id");
                                    MinecraftServer server = context.getSource().getServer();
                                    if (ChapterRegistry.get(chapterId) == null) {
                                        context.getSource().sendMessage(Text.literal("Chapter not found: " + chapterId));
                                        return 0;
                                    }
                                    
                                    GlobalWorldState state = GlobalWorldState.getServerState(server);
                                    if (!chapterId.equals(state.getActiveChapterId())) {
                                        ChapterManager.debugForceChapter(server, chapterId);
                                    }
                                    
                                    long currentTick = server.getWorld(net.minecraft.world.World.OVERWORLD).getTime();
                                    long maxDelay = 0;
                                    for (ScheduledEvent e : state.getScheduledEvents()) {
                                        if (e.chapterId().equals(chapterId)) {
                                            long delay = e.targetTick() - currentTick;
                                            if (delay > maxDelay) maxDelay = delay;
                                        }
                                    }
                                    if (maxDelay > 0) {
                                        long fastMax = 600;
                                        double scale = Math.min(1.0, (double)fastMax / maxDelay);
                                        for (int i = 0; i < state.getScheduledEvents().size(); i++) {
                                            ScheduledEvent e = state.getScheduledEvents().get(i);
                                            if (e.chapterId().equals(chapterId)) {
                                                long delay = e.targetTick() - currentTick;
                                                long fastDelay = (long)(delay * scale);
                                                ScheduledEvent compressed = new ScheduledEvent(e.eventId(), e.eventType(), currentTick + fastDelay, e.chapterId(), e.playerUuid(), e.payload(), e.isCompleted());
                                                state.getScheduledEvents().set(i, compressed);
                                            }
                                        }
                                        state.markDirty();
                                    }
                                                                        ServerPlayerEntity cmdPlayer = null;
                                    try { cmdPlayer = getTargetPlayer(context); } catch (Exception ignored) {}
                                    if (cmdPlayer != null) {
                                        PlayerStoryState.getState(cmdPlayer).setFlag(chapterId + "_debug_fast", true);
                                    } else {
                                        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
                                            PlayerStoryState.getState(p).setFlag(chapterId + "_debug_fast", true);
                                        }
                                    }
                                    state.setFlag(chapterId + "_debug_fast", true);
                                    context.getSource().sendMessage(Text.literal("Transitioned to chapter (FAST MODE): " + chapterId));
                                    return 1;
                                })
                            )
                        )
                    )
                )
            );
        });
    }
}


















