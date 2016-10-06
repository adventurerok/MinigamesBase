package com.ithinkrok.minigames.api.item.event;

import com.ithinkrok.minigames.api.event.MinigamesEvent;
import com.ithinkrok.minigames.api.item.CustomItem;
import com.ithinkrok.util.math.Variables;
import com.ithinkrok.util.lang.LanguageLookup;

import java.util.List;

/**
 * Created by paul on 03/01/16.
 */
public class CustomItemLoreCalculateEvent implements MinigamesEvent{

    private final CustomItem customItem;
    private final List<String> lore;
    private final LanguageLookup languageLookup;
    private final Variables variables;

    public CustomItemLoreCalculateEvent(CustomItem customItem, List<String> lore, LanguageLookup languageLookup,
                                        Variables variables) {
        this.customItem = customItem;
        this.lore = lore;
        this.languageLookup = languageLookup;
        this.variables = variables;
    }

    public CustomItem getCustomItem() {
        return customItem;
    }

    public List<String> getLore() {
        return lore;
    }

    public LanguageLookup getLanguageLookup() {
        return languageLookup;
    }

    public Variables getVariables() {
        return variables;
    }

}
