package zjun2.top.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisOperations;

import lombok.extern.slf4j.Slf4j;
import zjun2.top.cache.RedissonSpringLocalCachedCacheManager;

@Slf4j
@Configuration
@ConditionalOnClass({ Redisson.class, RedisOperations.class })
@AutoConfigureAfter(RedissonAutoConfiguration.class)
@EnableCaching
public class ZjunCacheAutoConfiguration {

	// - 加载默认配置文件地址
	private static String DEFFAUT_CONFIGLOCATION = "classpath:/zjun-cache-config.yaml";

	@Bean
	@ConditionalOnMissingBean(CacheManager.class)
	public CacheManager cacheManager(RedissonClient redissonClient) {
		log.debug("源码地址: https://gitee.com/zhouJ/zjun-cache");
		log.debug("如需配置命名空间请在resources中添加zjun-cache-config.yaml文件");
		log.debug("创建RedissonSpringLocalCachedCacheManager，加载配置文件: {}", DEFFAUT_CONFIGLOCATION);
		return new RedissonSpringLocalCachedCacheManager(redissonClient, DEFFAUT_CONFIGLOCATION);
	}

}
