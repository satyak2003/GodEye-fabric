import re

with open('src/main/java/com/carlo/story/Chapter04.java', 'r', encoding='utf-8') as f:
    code = f.read()

replacement = '''
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
                        
                        if (!pState.getFlag("ch04_witness_active")) {
                            if (player.getRandom().nextInt(20) == 0) {
                                player.setFrozenTicks(800); // 800 ticks decay by 2 per tick = 400 ticks (20 seconds). Min is 140, so 660 / 2 = 330 ticks (16.5 seconds) of freeze damage
                            }
                        } else {
                            if (player.getFrozenTicks() > 0) {
                                player.setFrozenTicks(0);
                            }
                        }
                    }
'''

code = code.replace('''                    if (pState.getFlag("ch04_snow_active")) {
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
                    }''', replacement.strip())

with open('src/main/java/com/carlo/story/Chapter04.java', 'w', encoding='utf-8') as f:
    f.write(code)
