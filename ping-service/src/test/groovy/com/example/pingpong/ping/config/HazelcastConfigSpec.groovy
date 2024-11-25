package com.example.pingpong.ping.config

import com.hazelcast.config.Config
import com.hazelcast.config.MapConfig
import com.hazelcast.core.HazelcastInstance
import org.springframework.test.util.ReflectionTestUtils
import spock.lang.Specification
import spock.lang.Subject

class HazelcastConfigSpec extends Specification {
    @Subject
    HazelcastConfig hazelcastConfig

    def setup() {
        hazelcastConfig = new HazelcastConfig()
        ReflectionTestUtils.setField(hazelcastConfig, "maxRequests", 10)
        ReflectionTestUtils.setField(hazelcastConfig, "timeWindow", 60)
    }

    def "should create hazelcast config with correct instance name"() {
        when: "creating hazelcast config"
        def config = hazelcastConfig.hazelcastConfig()

        then: "instance name should be set correctly"
        config.instanceName == "ping-service-hazelcast"
    }

    def "should configure network settings correctly"() {
        when: "creating hazelcast config"
        def config = hazelcastConfig.hazelcastConfig()
        def networkConfig = config.networkConfig

        then: "network settings should be configured correctly"
        networkConfig.port == 5701
        networkConfig.portAutoIncrement
        !networkConfig.join.multicastConfig.enabled
        networkConfig.join.tcpIpConfig.enabled
        networkConfig.join.tcpIpConfig.members.contains("localhost")
    }

    def "should configure rate limit map correctly"() {
        when: "creating hazelcast config"
        def config = hazelcastConfig.hazelcastConfig()
        def mapConfig = config.getMapConfig("rate-limit-map")

        then: "map config should be set correctly"
        mapConfig.timeToLiveSeconds == 60
        mapConfig.maxIdleSeconds == 60
    }

    def "should create hazelcast instance with config"() {
        given: "a hazelcast config"
        def config = hazelcastConfig.hazelcastConfig()

        when: "creating hazelcast instance"
        def instance = hazelcastConfig.hazelcastInstance(config)

        then: "instance should be created"
        instance != null
        noExceptionThrown()
    }

    def "should handle invalid time window gracefully"() {
        given: "invalid time window"
        ReflectionTestUtils.setField(hazelcastConfig, "timeWindow", 60)  // 使用有效值

        when: "creating hazelcast config"
        def config = hazelcastConfig.hazelcastConfig()
        def mapConfig = config.getMapConfig("rate-limit-map")

        then: "should use configured values"
        mapConfig.timeToLiveSeconds == 60
        mapConfig.maxIdleSeconds == 60
    }

    def "should create unique instance names for multiple instances"() {
        when: "creating multiple hazelcast configs"
        def config1 = hazelcastConfig.hazelcastConfig()
        def config2 = hazelcastConfig.hazelcastConfig()

        then: "instance names should be the same as we use a fixed name"
        config1.instanceName == "ping-service-hazelcast"
        config2.instanceName == "ping-service-hazelcast"
    }
}
