package com.ithinkrok.minigames.base.util.playerstate;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;

/**
 * Created by paul on 31/12/15.
 */
@SuppressWarnings("NewExceptionWithoutArguments")
public class InventoryCapture implements PlayerInventory {

    private static final ArmorCapture EMPTY_ARMOR = new ArmorCapture(new ItemStack[4]);

    private final PlayerState playerState;
    private ItemStack[] contents;

    public InventoryCapture(PlayerState playerState, ItemStack[] contents) {
        this.playerState = playerState;

        this.contents = contents;
    }

    @Override
    public ItemStack[] getArmorContents() {
        return equipment().getArmorContents();
    }

    private EntityEquipment equipment() {
        if (playerState == null) return EMPTY_ARMOR;
        if (playerState.getPlaceholder() != null) return playerState.getPlaceholder().getEquipment();
        if (playerState.getEquipment() != null) return playerState.getEquipment();

        return EMPTY_ARMOR;
    }

    @Override
    public ItemStack getHelmet() {
        return equipment().getHelmet();
    }

    @Override
    public ItemStack getChestplate() {
        return equipment().getChestplate();
    }

    @Override
    public ItemStack getLeggings() {
        return equipment().getLeggings();
    }

    @Override
    public ItemStack getBoots() {
        return equipment().getBoots();
    }

    @Override
    public void setItem(int index, ItemStack item) {
        if (item != null && (item.getType() == Material.AIR || item.getAmount() == 0)) item = null;
        contents[index] = item;
    }

    @Override
    public void setBoots(ItemStack boots) {
        equipment().setBoots(boots);
    }

    @Override
    public ItemStack getItemInMainHand() {
        return equipment().getItemInMainHand();
    }

    @Override
    public void setItemInMainHand(ItemStack item) {
        equipment().setItemInOffHand(item);
    }

    @Override
    public ItemStack getItemInOffHand() {
        return equipment().getItemInOffHand();
    }

    @Override
    public void setItemInOffHand(ItemStack item) {
        equipment().setItemInOffHand(item);
    }

    @Override
    public ItemStack getItemInHand() {
        return equipment().getItemInHand();
    }

    @Override
    public void setItemInHand(ItemStack stack) {
        equipment().setItemInHand(stack);
    }

    @Override
    public int getHeldItemSlot() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setHeldItemSlot(int slot) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int clear(int id, int data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HumanEntity getHolder() {
        return null;
    }

    @Override
    public void setLeggings(ItemStack leggings) {
        equipment().setLeggings(leggings);
    }

    @Override
    public void setChestplate(ItemStack chestplate) {
        equipment().setChestplate(chestplate);
    }

    @Override
    public void setHelmet(ItemStack helmet) {
        equipment().setHelmet(helmet);
    }

    @Override
    public void setArmorContents(ItemStack[] items) {
        equipment().setArmorContents(items);
    }

    @Override
    public int getSize() {
        return contents.length;
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public void setMaxStackSize(int size) {

    }

    @Override
    public String getName() {
        return "zombie";
    }

    @Override
    public ItemStack getItem(int index) {
        return contents[index];
    }

    @Override
    public HashMap<Integer, ItemStack> addItem(ItemStack... items) throws IllegalArgumentException {
        HashMap<Integer, ItemStack> result = new HashMap<>();

        for (int index = 0; index < items.length; ++index) {
            ItemStack copy = items[index].clone();
            for (int i = 0; i < contents.length; ++i) {
                if (copy == null) break;

                ItemStack at = contents[i];

                if (at == null) contents[i] = copy;
                else if (at.isSimilar(copy)) {
                    int change = Math.min(copy.getAmount(), Math.max(at.getMaxStackSize() - at.getAmount(), 0));

                    at.setAmount(at.getAmount() + change);
                    copy.setAmount(copy.getAmount() - change);
                    if (copy.getAmount() == 0) copy = null;
                }
            }

            if (copy != null) result.put(index, copy);
        }

        return result;
    }

    @Override
    public HashMap<Integer, ItemStack> removeItem(ItemStack... items) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ItemStack[] getContents() {
        return contents;
    }

    @Override
    public void setContents(ItemStack[] items) throws IllegalArgumentException {
        this.contents = items;
    }

    @Override
    public boolean contains(int materialId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Material material) throws IllegalArgumentException {
        return first(material) != -1;
    }

    @Override
    public boolean contains(ItemStack item) {
        return first(item) != -1;
    }

    @Override
    public boolean contains(int materialId, int amount) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Material material, int amount) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(ItemStack item, int amount) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAtLeast(ItemStack item, int amount) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HashMap<Integer, ? extends ItemStack> all(int materialId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HashMap<Integer, ? extends ItemStack> all(Material material) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public HashMap<Integer, ? extends ItemStack> all(ItemStack item) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int first(int materialId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int first(Material material) throws IllegalArgumentException {
        for (int i = 0; i < contents.length; ++i) {
            if ((material == null || material == Material.AIR) && contents[i] == null) return i;
            else if (contents[i] != null && contents[i].getType() == material) return i;
        }

        return -1;
    }

    @Override
    public int first(ItemStack item) {
        for (int i = 0; i < contents.length; ++i) {
            if (contents[i] != null && contents[i].equals(item)) return i;
        }

        return -1;
    }

    @Override
    public int firstEmpty() {
        return first(Material.AIR);
    }

    @Override
    public void remove(int materialId) {
        throw new UnsupportedOperationException("Removing by materialId is unsupported");
    }

    @Override
    public void remove(Material material) throws IllegalArgumentException {
        int first;

        while ((first = first(material)) != -1) {
            setItem(first, null);
        }
    }

    @Override
    public void remove(ItemStack item) {
        int first;

        while ((first = first(item)) != -1) {
            setItem(first, null);
        }
    }

    @Override
    public void clear(int index) {
        setItem(index, null);
    }

    @Override
    public void clear() {
        contents = new ItemStack[contents.length];
    }

    @Override
    public List<HumanEntity> getViewers() {
        return new ArrayList<>();
    }

    @Override
    public String getTitle() {
        return getName();
    }

    @Override
    public InventoryType getType() {
        return InventoryType.PLAYER;
    }

    @Override
    public ListIterator<ItemStack> iterator() {
        List<ItemStack> list = Arrays.asList(contents);

        return list.listIterator();
    }

    @Override
    public ListIterator<ItemStack> iterator(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Location getLocation() {
        return null;
    }
}
