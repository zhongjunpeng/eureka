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

package com.netflix.appinfo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Represents the <em>lease</em> information with <em>Eureka</em>.
 * 代表 Eureka 的契约信息
 *
 * <p>
 * <em>Eureka</em> decides to remove the instance out of its view depending on
 * the duration that is set in
 * {@link EurekaInstanceConfig#getLeaseExpirationDurationInSeconds()} which is
 * held in this lease. The lease also tracks the last time it was renewed.
 * </p>
 * Eureka 决定移除这个实例取决于 在 getLeaseExpirationDurationInSeconds() 设置的值，这个契约依然保留着上次续约的时间
 *
 * @author Karthik Ranganathan, Greg Kim
 *
 */
@JsonRootName("leaseInfo")
public class LeaseInfo {

    // 默认心跳续约的周期是 30 秒
    public static final int DEFAULT_LEASE_RENEWAL_INTERVAL = 30;
    // 默认维持续约的时间是 90 秒
    public static final int DEFAULT_LEASE_DURATION = 90;

    // Client settings
    private int renewalIntervalInSecs = DEFAULT_LEASE_RENEWAL_INTERVAL;
    private int durationInSecs = DEFAULT_LEASE_DURATION;

    // Server populated
    private long registrationTimestamp;
    private long lastRenewalTimestamp;
    private long evictionTimestamp;
    private long serviceUpTimestamp;

    public static final class Builder {

        @XStreamOmitField
        private LeaseInfo result;

        private Builder() {
            result = new LeaseInfo();
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        /**
         * Sets the registration timestamp.
         * 设置注册时的时间戳
         *
         * @param ts
         *            time when the lease was first registered.
         * @return the {@link LeaseInfo} builder.
         */
        public Builder setRegistrationTimestamp(long ts) {
            result.registrationTimestamp = ts;
            return this;
        }

        /**
         * Sets the last renewal timestamp of lease.
         * 记录最后一次续约的时间
         *
         * @param ts
         *            time when the lease was last renewed.
         * @return the {@link LeaseInfo} builder.
         */
        public Builder setRenewalTimestamp(long ts) {
            result.lastRenewalTimestamp = ts;
            return this;
        }

        /**
         * Sets the de-registration timestamp.
         * 记录被移除的时间戳
         *
         * @param ts
         *            time when the lease was removed.
         * @return the {@link LeaseInfo} builder.
         */
        public Builder setEvictionTimestamp(long ts) {
            result.evictionTimestamp = ts;
            return this;
        }

        /**
         * Sets the service UP timestamp.
         * 记录服务上线的时间戳
         *
         * @param ts
         *            time when the leased service marked as UP.
         * @return the {@link LeaseInfo} builder.
         */
        public Builder setServiceUpTimestamp(long ts) {
            result.serviceUpTimestamp = ts;
            return this;
        }

        /**
         * Sets the client specified setting for eviction (e.g. how long to wait
         * without renewal event).
         * 记录 client 端指定多久时间会被剔除，也就是等待多长时间没有续约
         *
         * @param d
         *            time in seconds after which the lease would expire without
         *            renewa.
         *            用秒来表示多久没有续约，这个契约就会过期了
         * @return the {@link LeaseInfo} builder.
         */
        public Builder setDurationInSecs(int d) {
            if (d <= 0) {
                result.durationInSecs = DEFAULT_LEASE_DURATION;
            } else {
                result.durationInSecs = d;
            }
            return this;
        }

        /**
         * Sets the client specified setting for renew interval.
         * 设置 client 端指定的续约周期
         * @param i
         *            the time interval with which the renewals will be renewed.
         * @return the {@link LeaseInfo} builder.
         */
        public Builder setRenewalIntervalInSecs(int i) {
            if (i <= 0) {
                result.renewalIntervalInSecs = DEFAULT_LEASE_RENEWAL_INTERVAL;
            } else {
                result.renewalIntervalInSecs = i;
            }
            return this;
        }

        /**
         * Build the {@link InstanceInfo}.
         *
         * @return the {@link LeaseInfo} information built based on the supplied
         *         information.
         */
        public LeaseInfo build() {
            return result;
        }
    }

    private LeaseInfo() {
    }

    /**
     * TODO: note about renewalTimestamp legacy:
     * The previous change to use Jackson ser/deser changed the field name for lastRenewalTimestamp to renewalTimestamp
     * for serialization, which causes an incompatibility with the jacksonNG codec when the server returns data with
     * field renewalTimestamp and jacksonNG expects lastRenewalTimestamp. Remove this legacy field from client code
     * in a few releases (once servers are updated to a release that generates json with the correct
     * lastRenewalTimestamp).
     */
    @JsonCreator
    public LeaseInfo(@JsonProperty("renewalIntervalInSecs") int renewalIntervalInSecs,
                     @JsonProperty("durationInSecs") int durationInSecs,
                     @JsonProperty("registrationTimestamp") long registrationTimestamp,
                     @JsonProperty("lastRenewalTimestamp") Long lastRenewalTimestamp,
                     @JsonProperty("renewalTimestamp") long lastRenewalTimestampLegacy,  // for legacy
                     @JsonProperty("evictionTimestamp") long evictionTimestamp,
                     @JsonProperty("serviceUpTimestamp") long serviceUpTimestamp) {
        this.renewalIntervalInSecs = renewalIntervalInSecs;
        this.durationInSecs = durationInSecs;
        this.registrationTimestamp = registrationTimestamp;
        this.evictionTimestamp = evictionTimestamp;
        this.serviceUpTimestamp = serviceUpTimestamp;

        if (lastRenewalTimestamp == null) {
            this.lastRenewalTimestamp = lastRenewalTimestampLegacy;
        } else {
            this.lastRenewalTimestamp = lastRenewalTimestamp;
        }
    }

    /**
     * Returns the registration timestamp.
     * 返回注册时间戳
     *
     * @return time in milliseconds since epoch.
     */
    public long getRegistrationTimestamp() {
        return registrationTimestamp;
    }

    /**
     * Returns the last renewal timestamp of lease.
     *
     * @return time in milliseconds since epoch.
     */
    @JsonProperty("lastRenewalTimestamp")
    public long getRenewalTimestamp() {
        return lastRenewalTimestamp;
    }

    /**
     * Returns the de-registration timestamp.
     *
     * @return time in milliseconds since epoch.
     */
    public long getEvictionTimestamp() {
        return evictionTimestamp;
    }

    /**
     * Returns the service UP timestamp.
     *
     * @return time in milliseconds since epoch.
     */
    public long getServiceUpTimestamp() {
        return serviceUpTimestamp;
    }

    /**
     * Returns client specified setting for renew interval.
     *
     * @return time in milliseconds since epoch.
     */
    public int getRenewalIntervalInSecs() {
        return renewalIntervalInSecs;
    }

    /**
     * Returns client specified setting for eviction (e.g. how long to wait w/o
     * renewal event)
     *
     * @return time in milliseconds since epoch.
     */
    public int getDurationInSecs() {
        return durationInSecs;
    }

}
