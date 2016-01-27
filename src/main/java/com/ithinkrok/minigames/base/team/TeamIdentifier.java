package com.ithinkrok.minigames.base.team;

import com.ithinkrok.minigames.base.util.DyeToChatColorConverter;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;

/**
 * Created by paul on 06/01/16.
 */
public class TeamIdentifier {


    private final String name;
    private final String formattedName;

    private final Color armorColor;
    private final DyeColor dyeColor;
    private final ChatColor chatColor;

    public TeamIdentifier(String name, DyeColor dyeColor) {
        this(name, null, dyeColor);
    }

    public TeamIdentifier(String name, String formattedName, DyeColor dyeColor) {
        this(name, formattedName, dyeColor, null, null);
    }

    public TeamIdentifier(String name, String formattedName, DyeColor dyeColor, Color armorColor, ChatColor chatColor) {
        Validate.notNull(name, "name cannot be null");
        Validate.notNull(dyeColor, "dyeColor cannot be null");

        this.name = name;

        this.dyeColor = dyeColor;
        this.armorColor = (armorColor != null ? armorColor : dyeColor.getColor());
        this.chatColor = (chatColor != null ? chatColor : DyeToChatColorConverter.convert(dyeColor));

        this.formattedName = (formattedName != null ? formattedName : this.chatColor + WordUtils.capitalizeFully(name));
    }

    public ChatColor getChatColor() {
        return chatColor;
    }

    public Color getArmorColor() {
        return armorColor;
    }

    public String getFormattedName() {
        return formattedName;
    }

    public String getName() {
        return name;
    }

    public DyeColor getDyeColor() {
        return dyeColor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TeamIdentifier that = (TeamIdentifier) o;

        return name.equals(that.name);

    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return getName();
    }
}
