package com.ithinkrok.minigames.base.item.event;

import com.ithinkrok.minigames.base.event.MinigamesEvent;
import com.ithinkrok.minigames.base.item.CustomItem;
import com.ithinkrok.minigames.base.lang.LanguageLookup;
import com.ithinkrok.minigames.base.util.math.Variables;

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
