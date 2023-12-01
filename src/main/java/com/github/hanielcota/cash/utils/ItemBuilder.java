package com.github.hanielcota.cash.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class ItemBuilder {

    private final ItemStack itemStack;
    private final Map<String, ItemMeta> cachedSkulls = new HashMap<>();
    private final Map<String, GameProfile> cachedProfiles = new HashMap<>();

    public ItemBuilder(Material material) {
        this(material, 1);
    }

    public ItemBuilder(String materialName) {
        if (materialName == null || materialName.trim().isEmpty()) {
            itemStack = new ItemStack(Material.AIR, 1);
            return;
        }

        boolean isSkull = materialName.startsWith("eyJ0");
        Material material = Material.getMaterial(materialName.toUpperCase());

        if (isSkull) {
            itemStack = new ItemStack(Material.PLAYER_HEAD, 1);
            itemStack.setDurability((short) 3);
            setSkull(materialName);
            return;
        }

        itemStack = new ItemStack(Objects.requireNonNullElse(material, Material.AIR), 1);
    }

    public ItemBuilder(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public ItemBuilder(Material material, int quantity) {
        itemStack = new ItemStack(material, quantity);
    }

    public ItemBuilder(Material material, int quantity, byte durability) {
        itemStack = new ItemStack(material, quantity, durability);
    }

    public ItemBuilder(ItemBuilder other) {
        this.itemStack = other.itemStack.clone();
    }

    public ItemBuilder setDurability(short durability) {
        itemStack.setDurability(durability);
        return this;
    }

    public ItemBuilder setQuantity(int quantity) {
        itemStack.setAmount(quantity);
        return this;
    }

    public ItemBuilder setPotion(PotionEffectType type, int duration, int amplifier) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (!(itemMeta instanceof PotionMeta potionMeta)) {
            return this;
        }

        potionMeta.addCustomEffect(new PotionEffect(type, duration, amplifier), true);
        itemStack.setItemMeta(potionMeta);
        return this;
    }

    public ItemBuilder setName(String name) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName(name);
            itemStack.setItemMeta(itemMeta);
        }
        return this;
    }

    public ItemBuilder modifyName(String text, String replace) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return this;
        }

        String displayName = itemMeta.getDisplayName();
        displayName = displayName.replace(text, replace);

        itemMeta.setDisplayName(displayName);
        itemStack.setItemMeta(itemMeta);

        return this;
    }

    public ItemBuilder addUnsafeEnchantment(Enchantment enchantment, int level) {
        itemStack.addUnsafeEnchantment(enchantment, level);
        return this;
    }

    public ItemBuilder removeEnchantment(Enchantment enchantment) {
        itemStack.removeEnchantment(enchantment);
        return this;
    }

    public ItemBuilder setSkullOwner(String owner) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (!(itemMeta instanceof SkullMeta skullMeta)) {
            return this;
        }

        skullMeta.setOwner(owner);
        itemStack.setItemMeta(skullMeta);
        itemStack.setDurability((short) 3);

        return this;
    }

    public ItemBuilder addEnchant(Enchantment enchantment, int level) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            itemMeta.addEnchant(enchantment, level, true);
            itemStack.setItemMeta(itemMeta);
        }
        return this;
    }

    public ItemBuilder setAmount(int amount) {
        itemStack.setAmount(amount);
        return this;
    }

    public ItemBuilder addEnchantments(Map<Enchantment, Integer> enchantments) {
        itemStack.addEnchantments(enchantments);
        return this;
    }

    public ItemBuilder setInfinityDurability() {
        itemStack.setDurability(Short.MAX_VALUE);
        return this;
    }

    public ItemBuilder addItemFlag(ItemFlag flag) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            itemMeta.addItemFlags(flag);
            itemStack.setItemMeta(itemMeta);
        }
        return this;
    }

    public ItemBuilder setLore(String... lore) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setLore(ImmutableList.copyOf(lore));
            itemStack.setItemMeta(itemMeta);
        }
        return this;
    }

    public ItemBuilder replaceLore(String key, String value) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        List<String> lore = itemMeta != null ? itemMeta.getLore() : null;
        if (lore != null) {
            lore = lore.stream()
                    .map(line -> line != null ? line.replace(key, value) : line)
                    .collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableList::copyOf));

            itemMeta.setLore(lore);
            itemStack.setItemMeta(itemMeta);
        }
        return this;
    }

    public ItemBuilder setLore(List<String> lore) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setLore(ImmutableList.copyOf(lore));
            itemStack.setItemMeta(itemMeta);
        }
        return this;
    }

    public ItemBuilder setLoreIf(boolean condition, String... lore) {
        if (!condition) return this;
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setLore(ImmutableList.copyOf(lore));
            itemStack.setItemMeta(itemMeta);
        }
        return this;
    }

    public ItemBuilder setLoreIf(boolean condition, List<String> lore) {
        if (!condition) return this;
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setLore(ImmutableList.copyOf(lore));
            itemStack.setItemMeta(itemMeta);
        }
        return this;
    }

    public ItemBuilder removeLoreLine(String line) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        List<String> lore = itemMeta != null ? itemMeta.getLore() : null;
        if (lore != null) {
            lore = new ArrayList<>(lore);
            lore.remove(line);
            itemMeta.setLore(ImmutableList.copyOf(lore));
            itemStack.setItemMeta(itemMeta);
        }
        return this;
    }

    public ItemBuilder removeLoreLine(int index) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        List<String> lore = itemMeta != null ? itemMeta.getLore() : null;
        if (lore != null && index >= 0 && index < lore.size()) {
            lore = new ArrayList<>(lore);
            lore.remove(index);
            itemMeta.setLore(ImmutableList.copyOf(lore));
            itemStack.setItemMeta(itemMeta);
        }
        return this;
    }

    public ItemBuilder addLoreIf(boolean condition, String string) {
        if (!condition) return this;

        ItemMeta itemMeta = itemStack.getItemMeta();

        // Create a new ItemMeta if it is null
        if (itemMeta == null) {
            itemMeta = Bukkit.getItemFactory().getItemMeta(itemStack.getType());
        }

        List<String> lore = itemMeta.getLore();
        if (lore == null) {
            lore = Lists.newArrayList();
        }
        lore.add(string);
        itemMeta.setLore(ImmutableList.copyOf(lore));
        itemStack.setItemMeta(itemMeta);
        return this;
    }

    public ItemBuilder addLoreLine(String string) {
        ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta == null) {
            itemMeta = Bukkit.getItemFactory().getItemMeta(itemStack.getType());
        }

        List<String> lore = itemMeta.getLore();
        if (lore == null) {
            lore = Lists.newArrayList();
        }
        lore.add(string);
        itemMeta.setLore(ImmutableList.copyOf(lore));
        itemStack.setItemMeta(itemMeta);
        return this;
    }

    public ItemBuilder addLoreLine(int pos, String string) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        List<String> lore = itemMeta != null ? itemMeta.getLore() : null;
        if (lore != null && pos >= 0 && pos < lore.size()) {
            lore = new ArrayList<>(lore);
            lore.set(pos, string);
            itemMeta.setLore(ImmutableList.copyOf(lore));
            itemStack.setItemMeta(itemMeta);
        }
        return this;
    }

    public ItemBuilder setDyeColor(DyeColor color) {
        itemStack.setDurability(color.getDyeData());
        return this;
    }

    public ItemBuilder setWoolColor(DyeColor color) {
        if (!itemStack.getType().toString().contains("WOOL")) return this;
        itemStack.setDurability(color.getWoolData());
        return this;
    }

    public ItemBuilder setLeatherArmorColor(Color color) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (!(itemMeta instanceof LeatherArmorMeta leatherArmorMeta)) {
            return this;
        }

        leatherArmorMeta.setColor(color);
        itemStack.setItemMeta(leatherArmorMeta);
        return this;
    }

    public ItemBuilder setSkull(String url) {
        if (cachedSkulls.containsKey(url)) {
            itemStack.setItemMeta(cachedSkulls.get(url));
            return this;
        }

        SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();

        if (skullMeta == null) {
            return this;
        }

        GameProfile profile = cachedProfiles.computeIfAbsent(url, key -> createGameProfile());
        profile.getProperties().put("textures", new Property("textures", url));

        try {
            Method setProfileMethod = skullMeta.getClass().getDeclaredMethod("setProfile", GameProfile.class);
            setProfileMethod.setAccessible(true);
            setProfileMethod.invoke(skullMeta, profile);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            return this;
        }

        itemStack.setItemMeta(skullMeta);
        cachedSkulls.put(url, skullMeta);
        return this;
    }

    private GameProfile createGameProfile() {
        return new GameProfile(UUID.randomUUID(), "");
    }

    public ItemStack build() {
        return itemStack;
    }

    public ItemBuilder removeLastLoreLine() {
        ItemMeta itemMeta = itemStack.getItemMeta();
        List<String> lore = itemMeta != null ? itemMeta.getLore() : null;
        if (lore != null && !lore.isEmpty()) {
            lore = new ArrayList<>(lore);
            lore.remove(lore.size() - 1);
            itemMeta.setLore(ImmutableList.copyOf(lore));
            itemStack.setItemMeta(itemMeta);
        }
        return this;
    }
}
