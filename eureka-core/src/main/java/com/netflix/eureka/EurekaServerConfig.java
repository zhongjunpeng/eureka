/*
 * Copyright 2012 Netflix, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.netflix.eureka;

import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.netflix.eureka.aws.AwsBindingStrategy;

/**
 * Configuration information required by the eureka server to operate.
 * eureka server 运行所需的配置信息
 *
 * <p>
 * Most of the required information is provided by the default configuration
 * {@link com.netflix.eureka.DefaultEurekaServerConfig}.
 * 大多数所需要的信息都是由默认配置类提供的 com.netflix.eureka.DefaultEurekaServerConfig
 *
 * Note that all configurations are not effective at runtime unless and
 * otherwise specified.
 * 注意，所有的配置类在运行时期都不生效，除非另有说明
 * </p>
 *
 * @author Karthik Ranganathan
 *
 */
public interface EurekaServerConfig {

    /**
     * Gets the <em>AWS Access Id</em>. This is primarily used for
     * <em>Elastic IP Biding</em>. The access id should be provided with
     * appropriate AWS permissions to bind the EIP.
     *
     * @return
     */
    String getAWSAccessId();

    /**
     * Gets the <em>AWS Secret Key</em>. This is primarily used for
     * <em>Elastic IP Biding</em>. The access id should be provided with
     * appropriate AWS permissions to bind the EIP.
     *
     * @return
     */
    String getAWSSecretKey();

    /**
     * Gets the number of times the server should try to bind to the candidate
     * EIP.
     *
     * <p>
     * <em>The changes are effective at runtime.</em>
     * </p>
     *
     * @return the number of times the server should try to bind to the
     *         candidate EIP.
     */
    int getEIPBindRebindRetries();

    /**
     * Get the interval with which the server should check if the EIP is bound
     * and should try to bind in the case if it is already not bound, iff the EIP
     * is not currently bound.
     * <p>
     * <em>The changes are effective at runtime.</em>
     * </p>
     *
     * @return the time in milliseconds.
     */
    int getEIPBindingRetryIntervalMsWhenUnbound();

    /**
     * Gets the interval with which the server should check if the EIP is bound
     * and should try to bind in the case if it is already not bound, iff the EIP
     * is already bound. (so this refresh is just for steady state checks)
     * <p>
     * <em>The changes are effective at runtime.</em>
     * </p>
     *
     * @return the time in milliseconds.
     */
    int getEIPBindingRetryIntervalMs();

    /**
     * Checks to see if the eureka server is enabled for self preservation.
     * 检查eureka服务器是否已启用自我保护。
     *
     * <p>
     * When enabled, the server keeps track of the number of <em>renewals</em>
     * it should receive from the server. Any time, the number of renewals drops
     * below the threshold percentage as defined by
     * {@link #getRenewalPercentThreshold()}, the server turns off expirations
     * to avert danger.This will help the server in maintaining the registry
     * information in case of network problems between client and the server.
     *
     * 当这个配置启用时，server 会追踪收到客户端发送的续约心跳次数。如果在任意时刻，client 发送的续约心跳次数
     * 少于 #getRenewalPercentThreshold() 这个方法定义的阈值也就是这个百分比时，server 会停止过期服务以避免风险。
     * 这样会在eureka server 和client 出现网络问题时，不会删除服务注册表的信息。
     *
     * <p>
     * <em>The changes are effective at runtime.</em>
     * </p>
     *
     * 是否开启自我保护模式。
     * FROM 周立——《理解Eureka的自我保护模式》
     * 当Eureka Server节点在短时间内丢失过多客户端时（可能发生了网络分区故障），那么这个节点就会进入自我保护模式。
     * 一旦进入该模式，Eureka Server就会保护服务注册表中的信息，不再删除服务注册表中的数据（也就是不会注销任何服务节点）。
     * 当网络故障恢复后，该Eureka Server节点会自动退出自我保护模式。
     *
     * @return true to enable self preservation, false otherwise.
     */
    boolean shouldEnableSelfPreservation();

    /**
     * The minimum percentage of renewals that is expected from the clients in
     * the period specified by {@link #getRenewalThresholdUpdateIntervalMs()}.
     * If the renewals drop below the threshold, the expirations are disabled if
     * the {@link #shouldEnableSelfPreservation()} is enabled.
     *
     * 通过 getRenewalThresholdUpdateIntervalMs 方法在指定的一段时间内，期望的续约次数的最小百分比。
     * 如果续约百分比低于阈值，那么 shouldEnableSelfPreservation 这个方法就会生效，而这个服务过期设置就会失效。
     * <p>
     * <em>The changes are effective at runtime.</em>
     * </p>
     *
     * 开启自我保护模式比例，超过低于该比例后开启自我保护模式，默认是85%
     *
     * @return value between 0 and 1 indicating the percentage. For example,
     *         <code>85%</code> will be specified as <code>0.85</code>.
     */
    double getRenewalPercentThreshold();

    /**
     * The interval with which the threshold as specified in
     * {@link #getRenewalPercentThreshold()} needs to be updated.
     *
     * getRenewalPercentThreshold 方法中获取自我保护模式比例的更新周期
     *
     * 自我保护模式比例更新频率，单位：毫秒
     *
     * @return time in milliseconds indicating the interval.
     */
    int getRenewalThresholdUpdateIntervalMs();

    /**
     * The interval with which clients are expected to send their heartbeats. Defaults to 30
     * seconds. If clients send heartbeats with different frequency, say, every 15 seconds, then
     * this parameter should be tuned accordingly, otherwise, self-preservation won't work as
     * expected.
     *
     * client 端发送心跳的周期，默认是 30 秒。如果 client 端需要不同周期去发送心跳，例如每 15 秒 发送一次，
     * 则需要调整此参数，否则，自我保护模式就无法按照预期去执行。
     *
     * @return time in seconds indicating the expected interval (表示预期间隔的时间（以秒为单位）)
     */
    int getExpectedClientRenewalIntervalSeconds();

    /**
     * The interval with which the information about the changes in peer eureka
     * nodes is updated. The user can use the DNS mechanism or dynamic
     * configuration provided by <a href="https://github.com/Netflix/archaius">Archaius</a> to
     * change the information dynamically.
     *
     * 更新对等eureka节点中的更改信息的间隔
     * <p>
     * <em>The changes are effective at runtime.</em>
     * </p>
     *
     * @return timer in milliseconds indicating the interval.
     */
    int getPeerEurekaNodesUpdateIntervalMs();

    /**
     * If set to true, the replicated data send in the request will be always compressed.
     * This does not define response path, which is driven by "Accept-Encoding" header.
     *
     * 如果设置为true，则请求中发送的复制数据将始终被压缩
     */
    boolean shouldEnableReplicatedRequestCompression();

    /**
     * Get the number of times the replication events should be retried with
     * peers.
     * <p>
     * <em>The changes are effective at runtime.</em>
     * </p>
     *
     * 获取应与对等方重试复制事件的次数。
     *
     * @return the number of retries.
     */
    int getNumberOfReplicationRetries();

    /**
     * Gets the interval with which the status information about peer nodes is
     * updated.
     *
     * 获取更新对等节点状态信息的间隔
     * <p>
     * <em>The changes are effective at runtime.</em>
     * </p>
     *
     * @return time in milliseconds indicating the interval.
     */
    int getPeerEurekaStatusRefreshTimeIntervalMs();

    /**
     * Gets the time to wait when the eureka server starts up unable to get
     * instances from peer nodes. It is better not to start serving rightaway
     * during these scenarios as the information that is stored in the registry
     * may not be complete.
     *
     * 获取eureka服务器启动时无法从对等节点获取实例时等待的时间。
     * 最好不要在这些场景中立即开始服务，因为存储在注册表中的信息可能不完整。
     *
     * When the instance registry starts up empty, it builds over time when the
     * clients start to send heartbeats and the server requests the clients for
     * registration information.
     *
     * 当实例注册表启动为空时，当客户端开始发送心跳，服务器请求客户端提供注册信息时，它会随着时间的推移而构建。
     *
     * @return time in milliseconds.
     */
    int getWaitTimeInMsWhenSyncEmpty();

    /**
     * Gets the timeout value for connecting to peer eureka nodes for
     * replication.
     * 获取连接到同等 eureka 节点复制的超时时间。
     * @return timeout value in milliseconds.
     */
    int getPeerNodeConnectTimeoutMs();

    /**
     * Gets the timeout value for reading information from peer eureka nodes for
     * replication.
     *
     * 获取从对等eureka节点读取复制信息的超时值。
     *
     * @return timeout value in milliseconds.
     */
    int getPeerNodeReadTimeoutMs();

    /**
     * Gets the total number of <em>HTTP</em> connections allowed to peer eureka
     * nodes for replication.
     *
     * @return total number of allowed <em>HTTP</em> connections.
     */
    int getPeerNodeTotalConnections();

    /**
     * Gets the total number of <em>HTTP</em> connections allowed to a
     * particular peer eureka node for replication.
     *
     * @return total number of allowed <em>HTTP</em> connections for a peer
     *         node.
     */
    int getPeerNodeTotalConnectionsPerHost();

    /**
     * Gets the idle time after which the <em>HTTP</em> connection should be
     * cleaned up.
     *
     * @return idle time in seconds.
     */
    int getPeerNodeConnectionIdleTimeoutSeconds();

    /**
     * Get the time for which the delta information should be cached for the
     * clients to retrieve the value without missing it.
     *
     * 获取应该缓存增量信息的时间，以便客户端在不丢失值的情况下检索该值
     *
     * @return time in milliseconds
     */
    long getRetentionTimeInMSInDeltaQueue();

    /**
     * Get the time interval with which the clean up task should wake up and
     * check for expired delta information.
     *
     * 获取清理任务应唤醒并检查过期增量信息的时间间隔
     *
     * @return time in milliseconds.
     */
    long getDeltaRetentionTimerIntervalInMs();

    /**
     * Get the time interval with which the task that expires instances should
     * wake up and run.
     *
     * 获取过期实例的任务应该唤醒并运行的时间间隔。
     *
     * @return time in milliseconds.
     */
    long getEvictionIntervalTimerInMs();

    /**
     * Whether to use AWS API to query ASG statuses.
     *
     * @return true if AWS API is used, false otherwise.
     */
    boolean shouldUseAwsAsgApi();

    /**
     * Get the timeout value for querying the <em>AWS</em> for <em>ASG</em>
     * information.
     *
     * @return timeout value in milliseconds.
     */
    int getASGQueryTimeoutMs();

    /**
     * Get the time interval with which the <em>ASG</em> information must be
     * queried from <em>AWS</em>.
     *
     * @return time in milliseconds.
     */
    long getASGUpdateIntervalMs();

    /**
     * Get the expiration value for the cached <em>ASG</em> information
     *
     * @return time in milliseconds.
     */
    long getASGCacheExpiryTimeoutMs();

    /**
     * Gets the time for which the registry payload should be kept in the cache
     * if it is not invalidated by change events.
     *
     * @return time in seconds.
     */
    long getResponseCacheAutoExpirationInSeconds();

    /**
     * Gets the time interval with which the payload cache of the client should
     * be updated.
     *
     * @return time in milliseconds.
     */
    long getResponseCacheUpdateIntervalMs();

    /**
     * The {@link com.netflix.eureka.registry.ResponseCache} currently uses a two level caching
     * strategy to responses. A readWrite cache with an expiration policy, and a readonly cache
     * that caches without expiry.
     *
     * 否开启只读请求响应缓存。
     * 响应缓存 ( ResponseCache ) 机制目前使用两层缓存策略。
     * 优先读取永不过期的只读缓存，读取不到后读取固定过期的读写缓存。
     *
     * @return true if the read only cache is to be used
     */
    boolean shouldUseReadOnlyResponseCache();

    /**
     * Checks to see if the delta information can be served to client or not.
     * <p>
     * <em>The changes are effective at runtime.</em>
     * </p>
     *
     * @return true if the delta information is allowed to be served, false
     *         otherwise.
     */
    boolean shouldDisableDelta();

    /**
     * Get the idle time for which the status replication threads can stay
     * alive.
     *
     * @return time in minutes.
     */
    long getMaxIdleThreadInMinutesAgeForStatusReplication();

    /**
     * Get the minimum number of threads to be used for status replication.
     *
     * @return minimum number of threads to be used for status replication.
     */
    int getMinThreadsForStatusReplication();

    /**
     * Get the maximum number of threads to be used for status replication.
     *
     * @return maximum number of threads to be used for status replication.
     */
    int getMaxThreadsForStatusReplication();

    /**
     * Get the maximum number of replication events that can be allowed to back
     * up in the status replication pool.
     * <p>
     * Depending on the memory allowed, timeout and the replication traffic,
     * this value can vary.
     * </p>
     *
     * @return the maximum number of replication events that can be allowed to
     *         back up.
     */
    int getMaxElementsInStatusReplicationPool();

    /**
     * Checks whether to synchronize instances when timestamp differs.
     * <p>
     * <em>The changes are effective at runtime.</em>
     * </p>
     *
     * @return true, to synchronize, false otherwise.
     */
    boolean shouldSyncWhenTimestampDiffers();

    /**
     * Get the number of times that a eureka node would try to get the registry
     * information from the peers during startup.
     *
     * @return the number of retries
     */
    int getRegistrySyncRetries();

    /**
     * Get the wait/sleep time between each retry sync attempts, if the prev retry failed and there are
     * more retries to attempt.
     *
     * @return the wait time in ms between each sync retries
     */
    long getRegistrySyncRetryWaitMs();

    /**
     * Get the maximum number of replication events that can be allowed to back
     * up in the replication pool. This replication pool is responsible for all
     * events except status updates.
     * <p>
     * Depending on the memory allowed, timeout and the replication traffic,
     * this value can vary.
     * </p>
     *
     * @return the maximum number of replication events that can be allowed to
     *         back up.
     */
    int getMaxElementsInPeerReplicationPool();

    /**
     * Get the idle time for which the replication threads can stay alive.
     *
     * @return time in minutes.
     */
    long getMaxIdleThreadAgeInMinutesForPeerReplication();

    /**
     * Get the minimum number of threads to be used for replication.
     *
     * @return minimum number of threads to be used for replication.
     */
    int getMinThreadsForPeerReplication();

    /**
     * Get the maximum number of threads to be used for replication.
     *
     * @return maximum number of threads to be used for replication.
     */
    int getMaxThreadsForPeerReplication();

    /**
     * Get the minimum number of available peer replication instances
     * for this instance to be considered healthy. The design of eureka allows
     * for an instance to continue operating with zero peers, but that would not
     * be ideal.
     * <p>
     * The default value of -1 is interpreted as a marker to not compare
     * the number of replicas. This would be done to either disable this check
     * or to run eureka in a single node configuration.
     *
     * @return minimum number of available peer replication instances
     *         for this instance to be considered healthy.
     */
    int getHealthStatusMinNumberOfAvailablePeers();

    /**
     * Get the time in milliseconds to try to replicate before dropping
     * replication events.
     *
     * @return time in milliseconds
     */
    int getMaxTimeForReplication();

    /**
     * Checks whether the connections to replicas should be primed. In AWS, the
     * firewall requires sometime to establish network connection for new nodes.
     *
     * @return true, if connections should be primed, false otherwise.
     */
    boolean shouldPrimeAwsReplicaConnections();

    /**
     * Checks to see if the delta information can be served to client or not for
     * remote regions.
     * <p>
     * <em>The changes are effective at runtime.</em>
     * </p>
     *
     * @return true if the delta information is allowed to be served, false
     *         otherwise.
     */
    boolean shouldDisableDeltaForRemoteRegions();

    /**
     * Gets the timeout value for connecting to peer eureka nodes for remote
     * regions.
     *
     * @return timeout value in milliseconds.
     */
    int getRemoteRegionConnectTimeoutMs();

    /**
     * Gets the timeout value for reading information from peer eureka nodes for
     * remote regions.
     *
     * @return timeout value in milliseconds.
     */
    int getRemoteRegionReadTimeoutMs();

    /**
     * Gets the total number of <em>HTTP</em> connections allowed to peer eureka
     * nodes for remote regions.
     *
     * @return total number of allowed <em>HTTP</em> connections.
     */

    int getRemoteRegionTotalConnections();

    /**
     * Gets the total number of <em>HTTP</em> connections allowed to a
     * particular peer eureka node for remote regions.
     *
     * @return total number of allowed <em>HTTP</em> connections for a peer
     *         node.
     */
    int getRemoteRegionTotalConnectionsPerHost();

    /**
     * Gets the idle time after which the <em>HTTP</em> connection should be
     * cleaned up for remote regions.
     *
     * @return idle time in seconds.
     */
    int getRemoteRegionConnectionIdleTimeoutSeconds();

    /**
     * Indicates whether the content fetched from eureka server has to be
     * compressed for remote regions whenever it is supported by the server. The
     * registry information from the eureka server is compressed for optimum
     * network traffic.
     *
     * @return true, if the content need to be compressed, false otherwise.
     */
    boolean shouldGZipContentFromRemoteRegion();

    /**
     * Get a map of region name against remote region discovery url.
     *
     * @return - An unmodifiable map of remote region name against remote region discovery url. Empty map if no remote
     * region url is defined.
     */
    Map<String, String> getRemoteRegionUrlsWithName();

    /**
     * Get the list of remote region urls.
     * @return - array of string representing {@link java.net.URL}s.
     * @deprecated Use {@link #getRemoteRegionUrlsWithName()}
     */
    String[] getRemoteRegionUrls();

    /**
     * Returns a list of applications that must be retrieved from the passed remote region. <br/>
     * This list can be <code>null</code> which means that no filtering should be applied on the applications
     * for this region i.e. all applications must be returned. <br/>
     * A global whitelist can also be configured which can be used when no setting is available for a region, such a
     * whitelist can be obtained by passing <code>null</code> to this method.
     *
     * @param regionName Name of the region for which the application whitelist is to be retrieved. If null a global
     *                   setting is returned.
     *
     * @return A set of application names which must be retrieved from the passed region. If <code>null</code> all
     * applications must be retrieved.
     */
    @Nullable
    Set<String> getRemoteRegionAppWhitelist(@Nullable String regionName);

    /**
     * Get the time interval for which the registry information need to be fetched from the remote region.
     * @return time in seconds.
     */
    int getRemoteRegionRegistryFetchInterval();

    /**
     * Size of a thread pool used to execute remote region registry fetch requests. Delegating these requests
     * to internal threads is necessary workaround to https://bugs.openjdk.java.net/browse/JDK-8049846 bug.
     */
    int getRemoteRegionFetchThreadPoolSize();

    /**
     * Gets the fully qualified trust store file that will be used for remote region registry fetches.
     * @return
     */
    String getRemoteRegionTrustStore();

    /**
     * Get the remote region trust store's password.
     */
    String getRemoteRegionTrustStorePassword();

    /**
     * Old behavior of fallback to applications in the remote region (if configured) if there are no instances of that
     * application in the local region, will be disabled.
     *
     * @return {@code true} if the old behavior is to be disabled.
     */
    boolean disableTransparentFallbackToOtherRegion();

    /**
     * Indicates whether the replication between cluster nodes should be batched for network efficiency.
     * @return {@code true} if the replication needs to be batched.
     */
    boolean shouldBatchReplication();

    /**
     * Allows to configure URL which Eureka should treat as its own during replication. In some cases Eureka URLs don't
     * match IP address or hostname (for example, when nodes are behind load balancers). Setting this parameter on each
     * node to URLs of associated load balancers helps to avoid replication to the same node where event originally came
     * to. Important: you need to configure the whole URL including scheme and path, like
     * <code>http://eureka-node1.mydomain.com:8010/eureka/v2/</code>
     * @return URL Eureka will treat as its own
     */
    String getMyUrl();

    /**
     * Indicates whether the eureka server should log/metric clientAuthHeaders
     * @return {@code true} if the clientAuthHeaders should be logged and/or emitted as metrics
     */
    boolean shouldLogIdentityHeaders();

    /**
     * Indicates whether the rate limiter should be enabled or disabled.
     */
    boolean isRateLimiterEnabled();

    /**
     * Indicate if rate limit standard clients. If set to false, only non standard clients
     * will be rate limited.
     */
    boolean isRateLimiterThrottleStandardClients();

    /**
     * A list of certified clients. This is in addition to standard eureka Java clients.
     */
    Set<String> getRateLimiterPrivilegedClients();

    /**
     * Rate limiter, token bucket algorithm property. See also {@link #getRateLimiterRegistryFetchAverageRate()}
     * and {@link #getRateLimiterFullFetchAverageRate()}.
     */
    int getRateLimiterBurstSize();

    /**
     * Rate limiter, token bucket algorithm property. Specifies the average enforced request rate.
     * See also {@link #getRateLimiterBurstSize()}.
     */
    int getRateLimiterRegistryFetchAverageRate();

    /**
     * Rate limiter, token bucket algorithm property. Specifies the average enforced request rate.
     * See also {@link #getRateLimiterBurstSize()}.
     */
    int getRateLimiterFullFetchAverageRate();

    /**
     * Name of the Role used to describe auto scaling groups from third AWS accounts.
     */
    String getListAutoScalingGroupsRoleName();

    /**
     * @return the class name of the full json codec to use for the server. If none set a default codec will be used
     */
    String getJsonCodecName();

    /**
     * @return the class name of the full xml codec to use for the server. If none set a default codec will be used
     */
    String getXmlCodecName();

    /**
     * Get the configured binding strategy EIP or Route53.
     * @return the configured binding strategy
     */
    AwsBindingStrategy getBindingStrategy();

    /**
     *
     * @return the ttl used to set up the route53 domain if new
     */
    long getRoute53DomainTTL();

    /**
     * Gets the number of times the server should try to bind to the candidate
     * Route53 domain.
     *
     * <p>
     * <em>The changes are effective at runtime.</em>
     * </p>
     *
     * @return the number of times the server should try to bind to the
     *         candidate Route53 domain.
     */
    int getRoute53BindRebindRetries();

    /**
     * Gets the interval with which the server should check if the Route53 domain is bound
     * and should try to bind in the case if it is already not bound.
     * <p>
     * <em>The changes are effective at runtime.</em>
     * </p>
     *
     * @return the time in milliseconds.
     */
    int getRoute53BindingRetryIntervalMs();

    /**
     * To avoid configuration API pollution when trying new/experimental or features or for the migration process,
     * the corresponding configuration can be put into experimental configuration section.
     *
     * @return a property of experimental feature
     */
    String getExperimental(String name);

    /**
     * Get the capacity of responseCache, default value is 1000.
     *
     * @return the capacity of responseCache.
     */
    int getInitialCapacityOfResponseCache();
}
