package com.ithinkrok.minigames.database;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Query;

import javax.persistence.OptimisticLockException;

/**
 * Created by paul on 27/01/16.
 */
public interface DatabaseAccessor {

    EbeanServer getDatabase(Class<?> databaseClass);

    <T> Query<T> find(Class<T> beanType);

    <T> T createEntityBean(Class<T> type);

    void save(Object bean) throws OptimisticLockException;
}
