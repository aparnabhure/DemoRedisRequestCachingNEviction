# DemoRedisRequestCachingNEviction
Demo to Cache a request and evict

Notes: If multiple services are pointing to the same redis server then with same key cache is evictable from other service

For testing installed local redis and ran >redis-server command to have local redis server running

Swagger JSON API:
http://localhost:8080/swagger-ui.html