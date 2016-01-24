package com.ithinkrok.minigames.schematic.event;

import com.ithinkrok.minigames.schematic.PastedSchematic;

/**
 * Created by paul on 07/01/16.
 */
public class SchematicFinishedEvent extends SchematicEvent {

    public SchematicFinishedEvent(PastedSchematic schematic) {
        super(schematic);
    }

}
