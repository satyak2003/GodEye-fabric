import re

with open('src/client/java/com/carlo/client/GodeyeClient.java', 'r', encoding='utf-8') as f:
    code = f.read()

code = re.sub(
    r'\(\(net\.minecraft\.world\.World\)client\.world\)\.addParticle\(([^,]+),\s*([^,]+),\s*([^,]+),\s*([^,]+),\s*([^,]+),\s*([^,]+),\s*([^)]+)\);',
    r'client.world.addParticle(\1, \2, \3, \4, \5, \6, \7);', # revert
    code
)
code = code.replace('client.world.addParticle', 'client.world.addImportantParticle')

with open('src/client/java/com/carlo/client/GodeyeClient.java', 'w', encoding='utf-8') as f:
    f.write(code)
