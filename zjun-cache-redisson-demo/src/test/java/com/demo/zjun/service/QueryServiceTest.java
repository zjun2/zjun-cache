package com.demo.zjun.service;

import javax.annotation.Resource;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
class QueryServiceTest {
	
	@Resource
	private QueryService queryService;

	@Test
	void test() {
		String key = "QWE";
		String value = "ABC";
		String res = queryService.query(key);
		log.info("返回结果: {}", res);
		res = queryService.query(key);
		log.info("返回结果2: {}", res);
		res = queryService.put(key, value);
		log.info("更新结果: {}", res);
		res = queryService.query(key);
		log.info("更新后查询结果: {}", res);
		res = queryService.remote(key);
		res = queryService.query(key);
		log.info("删除后查询结果: {}", res);
		
	}

}
