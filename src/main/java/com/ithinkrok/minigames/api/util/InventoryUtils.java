package com.ithinkrok.minigames.api.util;

import com.ithinkrok.util.StringUtils;
import net.milkbowl.vault.item.Items;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.NBTTagList;
import net.minecraft.server.v1_12_R1.NBTTagString;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.*;

import java.util.*;

/**
 * Created by paul on 31/12/15.
 */
public class InventoryUtils {

    private static final String DEFAULT_LORE_STYLE = ChatColor.DARK_PURPLE.toString() + ChatColor.ITALIC.toString();

    public static boolean isMaterial(ItemStack stack, Material material) {
        return stack != null && stack.getType() == material;
    }

    public static ItemStack createLeatherArmorItem(Material material, Color armorColor) {
        return setLeatherArmorColor(new ItemStack(material), armorColor);
    }

    public static ItemStack setLeatherArmorColor(ItemStack armor, Color armorColor) {
        LeatherArmorMeta meta = (LeatherArmorMeta) armor.getItemMeta();

        meta.setColor(armorColor);
        armor.setItemMeta(meta);
        return armor;
    }

    public static ItemStack createLeatherArmorItem(Material material, Color armorColor, boolean unbreakable) {
        return setUnbreakable(setLeatherArmorColor(new ItemStack(material), armorColor), unbreakable);
    }

    public static ItemStack setUnbreakable(ItemStack itemStack, boolean unbreakable) {
        ItemMeta meta = itemStack.getItemMeta();

        meta.setUnbreakable(unbreakable);
        itemStack.setItemMeta(meta);

        return itemStack;
    }


    public static ItemStack parseItem(String itemString) {
        String baseString = itemString;
        String extendedString = "";

        int colonIndex = itemString.indexOf(':');
        if(colonIndex >= 0) {
            baseString = itemString.substring(0, colonIndex);
            extendedString = itemString.substring(colonIndex + 1, itemString.length());
        }

        ItemStack result = parseBaseItemString(baseString);

        if(!extendedString.isEmpty()) {
            applyExtendedItemString(extendedString, result);
        }

        return result;
    }

    private static void applyExtendedItemString(String extendedString, ItemStack item) {
        String[] parts = extendedString.trim().split(",");

        switch(item.getType()) {
            case POTION:
            case SPLASH_POTION:
            case LINGERING_POTION:
            case TIPPED_ARROW:
                String effectName = parts[0].toUpperCase();

                int amp = parts.length > 1 ? Integer.parseInt(parts[1]) - 1 : 0;
                int duration = parts.length > 2 ? (int) (Double.parseDouble(parts[2]) / 20.0) : 3 * 60 * 20;

                PotionEffectType type = PotionEffectType.getByName(effectName);

                PotionMeta meta = (PotionMeta) item.getItemMeta();
                PotionType potionType = PotionType.getByEffect(type);
                if(potionType == null) potionType = PotionType.UNCRAFTABLE;
                meta.setBasePotionData(new PotionData(potionType, false, false));
                meta.addCustomEffect(new PotionEffect(type, duration, amp), true);
                item.setItemMeta(meta);
        }
    }

    private static ItemStack parseBaseItemString(String baseString) {
        String[] parts = baseString.trim().split(",");

        String materialName = parts[0].trim();
        Material material = Material.matchMaterial(materialName);

        if(material == null) throw new IllegalArgumentException("Unknown material: " + materialName);

        int amount = parts.length >= 2 ? Integer.parseInt(parts[1].trim()) : 1;
        int durability = parts.length >= 3 ? Integer.parseInt(parts[2].trim()) : 0;
        String name = parts.length >= 4 ? parts[3] : null;

        if (name != null) name = StringUtils.convertAmpersandToSelectionCharacter(name);

        String[] lore;
        if (parts.length >= 5) lore = Arrays.copyOfRange(parts, 4, parts.length);
        else lore = new String[0];

        for (int index = 0; index < lore.length; ++index) {
            lore[index] = StringUtils.convertAmpersandToSelectionCharacter(lore[index]);
        }

        return createItemWithNameAndLore(material, amount, durability, name, lore);
    }

    public static ItemStack createItemWithNameAndLore(Material mat, int amount, int damage, String name,
                                                      String... lore) {
        ItemStack stack = new ItemStack(mat, amount, (short) damage);

        return setItemNameAndLore(stack, name, lore);
    }

    public static ItemStack setItemNameAndLore(ItemStack item, String name, String... lore) {
        ItemMeta im = item.getItemMeta();
        if (name != null) im.setDisplayName(name);

        if(lore != null && lore.length > 0) {
            im.setLore(Arrays.asList(lore));
        } else {
            im.setLore(null);
        }

        item.setItemMeta(im);

        return item;
    }

    public static String getIdentifier(ItemStack item) {
        if (isEmpty(item)) return null;

        net.minecraft.server.v1_12_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);

        NBTTagCompound tag = nmsItem.getTag();
        if(tag == null) return null;

        return tag.getString("CustomItem");
    }

    public static ItemStack disableBlockPlacing(ItemStack blockItem) {
        net.minecraft.server.v1_12_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(blockItem);
        nmsItem.a("CanPlaceOn", new NBTTagList());

        //This works as the item meta stores its "unhandled" tags including our custom one
        ItemMeta meta = CraftItemStack.asBukkitCopy(nmsItem).getItemMeta();
        blockItem.setItemMeta(meta);
        return blockItem;
    }

    /**
     * @param item The item to get the name for
     * @return The default Minecraft English name of the item
     */
    public static String getItemStackDefaultName(ItemStack item) {
        return Items.itemByStack(item).getName();
    }

    public static ItemStack addIdentifier(ItemStack item, String identifier) {
        net.minecraft.server.v1_12_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        nmsItem.a("CustomItem", new NBTTagString(identifier));

        //This works as the item meta stores its "unhandled" tags including our custom one
        ItemMeta meta = CraftItemStack.asBukkitCopy(nmsItem).getItemMeta();
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isEmpty(ItemStack stack) {
        return stack == null || stack.getType() == Material.AIR || stack.getAmount() == 0;
    }


    public static ItemStack createPotion(PotionType type, int level, boolean splash, boolean extended, int amount) {
        Potion pot = new Potion(type, level);
        pot.setSplash(splash);
        if (extended) pot.setHasExtendedDuration(true);

        return pot.toItemStack(amount);
    }

    public static ItemStack setItemLore(ItemStack item, String... lore) {
        return setItemNameAndLore(item, null, lore);
    }

    public static ItemStack createItemWithEnchantments(Material mat, int amount, int damage, String name, String desc,
                                                       Object... enchantments) {
        ItemStack stack;
        if (desc != null) stack = createItemWithNameAndLore(mat, amount, damage, name, desc);
        else stack = createItemWithNameAndLore(mat, amount, damage, name);

        return enchantItem(stack, enchantments);
    }

    public static ItemStack enchantItem(ItemStack item, Object... enchantments) {
        for (int i = 0; i < enchantments.length; i += 2) {
            int level = (int) enchantments[i + 1];
            if (level == 0) continue;

            item.addUnsafeEnchantment((Enchantment) enchantments[i], level);
        }

        return item;
    }

    public static void replaceItem(Inventory inventory, ItemStack stack) {
        String id = getIdentifier(stack);
        if (id == null) throw new RuntimeException("replaceItem() can only be used on items with identifiers");

        for (int index = 0; index < inventory.getSize(); ++index) {
            if (!Objects.equals(getIdentifier(inventory.getItem(index)), id)) continue;

            inventory.setItem(index, stack);
        }
    }

    public static ItemStack addLore(ItemStack item, String... lore) {
        return addLore(item, Arrays.asList(lore));
    }

    public static ItemStack removeIdentifier(ItemStack item) {
        net.minecraft.server.v1_12_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);

        NBTTagCompound tag = nmsItem.getTag();
        if(tag == null) {
            return item; //No identifier in the first place
        }

        tag.remove("CustomItem");
        nmsItem.setTag(tag);

        ItemMeta meta = CraftItemStack.asBukkitCopy(nmsItem).getItemMeta();
        item.setItemMeta(meta);

        return item;
    }

    public static String getItemName(ItemStack item) {
        ItemMeta meta = item.getItemMeta();

        if (!meta.hasDisplayName()) return null;
        return meta.getDisplayName();
    }

    public static ItemStack setItemName(ItemStack item, String name) {
        ItemMeta im = item.getItemMeta();
        if (name != null) im.setDisplayName(name);
        item.setItemMeta(im);
        return item;
    }

    public static boolean containsIdentifier(Inventory inv, String identifier) {
        for (ItemStack item : inv) {
            if (Objects.equals(getIdentifier(item), identifier)) return true;
        }

        return false;
    }

    public static boolean isArmor(ItemStack item) {
        if (isEmpty(item)) return false;

        String type = item.getType().toString();

        return type.endsWith("HELMET") || type.endsWith("CHESTPLATE") || type.endsWith("LEGGINGS") ||
                type.endsWith("BOOTS");
    }

    public static boolean loreContainsLine(ItemStack item, String line) {
        for (String lore : getLore(item)) {
            if (lore.equals(line)) return true;
        }

        return false;
    }

    public static List<String> getLore(ItemStack item) {
        if (isEmpty(item)) return Collections.emptyList();

        ItemMeta meta = item.getItemMeta();

        if (meta.hasLore()) return meta.getLore();
        else return Collections.emptyList();
    }

    public static ItemStack addLore(ItemStack item, List<String> lore) {
        ItemMeta im = item.getItemMeta();

        List<String> oldLore;
        if (im.hasLore()) oldLore = im.getLore();
        else oldLore = new ArrayList<>();

        oldLore.addAll(lore);

        if (!oldLore.isEmpty()) im.setLore(oldLore);
        item.setItemMeta(im);

        return item;
    }

    public static int getAmountOfItemsWithIdentifier(Inventory inventory, String identifier) {
        int amount = 0;

        for (ItemStack itemStack : inventory.getContents()) {
            if(itemStack != null && Objects.equals(getIdentifier(itemStack), identifier)) {
                amount += itemStack.getAmount();
            }
        }

        return amount;
    }

    public static int removeItemsWithIdentifier(Inventory inventory, String identifier, int max) {
        int actual = 0;

        ItemStack[] contents = inventory.getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack itemStack = contents[i];
            if (itemStack == null || !Objects.equals(getIdentifier(itemStack), identifier)) continue;

            int amountToRemove = Math.min(itemStack.getAmount(), max);
            actual += amountToRemove;
            max -= amountToRemove;

            if (itemStack.getAmount() == amountToRemove) {
                inventory.setItem(i, null);
            } else {
                itemStack.setAmount(itemStack.getAmount() - amountToRemove);
                inventory.setItem(i, itemStack);
            }

            if(max == 0) break;
        }

        return actual;
    }
}
