package com.example.DemoRedisRequestCachingNEviction.cache;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Configuration
@EnableCaching
public class RedisBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisBean.class);

    RedisConfig redisConfig;
    LettuceConnectionFactory sharedCoreLettuceConnectionFactory;

    @Autowired
    public RedisBean(RedisConfig redisConfig){
        this.redisConfig = redisConfig;
    }

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .disableCachingNullValues();
    }
    @Bean
    public RedisCacheManager cabCacheManager() {
        return RedisCacheManager.builder(sharedCoreLettuceConnectionFactory())
            .withCacheConfiguration("test_ab", cacheConfiguration())
            .transactionAware()
            .build();
    }

    @Bean
    LettuceConnectionFactory sharedCoreLettuceConnectionFactory() {
        if (sharedCoreLettuceConnectionFactory == null) {
            sharedCoreLettuceConnectionFactory = createConnectionFactory(redisConfig);
        }
        return sharedCoreLettuceConnectionFactory;
    }

    static LettuceConnectionFactory createConnectionFactory(final RedisConfig redisConfig) {
        final List<RedisNode> redisNodeList = getRedisNodes(redisConfig.getAddress());
        if (redisNodeList.isEmpty()) {
            throw new IllegalArgumentException("redis.cache.cluster.address configuration value must be defined.");
        }

        final LettuceConnectionFactory lcf = redisConfig.isClustered() ?
                clusteredConfiguration(redisConfig, redisNodeList) :
                standaloneConfiguration(redisConfig, redisNodeList);
        LOGGER.info("Connecting to redis using ssl: {}, at host {}, port {}, database {}", lcf.isUseSsl(), lcf.getHostName(), lcf.getPort(), lcf.getDatabase());
        return lcf;
    }

    static LettuceConnectionFactory standaloneConfiguration(final RedisConfig redisConfig, final List<RedisNode> redisNodeList) {
        final LettuceClientConfiguration.LettuceClientConfigurationBuilder lettuceClientConfigurationBuilder =
                LettuceClientConfiguration.builder();
        if (redisConfig.isWithSSL()) {
            lettuceClientConfigurationBuilder.useSsl().disablePeerVerification();
        }
        lettuceClientConfigurationBuilder
//                .clientName(Utils.getHostname() + "hardening.backend")  // Not supported on GCP memorystore - https://cloud.google.com/memorystore/docs/redis/product-constraints
                .commandTimeout(Duration.of(redisConfig.getTimeout(), ChronoUnit.MILLIS))
                .clientOptions(ClientOptions.builder()
                        .autoReconnect(true)
                        .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                        .build())
                .build();
        final RedisNode redisNode = redisNodeList.get(0);
        final RedisStandaloneConfiguration redisStandAloneConfiguration = new RedisStandaloneConfiguration(Objects.requireNonNull(redisNode.getHost()), Objects.requireNonNull(redisNode.getPort()));
        redisStandAloneConfiguration.setPassword(RedisPassword.of(redisConfig.getPassword()));
        return new LettuceConnectionFactory(redisStandAloneConfiguration, lettuceClientConfigurationBuilder.build());
    }

    static LettuceConnectionFactory clusteredConfiguration(final RedisConfig redisConfig, final List<RedisNode> redisNodeList) {
        final GenericObjectPoolConfig<Object> genericObjectPoolConfig = new GenericObjectPoolConfig<>();
        genericObjectPoolConfig.setMaxTotal(20);
        genericObjectPoolConfig.setMaxIdle(10);
        genericObjectPoolConfig.setMinIdle(4);
        final LettucePoolingClientConfiguration.LettucePoolingClientConfigurationBuilder lettucePoolingClientConfigurationBuilder =
                LettucePoolingClientConfiguration.builder();
        if (redisConfig.isWithSSL()) {
            lettucePoolingClientConfigurationBuilder.useSsl().disablePeerVerification();
        }
        lettucePoolingClientConfigurationBuilder
//                .clientName(Utils.getHostname() + "hardening.backend")  // Not supported on GCP memorystore - https://cloud.google.com/memorystore/docs/redis/product-constraints
                .commandTimeout(Duration.of(redisConfig.getTimeout(), ChronoUnit.MILLIS))
                .clientOptions(ClusterClientOptions.builder()
                        .autoReconnect(true)
                        .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                        .topologyRefreshOptions(ClusterTopologyRefreshOptions.builder()
                                .enableAllAdaptiveRefreshTriggers()
                                .enablePeriodicRefresh()
                                .build())
                        .build())
                .poolConfig(genericObjectPoolConfig)
                .build();
        final RedisClusterConfiguration redisClusterConfiguration = new RedisClusterConfiguration();
        redisClusterConfiguration.setClusterNodes(redisNodeList);
        redisClusterConfiguration.setPassword(RedisPassword.of(redisConfig.getPassword()));
        return new LettuceConnectionFactory(redisClusterConfiguration, lettucePoolingClientConfigurationBuilder.build());
    }

    static List<RedisNode> getRedisNodes(final String redisAddress) {
        if (StringUtils.isEmpty(redisAddress)) {
            return Collections.emptyList();
        }
        final List<RedisNode> redisNodes = new LinkedList<>();
        final String[] addresses = StringUtils.split(redisAddress, ",;");
        for (final String address : addresses) {
            final String[] addressParts = StringUtils.split(address, ":", 2);
            final String host = addressParts[0];
            final int port = NumberUtils.toInt(addressParts[1], 6379);
            final RedisNode.RedisNodeBuilder builder = new RedisNode.RedisNodeBuilder().listeningAt(host, port);
            redisNodes.add(builder.build());
        }
        return redisNodes;
    }
}
