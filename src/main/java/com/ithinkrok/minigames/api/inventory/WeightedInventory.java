package com.ithinkrok.minigames.api.inventory;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.item.CustomItem;
import com.ithinkrok.minigames.api.util.MinigamesConfigs;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.math.*;
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

    private static final Variables nullVariables = new SingleValueVariables(0);

    private static final Random random = new Random();

    /**
     * The chance that a particular slot will have an item
     */
    private final Calculator baseChance;

    private final Calculator weightMultiplier;

    private final Calculator extraMod;

    private final List<WeightedItem> items = new ArrayList<>();

    public WeightedInventory(GameGroup gameGroup, Config config) {
        baseChance = new ExpressionCalculator(config.getString("base_chance"));

        List<Config> itemConfigs = config.getConfigList("items");

        for (Config itemConfig : itemConfigs) {
            try {
                WeightedItem item = new WeightedItem(gameGroup, itemConfig);

                items.add(item);
            } catch (Exception e) {
                System.err.println("Error while creating WeightedItem for WeightedInventory with base chance " +
                                           config.getString("base_chance"));

                e.printStackTrace();
            }
        }

        this.weightMultiplier = new ExpressionCalculator(config.getString("adjust_balance", "1"));
        this.extraMod = new ExpressionCalculator(config.getString("extra_mod", "0"));
    }

    private double calculateTotalWeight(Variables variables) {
        double totalWeight = 0;

        for (WeightedItem item : items) {
            totalWeight += item.weight.calculate(variables);
        }

        return totalWeight;
    }

    private WeightedInventory(Calculator baseChance, Calculator weightMultiplier, Calculator extraMod,
                              Collection<WeightedItem> weightedItems) {
        this.baseChance = baseChance;
        this.weightMultiplier = weightMultiplier;
        this.extraMod = extraMod;

        items.addAll(weightedItems);
    }

    public void populateInventory(Inventory inventory) {
        populateInventory(inventory, nullVariables);
    }

    public void populateInventory(Inventory inventory, Variables variables) {
        double baseChance = this.baseChance.calculate(variables);
        double totalWeight = calculateTotalWeight(variables);

        for (int index = 0; index < inventory.getSize(); ++index) {
            //Skip if we do not pass the base chance test
            if (random.nextDouble() > baseChance) continue;

            ItemStack randomStack = getRandomStack(totalWeight, variables);
            inventory.setItem(index, randomStack);
        }
    }

    private ItemStack getRandomStack(double totalWeight, Variables variables) {
        double weightIndex = random.nextDouble() * totalWeight;

        double averageWeight = totalWeight / items.size();

        double weightDeviator = this.weightMultiplier.calculate(variables);

        WeightedItem selected = null;

        for (WeightedItem item : items) {
            double weight = item.weight.calculate(variables);
            weight = averageWeight + (weight - averageWeight) * weightDeviator;

            weightIndex -= weight;

            if (weightIndex > 0) continue;

            selected = item;
            break;
        }

        if (selected == null) selected = items.get(items.size() - 1);

        double extraMod = this.extraMod.calculate(variables);

        return selected.createRandomStack(variables, extraMod);
    }

    public List<ItemStack> generateStacks(int count, boolean ignoreBaseChance, Variables variables) {
        double baseChance = this.baseChance.calculate(variables);
        double totalWeight = calculateTotalWeight(variables);

        List<ItemStack> result = new ArrayList<>();

        for (int index = 0; index < count; ++index) {

            if (!ignoreBaseChance && random.nextDouble() > baseChance) continue;

            ItemStack randomStack = getRandomStack(totalWeight, variables);
            result.add(randomStack);
        }

        return result;
    }

    public WeightedInventory adjust(Calculator baseChance, Calculator weightMultiply, Calculator extraMod) {
        return new WeightedInventory(baseChance, weightMultiply, extraMod, items);
    }

    private static class WeightedItem implements Cloneable {

        ItemStack item;
        Calculator weight;
        Calculator min;
        Calculator max;

        /**
         * The chance of adding another item to the stack (up to a maximum of {@code max} items)
         */
        Calculator extraChance;

        /**
         * If extraChance fails, and retryChance succeeds, we try again.
         */
        Calculator retryChance;

        public WeightedItem(GameGroup gameGroup, Config config) {
            weight = new ExpressionCalculator(config.getString("weight"));
            min = new ExpressionCalculator(config.getString("min", "1"));
            max = new ExpressionCalculator(config.getString("max", "1"));

            //Roughly 1/2 of the items average. Add 1 to prevent division by 1, and to prevent the power being
            // greater than one
            double defaultExtraChance =
                    Math.pow(0.5, 1 / (max.calculate(nullVariables) - min.calculate(nullVariables) + 1));

            extraChance =
                    new ExpressionCalculator(config.getString("extra_chance", Double.toString(defaultExtraChance)));

            retryChance = new ExpressionCalculator(config.getString("retry_chance", "0"));

            if (config.contains("item")) {
                item = MinigamesConfigs.getItemStack(config, "item");
            } else if (config.contains("custom_item")) {
                String customName = config.getString("custom_item");

                CustomItem customItem = gameGroup.getCustomItem(customName);

                if(customItem == null) {
                    throw new IllegalArgumentException("No custom item with name " + customName);
                }

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

        public ItemStack createRandomStack(Variables variables, double extraMod) {
            int amount = (int) min.calculate(variables);

            int tries = (int) (max.calculate(variables) - amount);

            double extraChance = this.extraChance.calculate(variables);

            if (extraMod > 0) {
                extraChance += (1 - extraChance) * extraMod;
            } else {
                extraChance *= (1 + extraMod);
            }

            double retryChance = this.retryChance.calculate(variables);

            for (int attempt = 0; attempt < tries; ++attempt) {
                if (random.nextDouble() < extraChance) {
                    ++amount;
                } else if (random.nextDouble() > retryChance) {
                    break;
                }
            }

            ItemStack item = this.item.clone();
            item.setAmount(amount);

            return item;
        }
    }
}
