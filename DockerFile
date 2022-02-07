FROM openjdk:11
COPY target/DemoRedisRequestCachingNEviction-0.0.1-SNAPSHOT.jar demo_redis_cache_eviction.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/demo_redis_cache_eviction.jar"]