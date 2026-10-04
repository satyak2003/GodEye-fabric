import re

with open('src/main/java/com/carlo/Godeye.java', 'r', encoding='utf-8') as f:
    code = f.read()

# Fix Identifier(...) -> Identifier.of(...)
code = re.sub(r'new Identifier\(([^,]+),\s*"([^"]+)"\)', r'Identifier.of(\1, "\2")', code)

# Fix getServerWorld
code = code.replace('player.getServerWorld()', '((net.minecraft.server.world.ServerWorld)player.getEntityWorld())')

# Fix getWorld
code = code.replace('newPlayer.getWorld()', 'newPlayer.getEntityWorld()')

# Fix getServer
code = code.replace('player.getServer()', '((net.minecraft.server.world.ServerWorld)player.getEntityWorld()).getServer()')

# Fix getSpawnPos
code = code.replace('world.getSpawnPos()', 'new net.minecraft.util.math.BlockPos(0, 100, 0)')

# Fix getOrCreateNbt
code = code.replace('book.getOrCreateNbt()', 'new net.minecraft.nbt.NbtCompound()')

with open('src/main/java/com/carlo/Godeye.java', 'w', encoding='utf-8') as f:
    f.write(code)
