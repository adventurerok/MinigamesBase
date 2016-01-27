package com.ithinkrok.minigames.base.command;

import com.ithinkrok.minigames.base.Nameable;
import com.ithinkrok.minigames.base.util.io.ListenerLoader;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by paul on 24/01/16.
 */
public class CommandConfig implements Nameable {

    private final String name;
    private final String formattedName;
    private final String permission;
    private final String othersPermission;

    private final List<String> aliases;

    private final String description, usage;

    private final Listener executor;

    public CommandConfig(String name, ConfigurationSection config, Object creator) {
        this.name = name.toLowerCase();

        if (config.contains("formatted_name")) formattedName = config.getString("formatted_name");
        else formattedName = name;

        this.permission = config.getString("permission");
        if (this.permission == null) throw new RuntimeException("All commands must have permissions");

        this.othersPermission = config.getString("others_permission");

        this.description = config.getString("description", "A minigames provided command");
        this.usage = config.getString("usage", "/<command>");

        List<String> aliases = config.getStringList("aliases");
        if (aliases == null) aliases = new ArrayList<>();
        aliases.add(name);

        this.aliases = aliases;

        Listener executor = null;

        try {
            executor = ListenerLoader.loadListener(creator, this, config);
        } catch (Exception e) {
            System.out.println("Error while loading command listener for command \"" + name + "\"");
            e.printStackTrace();
        }

        this.executor = executor;
    }

    public CommandConfig(String name, String permission, String description, String usage, Listener executor,
                         String othersPermission, String... aliases) {
        this(name, name, permission, othersPermission, description, usage, executor, aliases);
    }

    public CommandConfig(String name, String formattedName, String permission, String othersPermission,
                         String description, String usage, Listener executor, String... aliases) {
        this.name = name;
        this.formattedName = formattedName;
        this.permission = permission;
        this.othersPermission = othersPermission;
        this.aliases = Arrays.asList(aliases);
        this.description = description;
        this.usage = usage;
        this.executor = executor;
    }

    public String getName() {
        return name;
    }

    @Override
    public String getFormattedName() {
        return formattedName;
    }

    public String getPermission() {
        return permission;
    }

    public String getOthersPermission() {
        return othersPermission;
    }

    public boolean hasOthersPermission() {
        return othersPermission != null;
    }

    public Listener getExecutor() {
        return executor;
    }

    public String getDescription() {
        return description;
    }

    public String getUsage() {
        return usage;
    }

    public List<String> getAliases() {
        return aliases;
    }
}
