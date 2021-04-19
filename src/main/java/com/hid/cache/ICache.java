package com.hid.cache;

import java.util.Optional;
/**
 * Generic Interface for cache implementation.
 * 
 * @author raguramm
 *
 * @param <K>
 * @param <V>
 */
public interface ICache<K, V> {
	/**
	 * API to retrieve or obtain the cache entry from the map.
	 * @param key - cache entry key
	 * @return CacheValue
	 */
	Optional<V> get(K key);
	/**
	 * API to insert or add cache entry to the underlying cache (hashmap)!
	 * @param key   - Cache entry key
	 * @param value - Cache value
	 * @param timeToLive - 
	 */
    void add(K key, V value, Optional<Long> timeToLive) throws CacheOverFlowException;
    /**
     * API to remove the cache entry from hashmap.
     * @param key - cache key to be removed!
     */
    void remove(K key);

}
