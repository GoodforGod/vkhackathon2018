package io.hackathon.storage.impl;

import io.hackathon.storage.ICachedStorage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * "default comment"
 *
 * @author GoodforGod
 * @since 09.11.2018
 */
abstract class BasicCacheStorage<T, ID extends Serializable> extends BasicStorage<T, ID>
        implements ICachedStorage<T, ID> {

    final Map<ID, T> cache = new HashMap<>();

    BasicCacheStorage(MongoRepository<T, ID> repository) {
        super(repository);
    }

    @Override
    public Optional<T> find(ID id) {
        return Optional.ofNullable(cached(id)
                .orElse(super.find(id)
                        .orElse(null)));
    }

    @Override
    public T save(ID id, T t) {
        return super.save(cache(id, t));
    }

    private T cache(ID id, T t) {
        cache.put(id, t);
        return t;
    }

    @Override
    public T delete(T t) {
        return super.delete(t);
    }

    @Override
    public void delete(ID id) {
        super.delete(id);
    }

    @Override
    public Optional<T> cached(ID id) {
        return Optional.ofNullable(cache.get(id));
    }

    @Override
    public Optional<T> uncache(ID id) {
        return Optional.ofNullable(cache.remove(id));
    }
}
