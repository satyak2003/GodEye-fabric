import re

with open('src/main/java/com/carlo/Godeye.java', 'r', encoding='utf-8') as f:
    code = f.read()

correct_tick = '''
    private void onServerTick(net.minecraft.server.MinecraftServer server) {
        com.carlo.story.ChapterManager.tick(server);
        com.carlo.story.event.EventScheduler.tick(server);
    }
}
'''
code = re.sub(r'private void onServerTick\(net\.minecraft\.server\.MinecraftServer server\) \{[\s\S]*', correct_tick.strip(), code)

with open('src/main/java/com/carlo/Godeye.java', 'w', encoding='utf-8') as f:
    f.write(code)
