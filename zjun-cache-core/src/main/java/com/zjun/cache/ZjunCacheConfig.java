package com.zjun.cache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.Map;

import org.redisson.api.EvictionMode;
import org.redisson.api.LocalCachedMapOptions.CacheProvider;
import org.redisson.api.LocalCachedMapOptions.EvictionPolicy;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * - 扩展CacheConfig，添加淘汰机制
 * @author zJun
 * @date 2022年1月9日 上午1:26:35
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZjunCacheConfig {
	
	/** - 键值条目的存活时间，以毫秒为单位。 */
	private long ttl;
	/** - 键值输入的最大空闲时间(毫秒)。 */
    private long maxIdleTime;
    private int maxSize;
	private EvictionPolicy evictionPolicy = EvictionPolicy.LRU;
	private CacheProvider cacheProvider = CacheProvider.REDISSON;
	/** - 是否开启多级缓存: 默认：false */
	private Boolean multistage = Boolean.FALSE;
	/** - 非多级缓存时生效 */
	private EvictionMode mode = EvictionMode.LRU;
	
	/**
     * Read config objects stored in JSON format from <code>String</code>
     * @param content of config
     * @return config
     * @throws IOException error
     */
    public static Map<String, ? extends ZjunCacheConfig> fromJSON(String content) throws IOException {
        return new ZjunCacheConfigSupport().fromJSON(content);
    }

    /**
     * Read config objects stored in JSON format from <code>InputStream</code>
     * @param inputStream of config
     * @return config
     * @throws IOException error
     */
    public static Map<String, ? extends ZjunCacheConfig> fromJSON(InputStream inputStream) throws IOException {
        return new ZjunCacheConfigSupport().fromJSON(inputStream);
    }

    /**
     * Read config objects stored in JSON format from <code>File</code>
     * @param file of config
     * @return config
     * @throws IOException error
     */
    public static Map<String, ? extends ZjunCacheConfig> fromJSON(File file) throws IOException {
        return new ZjunCacheConfigSupport().fromJSON(file);
    }

    /**
     * Read config objects stored in JSON format from <code>URL</code>
     * @param url of config
     * @return config
     * @throws IOException error
     */
    public static Map<String, ? extends ZjunCacheConfig> fromJSON(URL url) throws IOException {
        return new ZjunCacheConfigSupport().fromJSON(url);
    }

    /**
     * Read config objects stored in JSON format from <code>Reader</code>
     * @param reader of config
     * @return config
     * @throws IOException error
     */
    public static Map<String, ? extends ZjunCacheConfig> fromJSON(Reader reader) throws IOException {
        return new ZjunCacheConfigSupport().fromJSON(reader);
    }

    /**
     * Convert current configuration to JSON format
     * @param config object
     * @return json string
     * @throws IOException error
     */
    public static String toJSON(Map<String, ? extends ZjunCacheConfig> config) throws IOException {
        return new ZjunCacheConfigSupport().toJSON(config);
    }

    /**
     * Read config objects stored in YAML format from <code>String</code>
     * @param content of config
     * @return config
     * @throws IOException error
     */
    public static Map<String, ? extends ZjunCacheConfig> fromYAML(String content) throws IOException {
        return new ZjunCacheConfigSupport().fromYAML(content);
    }

    /**
     * Read config objects stored in YAML format from <code>InputStream</code>
     * @param inputStream of config
     * @return config
     * @throws IOException  error
     */
    public static Map<String, ? extends ZjunCacheConfig> fromYAML(InputStream inputStream) throws IOException {
        return new ZjunCacheConfigSupport().fromYAML(inputStream);
    }

    /**
     * Read config objects stored in YAML format from <code>File</code>
     * @param file of config
     * @return config
     * @throws IOException error
     */
    public static Map<String, ? extends ZjunCacheConfig> fromYAML(File file) throws IOException {
        return new ZjunCacheConfigSupport().fromYAML(file);
    }

    /**
     * Read config objects stored in YAML format from <code>URL</code>
     * @param url of config
     * @return config
     * @throws IOException error
     */
    public static Map<String, ? extends ZjunCacheConfig> fromYAML(URL url) throws IOException {
        return new ZjunCacheConfigSupport().fromYAML(url);
    }

    /**
     * Read config objects stored in YAML format from <code>Reader</code>
     * @param reader of config
     * @return config
     * @throws IOException error
     */
    public static Map<String, ? extends ZjunCacheConfig> fromYAML(Reader reader) throws IOException {
        return new ZjunCacheConfigSupport().fromYAML(reader);
    }

    /**
     * Convert current configuration to YAML format
     * @param config map
     * @return yaml string
     * @throws IOException error
     */
    public static String toYAML(Map<String, ? extends ZjunCacheConfig> config) throws IOException {
        return new ZjunCacheConfigSupport().toYAML(config);
    }
}
