package com.ithinkrok.minigames.util.inventory;

import com.ithinkrok.minigames.api.inventory.ClickableInventory;
import com.ithinkrok.minigames.api.inventory.ClickableItem;
import com.ithinkrok.minigames.api.inventory.event.CalculateItemForUserEvent;
import com.ithinkrok.minigames.api.inventory.event.UserClickItemEvent;
import com.ithinkrok.minigames.api.util.InventoryUtils;
import com.ithinkrok.util.config.Config;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.WeakHashMap;

public class SubInventory extends ClickableItem {

    private static final WeakHashMap<ClickableInventory, ClickableInventory> previousInventories = new WeakHashMap<>();

    private List<Config> items;
    private String sharedObject;
    private boolean haveBackButton;
    private String subInventoryNameLocale;

    private boolean isBackButton;

    public SubInventory(ItemStack baseDisplay, int slot) {
        super(baseDisplay, slot);
    }


    private SubInventory createBackButton(int slot) {
        ItemStack baseDisplay = new ItemStack(Material.ARROW);
        InventoryUtils.setItemName(baseDisplay, "Back");

        SubInventory back = new SubInventory(baseDisplay, slot);
        back.isBackButton = true;

        return back;
    }


    @Override
    public void configure(Config config) {
        super.configure(config);

        if(config.contains("shared_object")) {
            sharedObject = config.getString("shared_object");
        }

        if(config.contains("items")) {
            items = config.getConfigList("items");
        }

        haveBackButton = config.getBoolean("back_button", true);

        subInventoryNameLocale = config.getString("name_locale");
    }


    @Override
    public void onCalculateItem(CalculateItemForUserEvent event) {
        if(isBackButton) return;

        ItemStack display = event.getDisplay();

        InventoryUtils.setItemName(display, event.getUser().getLanguageLookup().getLocale(subInventoryNameLocale));

        event.setDisplay(display);
    }


    @Override
    public void onClick(UserClickItemEvent event) {
        if(isBackButton) {
            ClickableInventory previous = previousInventories.get(event.getInventory());

            if(previous == null) {
                System.out.println("No previous inventory found");
                return;
            }

            event.getUser().showInventory(previous, event.getUser().getInventoryTether());
        } else {
            String name = event.getUser().getLanguageLookup().getLocale(subInventoryNameLocale);
            ClickableInventory inv = new ClickableInventory(name);

            if(sharedObject != null) {
                Config shared = event.getUser().getSharedObjectOrEmpty(sharedObject);
                inv.loadFromConfig(shared.getConfigList("items"));
            }

            if(!items.isEmpty()) {
                inv.loadFromConfig(items);
            }

            if(haveBackButton) {
                previousInventories.put(inv, event.getInventory());

                int slots = inv.getItems().size();
                int maxSlot = inv.getItems().stream().mapToInt(ClickableItem::getSlot).max().getAsInt();

                if(maxSlot > slots) {
                    slots = maxSlot;
                }

                int bottomLeft = (slots / 9) * 9 + 13;

                inv.addItem(createBackButton(bottomLeft));
            }

            event.getUser().showInventory(inv, event.getUser().getInventoryTether());
        }


    }
}
