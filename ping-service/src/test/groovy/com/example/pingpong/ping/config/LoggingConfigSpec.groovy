package com.example.pingpong.ping.config

import org.springframework.test.util.ReflectionTestUtils
import spock.lang.Specification
import spock.lang.Subject

class LoggingConfigSpec extends Specification {
    @Subject
    LoggingConfig loggingConfig
    
    File logsDir

    def setup() {
        loggingConfig = new LoggingConfig()
        ReflectionTestUtils.setField(loggingConfig, "serviceName", "ping-service")
        logsDir = new File("logs")
    }

    def "should create logs directory if it doesn't exist"() {
        given: "a non-existent logs directory"
        if (logsDir.exists()) {
            logsDir.deleteDir()
        }
        assert !logsDir.exists()

        when: "initializing logging config"
        loggingConfig.init()

        then: "logs directory should be created"
        logsDir.exists()
        logsDir.isDirectory()
    }

    def "should handle existing logs directory"() {
        given: "an existing logs directory"
        logsDir.mkdirs()
        assert logsDir.exists()

        when: "initializing logging config"
        loggingConfig.init()

        then: "logs directory should still exist"
        logsDir.exists()
        logsDir.isDirectory()
    }

    def "should log service information"() {
        when: "initializing logging config"
        loggingConfig.init()

        then: "service information should be logged"
        noExceptionThrown()
    }

    def cleanup() {
        // 清理测试目录
        if (logsDir.exists()) {
            logsDir.deleteDir()
        }
    }
}
