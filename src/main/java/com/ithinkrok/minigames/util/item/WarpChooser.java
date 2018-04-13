package com.ithinkrok.minigames.util.item;

import com.ithinkrok.minigames.api.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.api.event.user.world.UserInteractEvent;
import com.ithinkrok.minigames.util.inventory.WarpInventory;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;
import com.ithinkrok.util.lang.LanguageLookup;

import java.util.ArrayList;
import java.util.List;

public class WarpChooser implements CustomListener {

    String titleLocale;
    List<Config> warps = new ArrayList<>();

    @CustomEventHandler
    public void onListenerLoaded(ListenerLoadedEvent<?,?> event) {
        Config config = event.getConfigOrEmpty();

        warps = config.getConfigList("warps");

        titleLocale = config.getString("title_locale", "warp_chooser.title");
    }


    @CustomEventHandler
    public void onRightClick(UserInteractEvent event) {
        LanguageLookup lang = event.getUser().getLanguageLookup();

        WarpInventory.showToUser(lang.getLocale(titleLocale), warps, event.getUser());
    }

}
