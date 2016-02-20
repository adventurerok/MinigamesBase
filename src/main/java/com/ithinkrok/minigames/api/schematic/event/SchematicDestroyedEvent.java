package com.ithinkrok.minigames.api.schematic.event;

import com.ithinkrok.minigames.api.schematic.PastedSchematic;

/**
 * Created by paul on 07/01/16.
 */
public class SchematicDestroyedEvent extends SchematicEvent {

    public SchematicDestroyedEvent(PastedSchematic schematic) {
        super(schematic);
    }
}
