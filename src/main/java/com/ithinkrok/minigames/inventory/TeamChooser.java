package com.ithinkrok.minigames.inventory;

import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.event.MinigamesEventHandler;
import com.ithinkrok.minigames.event.user.world.UserInteractEvent;
import com.ithinkrok.minigames.inventory.event.UserClickItemEvent;
import com.ithinkrok.minigames.team.Team;
import com.ithinkrok.minigames.team.TeamIdentifier;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import static com.ithinkrok.minigames.util.InventoryUtils.createItemWithNameAndLore;

/**
 * Created by paul on 07/01/16.
 */
public class TeamChooser implements Listener {

    private List<String> choosable;
    private Material chooserMaterial;
    private String chosenLocale, fullLocale, alreadyLocale, titleLocale;

    @MinigamesEventHandler
    public void onListenerLoaded(ListenerLoadedEvent<?, ?> event) {
        ConfigurationSection config = event.getConfig();

        choosable = config.getStringList("choosable_teams");
        chooserMaterial = Material.matchMaterial(config.getString("chooser_material", "WOOL"));

        chosenLocale = config.getString("chosen_locale", "team_chooser.choose.chosen");
        fullLocale = config.getString("full_locale", "team_chooser.choose.full");
        alreadyLocale = config.getString("already_chosen_locale", "team_chooser.choose.already_chosen");
        titleLocale = config.getString("title_locale", "team_chooser.choose.title");
    }

    @MinigamesEventHandler
    public void onRightClick(UserInteractEvent event) {
        if(event.getInteractType() != UserInteractEvent.InteractType.RIGHT_CLICK) return;
        event.setCancelled(true);

        User user = event.getUser();

        ClickableInventory inventory = new ClickableInventory(user.getLanguageLookup().getLocale(titleLocale));

        for(String teamName : choosable) {
            TeamIdentifier identifier = event.getUserGameGroup().getTeamIdentifier(teamName);
            ItemStack display = createItemWithNameAndLore(chooserMaterial, 1, identifier.getDyeColor().getWoolData(),
                    identifier.getFormattedName());

            ClickableItem item = new ClickableItem(display) {

                @Override
                public void onClick(UserClickItemEvent event) {
                    if(teamName.equals(event.getUser().getTeamName())) {
                        event.getUser().sendLocale(alreadyLocale, identifier.getFormattedName());
                        return;
                    }

                    Team team = event.getUserGameGroup().getTeam(teamName);
                    if(!checkTeamJoinAllowed(event.getUser(), team)){
                        event.getUser().sendLocale(fullLocale, identifier.getFormattedName());
                        return;
                    }

                    user.setTeam(team);
                    user.sendLocale(chosenLocale, identifier.getFormattedName());

                    event.getUser().closeInventory();
                }
            };

            inventory.addItem(item);
        }

        user.showInventory(inventory, null);
    }

    private boolean checkTeamJoinAllowed(User user, Team team) {
        int players = team.getUserCount();

        if(players < (user.getGameGroup().getUserCount() / choosable.size())) return true;

        for(String teamName : choosable) {
            int count = user.getGameGroup().getTeam(teamName).getUserCount();
            if(teamName.equals(user.getTeamName())) --count;

            if(count < players) return false;
        }

        return true;
    }
}
