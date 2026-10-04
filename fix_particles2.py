import re

with open('src/client/java/com/carlo/client/GodeyeClient.java', 'r', encoding='utf-8') as f:
    code = f.read()

# Revert to original
code = re.sub(
    r'client\.world\.addParticle\(([^,]+),\s*true,\s*true,\s*([^,]+),\s*([^,]+),\s*([^,]+),\s*([^,]+),\s*([^,]+),\s*([^)]+)\);',
    r'((net.minecraft.world.World)client.world).addParticle(\1, \2, \3, \4, \5, \6, \7);',
    code
)

with open('src/client/java/com/carlo/client/GodeyeClient.java', 'w', encoding='utf-8') as f:
    f.write(code)
