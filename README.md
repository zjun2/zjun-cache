# Spring boot cache 多级缓存

## 多级缓存的好处是什么
    可以减少对redis的访问提高响应数度，除此之外也能很好的解决redis的缓存穿透、缓存击穿、缓存雪崩问题。另外本地缓存可以选择多种淘汰策略，比如使用LFU策略用来解决热点数据问题。

## 本地缓存策略

    1. LRU：最近最少使用算法，每次访问数据都会将其放在我们的队尾，如果需要淘汰数据，就只需要淘汰队首即可。仍然有个问题，如果有个数据在 1 分钟访问了 1000次，再后 1 分钟没有访问这个数据，但是有其他的数据访问，就导致了我们这个热点数据被淘汰。

    2. LFU：最近最少频率使用，利用额外的空间记录每个数据的使用频率，然后选出频率最低进行淘汰。这样就避免了 LRU 不能处理时间段的问题。
   
缓存策略各有利弊，实现的成本也是一个比一个高，同时命中率也是一个比一个好。Guava Cache虽然有这么多的功能，但是本质上还是对LRU的封装，如果有更优良的算法，并且也能提供这么多功能，相比之下就相形见绌了。

LFU的局限性 ：在 LFU 中只要数据访问模式的概率分布随时间保持不变时，其命中率就能变得非常高。比如有部新剧出来了，我们使用 LFU 给他缓存下来，这部新剧在这几天大概访问了几亿次，这个访问频率也在我们的 LFU 中记录了几亿次。但是新剧总会过气的，比如一个月之后这个新剧的前几集其实已经过气了，但是他的访问量的确是太高了，其他的电视剧根本无法淘汰这个新剧，所以在这种模式下是有局限性。

LRU的优点和局限性 ：LRU可以很好的应对突发流量的情况，因为他不需要累计数据频率。但LRU通过历史数据来预测未来是局限的，它会认为最后到来的数据是最可能被再次访问的，从而给与它最高的优先级。

## 使用方式

### 1. 引入pom依赖
    zjun-cache-spring-boot-starter 使用了自动化配置，拆箱即可使用，但匀使用的是默认配置

```xml
<dependency>
    <groupId>com.zjun</groupId>
    <artifactId>zjun-cache-spring-boot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 2. service 中使用

    用法参照Spring boot cache。相关文献：https://www.jianshu.com/p/2dc8566dd0a3

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

### 3. 缓存命名空间配置

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