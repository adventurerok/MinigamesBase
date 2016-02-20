package com.ithinkrok.minigames.api.schematic.event;

import com.ithinkrok.minigames.api.event.MinigamesEvent;
import com.ithinkrok.minigames.api.schematic.PastedSchematic;

/**
 * Created by paul on 07/01/16.
 */
public class SchematicEvent implements MinigamesEvent {

    private final PastedSchematic schematic;

    public SchematicEvent(PastedSchematic schematic) {
        this.schematic = schematic;
    }


    public PastedSchematic getSchematic() {
        return schematic;
    }
}
