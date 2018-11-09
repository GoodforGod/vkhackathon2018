package io.hackathon.storage;

import java.io.Serializable;
import java.util.Optional;

/**
 * "default comment"
 *
 * @author GoodforGod
 * @since 09.11.2018
 */
public interface ICachedStorage<T, ID extends Serializable> {

    T save(ID id, T t);

    Optional<T> cached(ID id);

    Optional<T> uncache(ID id);
}
