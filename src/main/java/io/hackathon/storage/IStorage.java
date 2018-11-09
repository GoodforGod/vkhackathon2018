package io.hackathon.storage;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * "default comment"
 *
 * @author GoodforGod
 * @since 09.11.2018
 */
public interface IStorage<T, ID extends Serializable> {

    Optional<T> find(ID id);
    List<T> findAll();

    T save(T t);
    List<T> save(List<T> list);

    T delete(T t);
    void delete(ID id);
}
