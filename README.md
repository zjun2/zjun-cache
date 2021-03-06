# Spring boot cache 多级缓存

- gitee仓库地址：[https://gitee.com/zhouJ/zjun-cache](https://gitee.com/zhouJ/zjun-cache)
- github仓库地址：[https://github.com/410919244/zjun-cache](https://github.com/410919244/zjun-cache)

==本文采用Spring boot cache + Caffenine + Redisson + redis 实现二级缓存，拆箱即可用。可做到零配置。==

笔者一直想通过Caffenine + Redis 实现二级缓存，却在不经意期间发现Spring boot cache 能与Caffenine集成，于是便想将这三者集成在一起。于是研究了一晚上的源码，通过复制Spring boot cache开启拦截的方式，强行在spring boot cache拦截之后以相同的方式加了一层Redis缓存，但仍无法实现Caffenine的refreshAfterWrit配置，如要实现得做较大改造，且并不好用。refreshAfterWrit的好处是：如果刷新时间到了且缓存还没过期，便会返回旧值，开启新的线程去更新缓存。无奈之下只好到处查找相关文章。于是找到了这篇文章：[SpringBoot+SpringCache实现两级缓存(Redis+Caffeine) ](https://www.cnblogs.com/cnndevelop/p/13429660.html)。因为太过于复杂，所以只是瞟了一眼，但发现了作者在结尾写的扩展，可以通过redisson增加一级缓存，于是便有了这个想法：将Spring boot cache、Caffenine、 Redisson、redis一起集成。然而过程并不顺利，继续不断查找文献，通过该文献[Redisson和Spring Cache框架整合使用](http://www.voidcc.com/redisson/redisson-integration-with-spring-cache)发现我想要的所有功能都已经实现，RedissonClusteredSpringLocalCachedCacheManager与RedissonSpringClusteredLocalCachedCacheManager已经实现了我想要的功能。但很不幸的是[redisson.pro](https://redisson.pro/)为商业收费版，找不到相关资源。无赖之下害的自己实现。于是复制了他的类名RedissonClusteredSpringLocalCachedCacheManager（懒的取名字），自己写了个CacheManager。放弃了使用数据分片功能，原因也很简单-用不到。因为大多数发布生产环境会采购云服务，以阿里云Redis集群版来说，阿里云提供了代理连接，通过代理实现了数据分片功能，数据怎么路由怎么存储都由代理处理过了。当然也可以开通直连功能，这时候就需要配置Redis多个节点。多一事不如少一事，有代理连接干嘛非得找事呢。

## 1、多级缓存的好处是什么
    
可以减少对redis的访问提高响应数度，除此之外也能很好的解决redis的缓存穿透、缓存击穿、缓存雪崩问题。另外本地缓存可以选择多种淘汰策略，比如使用LFU策略用来解决热点数据问题。

## 2、本地缓存策略

缓存淘汰策略参考的是该文献：[真正的缓存之王，Google Guava 只是弟弟](https://mp.weixin.qq.com/s/xyrvXRuG8GJfV5lWG4VrMQ)

笔者在该文献中出现了两处错误：

1. 第一处：文章指出：“refreshAfterWrite配置必须指定一个CacheLoader”。考虑到与Spring boot cache集成，并不能简单注册一个CacheLoader就能使用，原码中的CaffeineCacheManager已满足不了需求，需要创建一个新的CaffeineCacheManager，在manager中为每一个命名空间适配一个CacheLoader，然后在CacheLoader的load方法中调用对应service中的查询方法，做到这个程度要改动的代码不少。
2. 第二处：为@Cacheable与@CachePut的使用，一个用于查询一个用于修改，错误点是方法没有返回值，都是用的void类型。这会造成aop代理后无法获取返回值，导致缓存中存储的是null。通常查询都会修改void为具体对象，但会存在一部分人并不会给修改的方法添加返回值。

（也许文章作者仅是以伪代码作为示意，但这两处不够严谨，足以让不熟悉该组件的人为此掉不少头发）

### 2.1、缓存淘汰策略

1. LRU：最近最少使用算法，每次访问数据都会将其放在我们的队尾，如果需要淘汰数据，就只需要淘汰队首即可。仍然有个问题，如果有个数据在 1 分钟访问了 1000次，再后 1 分钟没有访问这个数据，但是有其他的数据访问，就导致了我们这个热点数据被淘汰。
2. LFU：最近最少频率使用，利用额外的空间记录每个数据的使用频率，然后选出频率最低进行淘汰。这样就避免了 LRU 不能处理时间段的问题。
   
缓存策略各有利弊，实现的成本也是一个比一个高，同时命中率也是一个比一个好。Guava Cache虽然有这么多的功能，但是本质上还是对LRU的封装，如果有更优良的算法，并且也能提供这么多功能，相比之下就相形见绌了。

LFU的局限性 ：在 LFU 中只要数据访问模式的概率分布随时间保持不变时，其命中率就能变得非常高。比如有部新剧出来了，我们使用 LFU 给他缓存下来，这部新剧在这几天大概访问了几亿次，这个访问频率也在我们的 LFU 中记录了几亿次。但是新剧总会过气的，比如一个月之后这个新剧的前几集其实已经过气了，但是他的访问量的确是太高了，其他的电视剧根本无法淘汰这个新剧，所以在这种模式下是有局限性。

LRU的优点和局限性 ：LRU可以很好的应对突发流量的情况，因为他不需要累计数据频率。但LRU通过历史数据来预测未来是局限的，它会认为最后到来的数据是最可能被再次访问的，从而给与它最高的优先级。

## 3、使用方式（回归正题）

### 3.1、 引入pom依赖

  zjun-cache-spring-boot-starter 使用了自动化配置，拆箱即可使用。

```xml
<!--已发布至oss.sonatype.org公共仓库-->
<!--SNAPSHOT版-->
<dependency>
  <groupId>io.github.zjun02</groupId>
  <artifactId>zjun-cache-spring-boot-starter</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>
<!--release版-->
<dependency>
  <groupId>io.github.zjun02</groupId>
  <artifactId>zjun-cache-spring-boot-starter</artifactId>
  <version>0.0.1-release</version>
</dependency>
```

### 3.2、 service 中使用

用法参照Spring boot cache。相关文献：[SpringBoot2.x—SpringCache使用](https://www.jianshu.com/p/2dc8566dd0a3)

```java
@Slf4j
@Service
// 指定了两个命名空间 demo、test。只需一个命名空间即可，此处仅为演示
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
```

### 3.3、 缓存命名空间配置

在src/main/resources目录下创建配置文件zjun-cache-config.yaml，配置文件非必需，如没有则将使用默认配置

```yaml
demo: // 命名空间
  ttl: 30000 // 键值条目的存活时间，以毫秒为单位。
  maxIdleTime: 720000 // 键值输入的最大空闲时间(毫秒)。
  maxSize: 100 // 缓存容量
  mode: LFU // 非多级缓存时生效。有效值： LRU、LFU

// 注意开启多级缓存后 ttl与maxIdleTime仅针对本地缓存生效，redis中为永久保存
test: // 命名空间
  ttl: 30000 // 键值条目的存活时间，以毫秒为单位。
  maxIdleTime: 720000 // 键值输入的最大空闲时间(毫秒)。
  maxSize: 100 // 本地缓存容量 
  multistage: true // 开启多级缓存，默认为false
  evictionPolicy: LRU // 本地缓存策略。有效值：NONE、LRU、LFU、SOFT、WEAK
  cacheProvider: CAFFEINE // 本地缓存技术选型。有效值：REDISSON、CAFFEINE。默认值：REDISSON
  

```

注意 如切换缓存淘汰策略请先清理redis中的数据，否则可能获取缓存时会出现异常

