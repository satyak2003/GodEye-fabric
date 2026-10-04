import re

with open('src/client/java/com/carlo/client/GodeyeClient.java', 'r', encoding='utf-8') as f:
    code = f.read()

code = code.replace('client.world.addParticle(net.minecraft.particle.ParticleTypes.SQUID_INK, eff.x + ox, eff.y + oy, eff.z + oz, -ox*0.1, -oy*0.05, -oz*0.1);', 'client.world.addParticle(net.minecraft.particle.ParticleTypes.SQUID_INK, eff.x + ox, eff.y + oy, eff.z + oz, -ox*0.1, -oy*0.05, -oz*0.1);')

code = code.replace('client.world.addParticle(net.minecraft.particle.ParticleTypes.SQUID_INK', 'client.world.addParticle(net.minecraft.particle.ParticleTypes.SQUID_INK')

# Actually, I can just do a regex replace
code = re.sub(r'client\.world\.addParticle\((net\.minecraft\.particle\.ParticleTypes\.[A-Z_]+), (.*?), (.*?), (.*?), (.*?), (.*?), (.*?)\);', r'client.world.addParticle(\1, \2, \3, \4, \5, \6, \7);', code)

# Let me check the order of parameters again.
# In 1.21.11: client.world.addParticle(ParticleEffect, x, y, z, velocityX, velocityY, velocityZ)
# Wait! I DID pass ParticleTypes.SQUID_INK as the first parameter!
# Why did it fail?
# Because ClientWorld doesn't have it! It's in World!
# client.world is ClientWorld which extends World.
# Let's look at the compilation error carefully.
