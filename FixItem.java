import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FixItem {
    public static void main(String[] args) throws Exception {
        String path = "src/main/java/com/carlo/Godeye.java";
        String content = new String(Files.readAllBytes(Paths.get(path)));
        
        String oldLine = "public static final Item NIGHTFALL_STAFF = new com.carlo.item.NightfallStaffItem(new Item.Settings().registryKey(net.minecraft.registry.RegistryKey.of(net.minecraft.registry.RegistryKeys.ITEM, Identifier.of(\"godeye\", \"nightfall_staff\"))).maxCount(1));";
        String newLine = "public static final Item NIGHTFALL_STAFF = new com.carlo.item.NightfallStaffItem(new Item.Settings().registryKey(net.minecraft.registry.RegistryKey.of(net.minecraft.registry.RegistryKeys.ITEM, Identifier.of(\"godeye\", \"nightfall_staff\"))).maxCount(1).rarity(net.minecraft.util.Rarity.EPIC).fireproof());";
        
        if (content.contains(oldLine)) {
            content = content.replace(oldLine, newLine);
            Files.write(Paths.get(path), content.getBytes());
            System.out.println("Replaced successfully!");
        } else {
            System.out.println("Could not find line in Godeye.java!");
        }
    }
}
