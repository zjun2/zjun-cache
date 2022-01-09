package io.github.zjun02;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.Map;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * - copy CacheConfigSupport
 * @author zJun
 * @date 2022年1月9日 上午2:39:24
 */
public class ZjunCacheConfigSupport {

	ObjectMapper jsonMapper = new ObjectMapper();
    ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    public Map<String, ZjunCacheConfig> fromJSON(String content) throws IOException {
        return jsonMapper.readValue(content, new TypeReference<Map<String, ZjunCacheConfig>>() {});
    }

    public Map<String, ZjunCacheConfig> fromJSON(File file) throws IOException {
        return jsonMapper.readValue(file, new TypeReference<Map<String, ZjunCacheConfig>>() {});
    }

    public Map<String, ZjunCacheConfig> fromJSON(URL url) throws IOException {
        return jsonMapper.readValue(url, new TypeReference<Map<String, ZjunCacheConfig>>() {});
    }

    public Map<String, ZjunCacheConfig> fromJSON(Reader reader) throws IOException {
        return jsonMapper.readValue(reader, new TypeReference<Map<String, ZjunCacheConfig>>() {});
    }

    public Map<String, ZjunCacheConfig> fromJSON(InputStream inputStream) throws IOException {
        return jsonMapper.readValue(inputStream, new TypeReference<Map<String, ZjunCacheConfig>>() {});
    }

    public String toJSON(Map<String, ? extends ZjunCacheConfig> configs) throws IOException {
        return jsonMapper.writeValueAsString(configs);
    }

    public Map<String, ZjunCacheConfig> fromYAML(String content) throws IOException {
        return yamlMapper.readValue(content, new TypeReference<Map<String, ZjunCacheConfig>>() {});
    }

    public Map<String, ZjunCacheConfig> fromYAML(File file) throws IOException {
        return yamlMapper.readValue(file, new TypeReference<Map<String, ZjunCacheConfig>>() {});
    }

    public Map<String, ZjunCacheConfig> fromYAML(URL url) throws IOException {
        return yamlMapper.readValue(url, new TypeReference<Map<String, ZjunCacheConfig>>() {});
    }

    public Map<String, ZjunCacheConfig> fromYAML(Reader reader) throws IOException {
        return yamlMapper.readValue(reader, new TypeReference<Map<String, ZjunCacheConfig>>() {});
    }

    public Map<String, ZjunCacheConfig> fromYAML(InputStream inputStream) throws IOException {
        return yamlMapper.readValue(inputStream, new TypeReference<Map<String, ZjunCacheConfig>>() {});
    }

    public String toYAML(Map<String, ? extends ZjunCacheConfig> configs) throws IOException {
        return yamlMapper.writeValueAsString(configs);
    }
}
