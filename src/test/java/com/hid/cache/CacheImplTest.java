package com.hid.cache;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;


public class CacheImplTest {

	private static final String KEY1 = "key1";
	private static final String KEY2 = "key2";
	private static final String KEY3 = "key3";
	private static final String VALUE1 = "value1";
	private static final String VALUE2 = "value2";
	private static final String VALUE3 = "value3";
	
	@Test
	public void testCacheAdd() {
		CacheImpl<String,Object> cacheObj = new CacheImpl<String, Object>(1000L, 2);
		String str = "1234";
		byte[] byteArray = str.getBytes();

		Optional<Long> ttl = Optional.of(500L);
		cacheObj.add(KEY1, VALUE1, ttl);
		//Store binary (byte array) data into cache map.
		cacheObj.add(KEY2, byteArray, ttl);

		Assert.assertEquals(cacheObj.get(KEY1), Optional.of(VALUE1));
		String result = new String((byte[])cacheObj.get(KEY2).get());
		
		Assert.assertEquals(result, str);
	}	

	@Test 
	public void testCacheAddItemAlreadyExists() {
		CacheImpl<String,String> cacheObj = new CacheImpl<String, String>(1000L, 2);
		Optional<Long> ttl = Optional.of(500L);
		cacheObj.add(KEY1, VALUE1, ttl);
		// VALUE2 overrides VALUE1 for given cache entry.
		cacheObj.add(KEY1, VALUE2, ttl);

		Assert.assertEquals(cacheObj.get(KEY1), Optional.of(VALUE2));
	}


	@Test 
	public void testCacheExpiry() throws InterruptedException {
		CacheImpl<String,String> cacheObj = new CacheImpl<String, String>(10L, 2);
		cacheObj.add(KEY1, VALUE1);

		Thread.sleep(100);

		Assert.assertEquals(cacheObj.get(KEY1), Optional.empty());		
	}

	@Test 
	public void testCacheTTL() throws InterruptedException {
		CacheImpl<String,String> cacheObj = new CacheImpl<String, String>(10L, 2);
		
		cacheObj.add(KEY1, VALUE1, Optional.of(500L));
		// Sleep for 100 ms.
		Thread.sleep(100);	
		
		Assert.assertEquals(cacheObj.get(KEY1), Optional.of(VALUE1));		
		// Sleep for 400 ms. 400 + 100 = 500ms that equals the actual TTL value.
		Thread.sleep(400);

		Assert.assertEquals(cacheObj.get(KEY1), Optional.empty());
	}

	@Test(expected = CacheOverFlowException.class)
	public void testCacheOverflow() {
		CacheImpl<String,String> cacheObj = new CacheImpl<String, String>(10L, 2);
		cacheObj.add(KEY1, VALUE1, Optional.of(500L));
		cacheObj.add(KEY2, VALUE2, Optional.of(500L));
		/**
		 * This would throw an cache overflow exception!!
		 */
		cacheObj.add(KEY3, VALUE3, Optional.of(500L));
	}

	@Test
	public void testCacheDelete() {
		CacheImpl<String,String> cacheObj = new CacheImpl<String, String>(1000L, 2);

		cacheObj.add(KEY1, VALUE1);
		// Before delete KEY1 should map to VALUE1.
		Assert.assertEquals(cacheObj.get(KEY1), Optional.of(VALUE1));
		cacheObj.remove(KEY1);
		// After delete - KEY1 shouldn't exist in cache and should return an optional empty.
		Assert.assertEquals(cacheObj.get(KEY1), Optional.empty());
	}
}

