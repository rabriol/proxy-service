# Redis Proxy

## Classes

* ProxyController - endpoint that interfaces GET http request with Path param being a cache key. This endpoint also treats 
  KeyNotFoundException and return an appropriate Http Status Code 404
* ConfigurationBeans - class that instantiate the main beans JedisPool and LoadingCache. JedisPool is a client to call 
  the backing Redis and LoadingCache is a cache guava library with support for:
  - default LRU eviction policy
  - global expiration based on Time Unit (Seconds, Minutes)
  - cache size by amount of keys
* CacheRepository - repository that interfaces calls to cache and treats Missing Keys throwing a customized exception to 
  upper levels
* KeyNotFoundException is a custom runtime exception created to let clear when a key is missing

## Algorithm Complexity
This project is backed by Google Guava Cache and as Google documentation 
(https://github.com/google/guava/blob/master/guava/src/com/google/common/cache/CacheBuilder.java) says it has
similar performance characteristics as ConcurrentHashMap. Similar to HashMap, ConcurrentHashMap has time complexity
for GET and PUT of O(1).

## Building and Testing

To run the tests:

```
make test
```

this command will start a redis docker instance server and after it will start the application which
will connect to Redis Server and run the integration tests.

To build and start the application:

```
./gradlew assemble && docker-compose build && docker-compose up
```

## Time Spent

* Thinking, searching and planning solution (implement my own cache solution or reuse one provided by 
  the community?) (3 hours)
* Setting Dockerfile and docker-compose (1 hour)
* Reading and learning about TestContainer
* Setting test environment with TestContainer library (3.5 hours) - I had serious problems trying to 
  figure out why it wasn't working until I noticed I was having this issue 
  https://github.com/testcontainers/testcontainers-java/issues/1773 then changing JVM to OpenJDK11 
  it worked.
* Writing Tests (30 minutes)  
* Writing documentation (2 hours)
  
## Dependencies
### JDK11
This project was built on JDK11

### Guava Cache
Google Cache Guava (https://github.com/google/guava) was used to implement LRU policy eviction, 
global expiration and cache size by key. I choose Guava instead of implementing my own solution because
I believe that in real projects it's a good practice to reuse solution specially if they are doing it well.
So, we don't need to be reinventing the wheel every time, and then we can focus on our core problem.

### Test Container
Test Container library (https://www.testcontainers.org/) was used to implement integration tests. It make it
possible to run your tests in a container environment

### Rest Assured
In order to implement Black Box tests this project is using Rest Assured (https://rest-assured.io/) that 
integration with Spring Context to boostrap the test environment and then it can execute HTTP actions 
such as POST, GET etc and validate with Json Path the response.

### Jedis
To connect to Redis this application is using the java client Jedis (To connect to Redis this application is 
using the client implementation https://github.com/redis/jedis) latest version.