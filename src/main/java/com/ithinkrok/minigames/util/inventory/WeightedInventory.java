package com.ithinkrok.minigames.util.inventory;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.item.CustomItem;
import com.ithinkrok.minigames.api.util.MinigamesConfigs;
import com.ithinkrok.util.math.MapVariables;
import com.ithinkrok.util.math.Variables;
import com.ithinkrok.util.config.Config;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Created by paul on 03/03/16.
 */
public class WeightedInventory {

    private static final Random random = new Random();

    /**
     * The chance that a particular slot will have an item
     */
    private final double baseChance;

    private final List<WeightedItem> items = new ArrayList<>();

    private final double totalWeight;

    public WeightedInventory(GameGroup gameGroup, Config config) {
        baseChance = config.getDouble("base_chance");

        List<Config> itemConfigs = config.getConfigList("items");

        double totalWeight = 0;

        for(Config itemConfig : itemConfigs) {
            WeightedItem item = new WeightedItem(gameGroup, itemConfig);

            items.add(item);
            totalWeight += item.weight;
        }

        this.totalWeight = totalWeight;
    }

    private WeightedInventory(double baseChance, Collection<WeightedItem> weightedItems) {
        this.baseChance = baseChance;

        items.addAll(weightedItems);

        double totalWeight = 0;

        for(WeightedItem item : items) {
            totalWeight += item.weight;
        }

        this.totalWeight = totalWeight;
    }

    public void populateInventory(Inventory inventory) {
        for(int index = 0; index < inventory.getSize(); ++index) {
            //Skip if we do not pass the base chance test
            if(random.nextDouble() > baseChance) continue;

            ItemStack randomStack = getRandomStack();
            inventory.setItem(index, randomStack);
        }
    }

    private ItemStack getRandomStack() {
        double weightIndex = random.nextDouble() * totalWeight;

        WeightedItem selected = null;

        for(WeightedItem item : items) {
            weightIndex -= item.weight;

            if(weightIndex > 0) continue;

            selected = item;
            break;
        }

        if(selected == null) selected = items.get(items.size() - 1);

        return selected.createRandomStack();
    }

    public List<ItemStack> generateStacks(int count, boolean ignoreBaseChance) {
        List<ItemStack> result = new ArrayList<>();

        for(int index = 0; index < count; ++index) {

            if(!ignoreBaseChance && random.nextDouble() > baseChance) continue;

            ItemStack randomStack = getRandomStack();
            result.add(randomStack);
        }

        return result;
    }

    /**
     *
     * @param multiply The amount to increase each weight away from the average
     * @return A new WeightedInventory with a difference in min and max weight equal to this ones times the multiply
     */
    public WeightedInventory adjustBalance(double multiply) {
        return adjust(baseChance, multiply, 0);
    }


    public WeightedInventory adjust(double baseChance, double weightMultiply, double extraMod) {
        double averageWeight = totalWeight / items.size();

        List<WeightedItem> newItems = new ArrayList<>();

        for(WeightedItem item : items) {
            double differ = item.weight - averageWeight;

            differ *= weightMultiply;

            WeightedItem copy = new WeightedItem(item);
            copy.weight = averageWeight + differ;

            if(extraMod > 0) {
                copy.extraChance += (1 - copy.extraChance) * extraMod;
            } else {
                copy.extraChance *= -extraMod;
            }

            newItems.add(copy);
        }

        return new WeightedInventory(baseChance, newItems);
    }

    public WeightedInventory adjustBaseChance(double baseChance) {
        return new WeightedInventory(baseChance, items);
    }

    private static class WeightedItem implements Cloneable {
        ItemStack item;
        double weight;
        int min;
        int max;

        /**
         * The chance of adding another item to the stack (up to a maximum of {@code max} items)
         */
        double extraChance;

        /**
         * If extraChance fails, and retryChance succeeds, we try again.
         */
        double retryChance;

        public WeightedItem(GameGroup gameGroup, Config config) {
            weight = config.getDouble("weight");
            min = config.getInt("min", 1);
            max = config.getInt("max", 1);

            //Roughly 1/2 of the items average. Add 1 to prevent division by 1, and to prevent the power being
            // greater than one
            extraChance = config.getDouble("extra_chance", Math.pow(0.5, 1 / (max - min + 1)));

            retryChance = config.getDouble("retry_chance", 0);

            if (config.contains("item")) {
                item = MinigamesConfigs.getItemStack(config, "item");
            } else if(config.contains("custom_item")) {
                String customName = config.getString("custom_item");

                CustomItem customItem = gameGroup.getCustomItem(customName);

                Config variableConfig = config.getConfigOrEmpty("variables");

                Variables variables = new MapVariables(variableConfig);

                item = customItem.createWithVariables(gameGroup.getLanguageLookup(), variables);
            }
        }

        public WeightedItem(WeightedItem copy) {
            weight = copy.weight;
            min = copy.min;
            max = copy.max;

            extraChance = copy.extraChance;
            retryChance = copy.retryChance;

            item = copy.item.clone();
        }

        public ItemStack createRandomStack() {
            int amount = min;

            int tries = max - min;

            for(int attempt = 0; attempt < tries; ++attempt) {
                if(random.nextDouble() < extraChance) {
                    ++amount;
                } else if(random.nextDouble() > retryChance) {
                    break;
                }
            }

            ItemStack item = this.item.clone();
            item.setAmount(amount);

            return item;
        }
    }
}
