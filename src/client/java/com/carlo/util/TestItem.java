package com.carlo.util;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
public class TestItem {
    public static void test() {
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, Identifier.of("godeye", "nightfall_staff"));
        Item.Settings settings = new Item.Settings().registryKey(key);
    }
}
