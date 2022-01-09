package com.demo.zjun.controller;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.RBucket;
import org.redisson.api.RLocalCachedMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.demo.zjun.service.QueryService;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@RestController
public class QueryController {

	@Autowired
    private QueryService queryService;
	
	@Autowired
	private RedissonClient redissonClient;

	@GetMapping("/query")
    public ResponseEntity<?> query(String keyWord) {
        String result = queryService.query(keyWord);
        return ResponseEntity.ok(result);
    }
	
	@GetMapping("/query2")
    public ResponseEntity<?> query2(String keyWord) {
//        String result = queryService.query(keyWord);
		String result = null;
//        RBucket<String> bucket = redissonClient.getBucket(keyWord);
//        result =  bucket.get();
//		if (StringUtils.isEmpty(result)) {
//			result = queryService.query(keyWord);
//			bucket.set(result, 30, TimeUnit.SECONDS);
//		}
		
		RLocalCachedMap<Object, Object> cities = redissonClient.getLocalCachedMap("cities",
				LocalCachedMapOptions.defaults());
		City c1 = new City("武汉", "湖北");
		cities.put(1, c1);
		City c = (City) cities.get(1);
		System.out.println(c.name + "-" + c.province);
        return ResponseEntity.ok(result);
    }
	
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class City implements Serializable {
		private static final long serialVersionUID = 1L;
		private String  name;
		private String province;
	}

//    @Autowired
//    @SuppressWarnings("all")
//    private CacheManager cacheManager;

//    @GetMapping("/caches")
//    public ResponseEntity<?> getCache() {
//        Map<String, ConcurrentMap> cacheMap = cacheManager.getCacheNames().stream()
//                .collect(Collectors.toMap(Function.identity(), name -> {
//                    Cache cache = (Cache) cacheManager.getCache(name).getNativeCache();
//                    return cache.asMap();
//                }));
//        return ResponseEntity.ok(cacheMap);
//    }
}
