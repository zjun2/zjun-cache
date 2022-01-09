package com.demo.zjun.service;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@CacheConfig(cacheNames = {"demo", "test"})
public class QueryService {
	
	private static int count = 0;

    @Cacheable(key = "#keyWord")
    public String query(String keyWord) {
        log.info("查询key: {}", keyWord);
        String queryResult = doQuery(keyWord);
        return queryResult;
    }
    
    @CachePut(key = "#keyWord")
    public String put(String keyWord, String value) {
    	 log.info("修改key: {}", keyWord);
         String queryResult = doQuery(value);
         return queryResult;
    }
    
    @CacheEvict(key = "#keyWord")
    public String remote(String keyWord) {
    	log.info("删除key: {}", keyWord);
        return keyWord;
    }

    private String doQuery(String keyWord) {
        try {
            Thread.sleep(100L);
            String result = keyWord + "-" + ++count;
            return result;
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }
}