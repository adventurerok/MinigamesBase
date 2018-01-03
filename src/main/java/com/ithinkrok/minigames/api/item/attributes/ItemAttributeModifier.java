package com.ithinkrok.minigames.api.item.attributes;

import com.ithinkrok.util.config.Config;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import org.bukkit.attribute.Attribute;

import java.util.UUID;

public class ItemAttributeModifier {

    private final String attribute;
    private final Slot slot;
    private final Operation operation;

    private final String name;
    private final double amount;
    private final UUID uuid;


    public ItemAttributeModifier(Attribute attribute, String name, double amount, Operation operation, Slot slot) {
        this(attribute, name, amount, operation, slot, UUID.randomUUID());
    }


    public ItemAttributeModifier(Attribute attribute, String name, double amount, Operation operation, Slot slot,
                                 UUID uuid) {
        this(getAttributeName(attribute), name, amount, operation, slot, uuid);
    }


    public ItemAttributeModifier(String attribute, String name, double amount, Operation operation, Slot slot,
                                 UUID uuid) {
        this.attribute = attribute;
        this.slot = slot;
        this.operation = operation;
        this.name = name;
        this.amount = amount;
        this.uuid = uuid;
    }


    private static String getAttributeName(Attribute attribute) {
        switch (attribute) {
            case GENERIC_MAX_HEALTH:
                return "generic.maxHealth";
            case GENERIC_FOLLOW_RANGE:
                return "generic.followRange";
            case GENERIC_KNOCKBACK_RESISTANCE:
                return "generic.knockbackResistance";
            case GENERIC_MOVEMENT_SPEED:
                return "generic.movementSpeed";
            case GENERIC_ATTACK_DAMAGE:
                return "generic.attackDamage";
            case GENERIC_ARMOR:
                return "generic.armor";
            case GENERIC_ATTACK_SPEED:
                return "generic.attackSpeed";
            case GENERIC_LUCK:
                return "generic.luck";
            case HORSE_JUMP_STRENGTH:
                return "horse.jumpStrength";
            case ZOMBIE_SPAWN_REINFORCEMENTS:
                return "zombie.spawnReinforcements";
            default:
                throw new IllegalArgumentException("Unsupported Attribute: " + attribute);
        }
    }


    public ItemAttributeModifier(String attribute, String name, double amount, Operation operation, Slot slot) {
        this(attribute, name, amount, operation, slot, UUID.randomUUID());
    }


    public ItemAttributeModifier(NBTTagCompound modifier) {
        this.attribute = modifier.getString("AttributeName");
        this.name = modifier.getString("Name");
        this.slot = Slot.getFromName(modifier.getString("Slot"));

        this.operation = Operation.getFromId(modifier.getInt("Operation"));

        this.amount = modifier.getDouble("Amount");

        this.uuid =
                new UUID(modifier.getLong("UUIDMost"), modifier.getLong("UUIDLeast"));

    }


    public ItemAttributeModifier(Config config) {
        String attribute = config.getString("attribute");

        try {
            attribute = getAttributeName(Attribute.valueOf(attribute.toUpperCase()));
        } catch (IllegalArgumentException ignored) {
        }

        this.attribute = attribute;

        this.name = config.getString("name", "Unnamed Attribute Modifier");
        this.slot = Slot.getFromName(config.getString("slot"));

        this.amount = config.getDouble("amount");

        Operation operation = Operation.ADDITIVE;

        if (config.isString("operation")) {
            try {
                operation = Operation.valueOf(config.getString("operation"));
            } catch (Exception ignored) {
                System.out.println("Invalid operation for attribute " + this.name + ", will use default additive");
            }
        } else {
            operation = Operation.getFromId(config.getInt("operation", 0));
        }

        this.operation = operation;

        UUID uuid;

        if (config.contains("uuid")) {
            uuid = UUID.fromString(config.getString("uuid"));
        } else {
            uuid = UUID.randomUUID();
        }

        this.uuid = uuid;
    }


    public NBTTagCompound getNBT() {
        NBTTagCompound data = new NBTTagCompound();

        data.setString("AttributeName", getAttribute());
        data.setString("Name", getName());
        if (getSlot() != null) {
            data.setString("Slot", getSlot().getName());
        }

        data.setInt("Operation", getOperation().getId());

        data.setDouble("Amount", getAmount());

        data.setLong("UUIDMost", getUuid().getMostSignificantBits());
        data.setLong("UUIDLeast", getUuid().getLeastSignificantBits());

        return data;

    }


    public String getAttribute() {
        return attribute;
    }


    public String getName() {
        return name;
    }


    public Slot getSlot() {
        return slot;
    }


    public Operation getOperation() {
        return operation;
    }


    public double getAmount() {
        return amount;
    }


    public UUID getUuid() {
        return uuid;
    }
}
