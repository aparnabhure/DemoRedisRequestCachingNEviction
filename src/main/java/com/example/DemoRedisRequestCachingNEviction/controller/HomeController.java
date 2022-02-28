package com.example.DemoRedisRequestCachingNEviction.controller;

import com.example.DemoRedisRequestCachingNEviction.RequestResponse;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/demo_cache")
public class HomeController {
    //NOTES: For generic Key it has to be declared as public static final
    /**
     * If 2 services pointing to the same redis server and we want to evict the cache from another server then
     * we need the same configuration CacheManger and then you can call the evict from another service
     * How I tested for future reference:
     * > Created Demo service 1 with same redis beans and configurations and same cache key
     * > On Get Text API added cachable and on savetext API not added any eviction code
     * > Ran the service on 8080
     * > Via postman localhost:8080/home/gettext API request got saved in cache
     * > Via postman localhost:8080/home/savetext API updated the text but as request was cahed the next get call was giving old response
     *
     * > Created this new app with same redis benas & configurations and with same Cache Key
     * > Created Save Text API as below and added Evict in this API
     * > Ran this service by pointing to 8081 port see applications.properties
     * > Via postman called savetext API this call had evicted the cache
     *
     * > Now made GetText call of the first service via localhost:8080/home/gettext
     * > Now this time I got the updated text as cache got evicted
     */
    public static final String KEY = "test_key";
    private String sometext = "Hello Aparna";

    private Map<String, Set<String>> userStringsMap = new HashMap<>();

    @GetMapping(value = "/home")
    public String home(){
        return "home";
    }

    @GetMapping(value = "/home/gettext")
    @ResponseBody
    @Cacheable(
        value="test_ab",
        cacheManager = "cabCacheManager",
        key = "#root.target.KEY")
    public String getHomeText(){
        return sometext;
    }

    @PostMapping(value = "/home/savetext")
    @ResponseBody
    @CacheEvict(value="test_ab",
        cacheManager = "cabCacheManager",
        key = "#root.target.KEY")
    public String saveHomeText(@RequestBody String text){
        this.sometext = text;
        return "Text updated";
    }


    @GetMapping(value = "/home/gettext/{userId}")
    @ResponseBody
    @Cacheable(
        value="test_ab",
        cacheManager = "cabCacheManager",
        key="{#userId}")
    public String getHomeTextForUser(@PathVariable String userId){
        return userStringsMap.getOrDefault(userId, new LinkedHashSet<>()).toString();
    }

    @PostMapping(value = "/home/savetext/{userId}")
    @ResponseBody
    @CacheEvict(value="test_ab",
        cacheManager = "cabCacheManager",
        key="{#userId}")
    public String saveHomeTextForUser(@PathVariable String userId, @RequestBody String text){
        Set<String> sets = new LinkedHashSet<>();
        if(userStringsMap.containsKey(userId)){
            sets = userStringsMap.get(userId);
        }
        sets.add(text);
        userStringsMap.put(userId, sets);
        return "Text updated";
    }

    @GetMapping(value = "/getDto")
    @ResponseBody
    public RequestResponse getDTO(){
        RequestResponse requestResponse = new RequestResponse();
        requestResponse.setMessage("Got DTO");
        requestResponse.setStatus(HttpStatus.OK);
        return requestResponse;
    }
}
