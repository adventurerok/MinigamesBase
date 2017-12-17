package com.ithinkrok.minigames.api.item;

import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

public class WeaponStats {

    private static final Map<Material, Double> legacyDamage = new HashMap<>();
    private static final Map<Material, Double> newDamage = new HashMap<>();
    
    private static final Map<Material, Double> speed = new HashMap<>();

    static {
        legacyDamage.put(Material.WOOD_SWORD, 4.0);
        legacyDamage.put(Material.WOOD_AXE, 3.0);
        legacyDamage.put(Material.WOOD_PICKAXE, 2.0);
        legacyDamage.put(Material.WOOD_SPADE, 1.0);
        
        legacyDamage.put(Material.GOLD_SWORD, 4.0);
        legacyDamage.put(Material.GOLD_AXE, 3.0);
        legacyDamage.put(Material.GOLD_PICKAXE, 2.0);
        legacyDamage.put(Material.GOLD_SPADE, 1.0);

        legacyDamage.put(Material.STONE_SWORD, 5.0);
        legacyDamage.put(Material.STONE_AXE, 4.0);
        legacyDamage.put(Material.STONE_PICKAXE, 3.0);
        legacyDamage.put(Material.STONE_SPADE, 2.0);

        legacyDamage.put(Material.IRON_SWORD, 6.0);
        legacyDamage.put(Material.IRON_AXE, 5.0);
        legacyDamage.put(Material.IRON_PICKAXE, 4.0);
        legacyDamage.put(Material.IRON_SPADE, 3.0);

        legacyDamage.put(Material.DIAMOND_SWORD, 7.0);
        legacyDamage.put(Material.DIAMOND_AXE, 6.0);
        legacyDamage.put(Material.DIAMOND_PICKAXE, 5.0);
        legacyDamage.put(Material.DIAMOND_SPADE, 4.0);


        newDamage.put(Material.WOOD_SWORD, 4.0);
        newDamage.put(Material.WOOD_AXE, 7.0);
        newDamage.put(Material.WOOD_PICKAXE, 2.0);
        newDamage.put(Material.WOOD_SPADE, 2.5);

        newDamage.put(Material.GOLD_SWORD, 4.0);
        newDamage.put(Material.GOLD_AXE, 7.0);
        newDamage.put(Material.GOLD_PICKAXE, 2.0);
        newDamage.put(Material.GOLD_SPADE, 2.5);

        newDamage.put(Material.STONE_SWORD, 5.0);
        newDamage.put(Material.STONE_AXE, 9.0);
        newDamage.put(Material.STONE_PICKAXE, 3.0);
        newDamage.put(Material.STONE_SPADE, 3.5);

        newDamage.put(Material.IRON_SWORD, 6.0);
        newDamage.put(Material.IRON_AXE, 9.0);
        newDamage.put(Material.IRON_PICKAXE, 4.0);
        newDamage.put(Material.IRON_SPADE, 4.5);

        newDamage.put(Material.DIAMOND_SWORD, 7.0);
        newDamage.put(Material.DIAMOND_AXE, 9.0);
        newDamage.put(Material.DIAMOND_PICKAXE, 5.0);
        newDamage.put(Material.DIAMOND_SPADE, 5.5);


        double swordSpeed = 1.6;
        speed.put(Material.WOOD_SWORD, swordSpeed);
        speed.put(Material.GOLD_SWORD, swordSpeed);
        speed.put(Material.STONE_SWORD, swordSpeed);
        speed.put(Material.IRON_SWORD, swordSpeed);
        speed.put(Material.DIAMOND_SWORD, swordSpeed);

        speed.put(Material.WOOD_AXE, 0.8);
        speed.put(Material.GOLD_AXE, 1.0);
        speed.put(Material.STONE_AXE, 0.8);
        speed.put(Material.IRON_AXE, 0.9);
        speed.put(Material.DIAMOND_AXE, 1.0);

        double pickaxeSpeed = 1.2;
        speed.put(Material.WOOD_PICKAXE, pickaxeSpeed);
        speed.put(Material.GOLD_PICKAXE, pickaxeSpeed);
        speed.put(Material.STONE_PICKAXE, pickaxeSpeed);
        speed.put(Material.IRON_PICKAXE, pickaxeSpeed);
        speed.put(Material.DIAMOND_PICKAXE, pickaxeSpeed);

        double spadeSpeed = 1.0;
        speed.put(Material.WOOD_SPADE, spadeSpeed);
        speed.put(Material.GOLD_SPADE, spadeSpeed);
        speed.put(Material.STONE_SPADE, spadeSpeed);
        speed.put(Material.IRON_SPADE, spadeSpeed);
        speed.put(Material.DIAMOND_SPADE, spadeSpeed);

        speed.put(Material.WOOD_HOE, 1.0);
        speed.put(Material.GOLD_HOE, 1.0);
        speed.put(Material.STONE_HOE, 2.0);
        speed.put(Material.IRON_HOE, 3.0);
        speed.put(Material.DIAMOND_HOE, 4.0);
    }

}
