package com.hid.cache;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
/**
 * Generic class to support caching mechanism.<br><br>
 * 
 * <code>Business Logic</code> :  During cache creation - the timestamp (or creation time) is computed and
 * based on this creation date/time and TTL parameter, we could calculate whether an entry in the cache is expired or not
 * 
 * @author raguramm
 *
 * @param <K>
 * @param <V>
 */
public class CacheImpl<K, V> implements ICache<K, V> {	

	protected Map<K, CacheValue<V>> cacheMap;
    protected Long cacheTimeToLive;
    protected int maxEntries;
 
    public CacheImpl(Long globalTimeToLive, int maxEntries) {
        this.cacheTimeToLive = globalTimeToLive;
        this.maxEntries = maxEntries;
        this.clear();
        // Trigger the clean up task
       // this.cleanup();
    }

    
    protected Set<K> getExpiredKeys() {
        return this.cacheMap.keySet().parallelStream()
                .filter(this::isExpired)
                .collect(Collectors.toSet());
    }
    /**
     * This method is responsible to validate the cache entries for time of expiration. If expired, it would be invalidated or removed from map else it would remain intact.
     * @param key - Cache entry
     * @return
     */
    protected boolean isExpired(K key) {
    	Optional<Long> timeToLive =   Optional.ofNullable(Optional.ofNullable(this.cacheMap.get(key)).map(CacheValue::getTimeToLive).orElse(this.cacheTimeToLive));
    	LocalDateTime expirationDateTime = null;
    	/**
    	 * If timeToLive optional entry exists as part of 'CacheValue', compare it against present time orElse compare global timeout parameter
    	 */
    	if(timeToLive.isPresent()) {
    		expirationDateTime = this.cacheMap.get(key).getCreatedAt().plus(timeToLive.get(), ChronoUnit.MILLIS);
    	} 
        
        return expirationDateTime != null ?  LocalDateTime.now().isAfter(expirationDateTime) : false;
    }
    
   @Override
    public Optional<V> get(K key) {
    	/**
    	 * Remove the expired cache entries from the map so that only active entries are being processed.
    	 */
        this.clean();
        return Optional.ofNullable(this.cacheMap.get(key)).map(CacheValue::getValue);
    }
   
   /**
    * This method is responsible to perform cleanup of the expired cache entries periodically (probably after every 2 seconds).
    */
   public void cleanup() {
	   ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
	   exec.scheduleAtFixedRate(new Runnable() {
		   @Override
		   public void run() {
			   //System.out.println("Clean invoked!!");
			   CacheImpl.this.clean();
		   }
	   }
	   , 0, 2, TimeUnit.SECONDS);
   }
   

    @Override
    public void add(K key, V value, Optional<Long> timeToLive) throws CacheOverFlowException {
    	Long ttl = null;
    	if(timeToLive.isPresent()) {
    		ttl = timeToLive.get();    		
    	} 
    	if(this.cacheMap.size() >= this.maxEntries) {
    		throw new CacheOverFlowException("Cache Overflow!!");
    	}
    	    	
    	this.cacheMap.put(key, this.createCacheValue(value, ttl));
    }
    
    
    public void add(K key, V value) throws CacheOverFlowException {
    	this.add(key, value, Optional.empty());
    }

    protected CacheValue<V> createCacheValue(V value, Long timeToLive) {
    	// Obtain the current time
        LocalDateTime now = LocalDateTime.now();
        return new CacheValue<V>() {
            @Override
            public V getValue() {
                return value;
            }

            @Override
            public LocalDateTime getCreatedAt() {
                return now;
            }

			@Override
			public Long getTimeToLive() {
				// optional 'ttl' as part of cache entry
				return timeToLive;
			}
        };
    }

    @Override
    public void remove(K key) {
        this.cacheMap.remove(key);
    }
    /**
     * Interface that keeps track of the cache value(s) and its associated creation time-stamp along with the 'time to live' parameter.
     * 
     * This interface only stores the value of the “cached” item (which is of the generic type V) 
     * its creation date/time and (optional) time to expire parameter. 
     * 
     * @author raguramm
     *
     * @param <V>
     */
    protected interface CacheValue<V> {
        V getValue();

        LocalDateTime getCreatedAt();
        
        Long getTimeToLive();
    }
    
    public void clean() {
        for(K key: this.getExpiredKeys()) {
            this.remove(key);
        }
    }
    

    public void clear() {
        this.cacheMap = new HashMap<>();
    }

}
