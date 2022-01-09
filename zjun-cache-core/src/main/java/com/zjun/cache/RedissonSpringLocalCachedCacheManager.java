package com.zjun.cache;


import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.RMap;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.spring.cache.CacheConfig;
import org.redisson.spring.cache.RedissonCache;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import lombok.extern.slf4j.Slf4j;

/**
 * - copy RedissonSpringCacheManager
 * - RedissonSpringLocalCachedCacheManager 只有 edisson PRO 版本有，因此自己实现一个
 * @author zJun
 * @date 2022年1月9日 上午1:39:12
 */
@SuppressWarnings("unchecked")
@Slf4j
public class RedissonSpringLocalCachedCacheManager implements CacheManager, ResourceLoaderAware, InitializingBean {

    ResourceLoader resourceLoader;

    private boolean dynamic = true;
    
    private boolean allowNullValues = true;
    
    Codec codec;

    RedissonClient redisson;

    Map<String, ZjunCacheConfig> configMap = new ConcurrentHashMap<String, ZjunCacheConfig>();
    ConcurrentMap<String, Cache> instanceMap = new ConcurrentHashMap<String, Cache>();

    String configLocation;

    /**
     * - 创建由Redisson实例提供的CacheManager
     * @param redisson object
     */
    public RedissonSpringLocalCachedCacheManager(RedissonClient redisson) {
        this(redisson, (String) null, null);
    }

    /**
     * - 创建由Redisson实例提供的CacheManager
     * - 缓存配置映射的缓存名称
     * @param redisson object
     * @param config object
     */
    public RedissonSpringLocalCachedCacheManager(RedissonClient redisson, Map<String, ? extends ZjunCacheConfig> config) {
        this(redisson, config, null);
    }

    /**
     * - 创建由Redisson实例提供的CacheManager
     * - 缓存配置映射的缓存名称
     * 每个Cache实例共享一个编解码器实例。
     * @param redisson object
     * @param config object
     * @param codec object
     */
    public RedissonSpringLocalCachedCacheManager(RedissonClient redisson, Map<String, ? extends ZjunCacheConfig> config, Codec codec) {
        this.redisson = redisson;
        this.configMap = (Map<String, ZjunCacheConfig>) config;
        this.codec = codec;
    }

    /**
     * - 创建由Redisson实例提供的CacheManager
     * - 缓存配置映射的缓存名称
     * - 从类路径加载配置文件，将普通路径解释为类路径资源名
     * - 包含包路径(例如:“mypackage / myresource.txt”)。
     * @param redisson object
     * @param configLocation path
     */
    public RedissonSpringLocalCachedCacheManager(RedissonClient redisson, String configLocation) {
        this(redisson, configLocation, null);
    }

    /**
     * - 创建由Redisson实例提供的CacheManager
     * - 缓存配置映射的缓存名称
     * - 每个Cache实例共享一个编解码器实例。
     * - 从类路径加载配置文件，将普通路径解释为类路径资源名
     * - 包含包路径(例如:“mypackage / myresource.txt”)。
     * @param redisson object
     * @param configLocation path
     * @param codec object
     */
    public RedissonSpringLocalCachedCacheManager(RedissonClient redisson, String configLocation, Codec codec) {
        this.redisson = redisson;
        this.configLocation = configLocation;
        this.codec = codec;
    }
    
    /**
     * - 定义存储{@code null}值的可能性。
     * - 默认值 <code>true</code>
     * @param allowNullValues - stores if <code>true</code>
     */
    public void setAllowNullValues(boolean allowNullValues) {
        this.allowNullValues = allowNullValues;
    }

    /**
     * - 定义“固定”缓存名称。
     * - 对于未定义的名称，将不会动态创建新的缓存实例。
     * - ' null '参数设置动态模式
     * @param names of caches
     */
    public void setCacheNames(Collection<String> names) {
        if (names != null) {
            for (String name : names) {
                getCache(name);
            }
            dynamic = false;
        } else {
            dynamic = true;
        }
    }
    
    /**
     * - 设置缓存配置位置
     * @param configLocation object
     */
    public void setConfigLocation(String configLocation) {
        this.configLocation = configLocation;
    }

    /**
     * - 设置缓存名称映射的缓存配置
     * @param config object
     */
    public void setConfig(Map<String, ? extends ZjunCacheConfig> config) {
        this.configMap = (Map<String, ZjunCacheConfig>) config;
    }

    /**
     * - 设置Redisson实例
     * @param redisson instance
     */
    public void setRedisson(RedissonClient redisson) {
        this.redisson = redisson;
    }

    /**
     * - 设置所有缓存实例之间共享的编解码器实例
     * @param codec object
     */
    public void setCodec(Codec codec) {
        this.codec = codec;
    }
    
    protected ZjunCacheConfig createDefaultConfig() {
        return new ZjunCacheConfig();
    }

    @Override
    public Cache getCache(String name) {
        Cache cache = instanceMap.get(name);
        if (cache != null) {
            return cache;
        }
        if (!dynamic) {
            return cache;
        }
        ZjunCacheConfig config = configMap.get(name);
        if (config == null) {
            config = createDefaultConfig();
            configMap.put(name, config);
        }
        
		if (config.getMultistage()) {
			return createMap(name, config);
		}
        
        return createMapCache(name, config);
    }
    
    private Cache createMapCache(String name, ZjunCacheConfig config) {
    	CacheConfig cc = new CacheConfig(config.getTtl(), config.getMaxIdleTime());
    	cc.setMaxSize(config.getMaxSize());
        RMapCache<Object, Object> map = getMapCache(name, cc);
        
        Cache cache = new RedissonCache(map, cc, allowNullValues);
        Cache oldCache = instanceMap.putIfAbsent(name, cache);
        if (oldCache != null) {
            cache = oldCache;
        } else {
            map.setMaxSize(config.getMaxSize(), config.getMode());
        }
        return cache;
    }

    protected RMapCache<Object, Object> getMapCache(String name, CacheConfig config) {
        if (codec != null) {
            return redisson.getMapCache(name, codec);
        }
        return redisson.getMapCache(name);
    }

	private Cache createMap(String name, ZjunCacheConfig config) {
		RMap<Object, Object> map = getMap(name, config);
//        Cache cache = new RedissonLocalCache(map, allowNullValues);
		Cache cache = new RedissonCache(map, allowNullValues);
		Cache oldCache = instanceMap.putIfAbsent(name, cache);
		if (oldCache != null) {
			cache = oldCache;
		}
		return cache;
	}

	protected RMap<Object, Object> getMap(String name, ZjunCacheConfig config) {
		LocalCachedMapOptions<Object, Object> options = LocalCachedMapOptions.defaults()
				// 淘汰机制有LFU, LRU和NONE这几种算法策略可供选择
				.evictionPolicy(config.getEvictionPolicy()).cacheSize(config.getMaxSize())
				.cacheProvider(config.getCacheProvider())
				// 每个Map本地缓存里元素的有效时间，默认毫秒为单位
				.timeToLive(config.getTtl())
				// 每个Map本地缓存里元素的最长闲置时间，默认毫秒为单位
				.maxIdle(config.getMaxIdleTime());
		if (codec != null) {
			return redisson.getLocalCachedMap(name, codec, options);
		}
		return redisson.getLocalCachedMap(name, options);
	}

    @Override
    public Collection<String> getCacheNames() {
        return Collections.unmodifiableSet(configMap.keySet());
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (configLocation == null) {
            return;
        }

        Resource resource = resourceLoader.getResource(configLocation);
        try {
            this.configMap = (Map<String, ZjunCacheConfig>) ZjunCacheConfig.fromJSON(resource.getInputStream());
        } catch (IOException e) {
            // try to read yaml
            try {
                this.configMap = (Map<String, ZjunCacheConfig>) ZjunCacheConfig.fromYAML(resource.getInputStream());
            } catch (IOException e1) {
                throw new BeanDefinitionStoreException(
                        "Could not parse cache configuration at [" + configLocation + "]", e1);
            }
        }
        log.debug("配置文件「{}」解析结果：{}", configLocation, this.configMap);
    }
    
}