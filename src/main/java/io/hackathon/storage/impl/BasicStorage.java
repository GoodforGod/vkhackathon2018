package io.hackathon.storage.impl;

import io.hackathon.storage.IStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * "default comment"
 *
 * @author GoodforGod
 * @since 09.11.2018
 */
abstract class BasicStorage<T, ID extends Serializable> implements IStorage<T, ID> {

    final Logger logger = LoggerFactory.getLogger(BasicStorage.class);

    final MongoRepository<T, ID> repository;

    BasicStorage(MongoRepository<T, ID> repository) {
        this.repository = repository;
    }

    @Override
    public Optional<T> find(ID id) {
        return repository.findById(id);
    }

    @Override
    public List<T> findAll() {
        return repository.findAll();
    }

    @Override
    public T save(T t) {
        return repository.save(t);
    }

    @Override
    public List<T> save(List<T> list) {
        return repository.saveAll(list);
    }

    @Override
    public T delete(T t) {
        repository.delete(t);
        return t;
    }

    @Override
    public void delete(ID id) {
        repository.deleteById(id);
    }

    @Override
    public void deleteAll() {
        repository.deleteAll();
    }
}
