package com.ithinkrok.minigames.api.item.attributes;

import com.ithinkrok.minigames.api.util.ReflectionUtils;
import com.ithinkrok.util.config.Config;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

    public ItemAttributeModifier(Object modifier) {
        try {

            Method getString = modifier.getClass().getMethod("getString", String.class);

            this.attribute = (String) getString.invoke(modifier, "AttributeName");
            this.name = (String) getString.invoke(modifier, "Name");
            this.slot = Slot.getFromName((String) getString.invoke(modifier, "Slot"));

            Method getInt = modifier.getClass().getMethod("getInt", String.class);
            this.operation = Operation.getFromId((int) getInt.invoke(modifier, "Operation"));

            Method getDouble = modifier.getClass().getMethod("getDouble", String.class);
            this.amount = (double) getDouble.invoke(modifier, "Amount");

            Method getLong = modifier.getClass().getMethod("getLong", String.class);
            this.uuid =
                    new UUID((long) getLong.invoke(modifier, "UUIDMost"), (long) getLong.invoke(modifier, "UUIDLeast"));

        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            throw new IllegalArgumentException("Object provided is not an NBTCompound");
        }
    }

    public ItemAttributeModifier(Config config) {
        String attribute = config.getString("attribute");

        try{
            attribute = getAttributeName(Attribute.valueOf(attribute.toUpperCase()));
        } catch (IllegalArgumentException ignored) { }

        this.attribute = attribute;

        this.name = config.getString("name", "Unnamed Attribute Modifier");
        this.slot = Slot.getFromName(config.getString("slot"));

        this.amount = config.getDouble("amount");

        Operation operation = Operation.ADDITIVE;

        if(config.isString("operation")) {
            try{
                operation = Operation.valueOf(config.getString("operation"));
            } catch (Exception ignored) {
                System.out.println("Invalid operation for attribute " + this.name + ", will use default additive");
            }
        } else {
            operation = Operation.getFromId(config.getInt("operation", 0));
        }

        this.operation = operation;

        UUID uuid;

        if(config.contains("uuid")) {
            uuid = UUID.fromString(config.getString("uuid"));
        } else {
            uuid = UUID.randomUUID();
        }

        this.uuid = uuid;
    }

    @SuppressWarnings("ConstantConditions")
    public Object getNBT() {
        try {
            Object data = ReflectionUtils.getNMSClass("NBTTagCompound").newInstance();
            if (data != null) {

                Method setString = data.getClass().getMethod("setString", String.class, String.class);
                setString.invoke(data, "AttributeName", getAttribute());
                setString.invoke(data, "Name", getName());
                if (getSlot() != null) {
                    setString.invoke(data, "Slot", getSlot().getName());
                }

                Method setInt = data.getClass().getMethod("setInt", String.class, int.class);
                setInt.invoke(data, "Operation", getOperation().getId());

                Method setDouble = data.getClass().getMethod("setDouble", String.class, double.class);
                setDouble.invoke(data, "Amount", getAmount());

                Method setLong = data.getClass().getMethod("setLong", String.class, long.class);
                setLong.invoke(data, "UUIDMost", getUuid().getMostSignificantBits());
                setLong.invoke(data, "UUIDLeast", getUuid().getLeastSignificantBits());

                return data;
            } else {
                Bukkit.getLogger().info("[ItemAttributeAPI] Incompatible Server version! Missing classes.");
                return null;
            }
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException e) {
            Bukkit.getLogger().info("[ItemAttributeAPI] Incompatible server version! Some methods can't be applied.");
            return null;
        }
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
