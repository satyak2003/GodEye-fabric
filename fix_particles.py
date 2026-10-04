import re

with open('src/client/java/com/carlo/client/GodeyeClient.java', 'r', encoding='utf-8') as f:
    code = f.read()

# Fix the method calls to add 2 boolean parameters
code = re.sub(
    r'client\.world\.addParticle\((net\.minecraft\.particle\.ParticleTypes\.[A-Z_]+),\s*(eff\.x[^,]*),\s*(eff\.y[^,]*),\s*(eff\.z[^,]*),\s*([^,]*),\s*([^,]*),\s*([^)]*)\);',
    r'client.world.addParticle(\1, true, true, \2, \3, \4, \5, \6, \7);',
    code
)

with open('src/client/java/com/carlo/client/GodeyeClient.java', 'w', encoding='utf-8') as f:
    f.write(code)
