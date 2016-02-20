package com.ithinkrok.minigames.api.metadata;

/**
 * Created by paul on 06/01/16.
 */
public interface MetadataHolder<M extends Metadata> {

    <B extends M> B getMetadata(Class<? extends B> clazz);
    <B extends M> void setMetadata(B metadata);
    boolean hasMetadata(Class<? extends M> clazz);
}
