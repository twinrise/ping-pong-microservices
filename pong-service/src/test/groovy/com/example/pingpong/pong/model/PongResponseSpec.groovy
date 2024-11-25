package com.example.pingpong.pong.model

import spock.lang.Specification

import java.time.LocalDateTime

class PongResponseSpec extends Specification {
    def "should build PongResponse with all fields"() {
        given:
        def message = "test message"
        def instanceId = "test-instance"
        def time = LocalDateTime.now()
        def status = "SUCCESS"

        when:
        def response = PongResponse.builder()
            .message(message)
            .pongInstanceId(instanceId)
            .responseTime(time)
            .status(status)
            .build()

        then:
        response.message == message
        response.pongInstanceId == instanceId
        response.responseTime == time
        response.status == status
    }

    def "should build PongResponse with minimal fields"() {
        when:
        def response = PongResponse.builder()
            .message("test")
            .build()

        then:
        response.message == "test"
        response.pongInstanceId == null
        response.responseTime == null
        response.status == null
    }

    def "builder should be chainable"() {
        when:
        def builder = PongResponse.builder()
            .message("test")
            .status("SUCCESS")

        then:
        builder instanceof PongResponse.Builder

        when:
        def response = builder
            .pongInstanceId("test-id")
            .responseTime(LocalDateTime.now())
            .build()

        then:
        response instanceof PongResponse
        response.message == "test"
        response.status == "SUCCESS"
        response.pongInstanceId == "test-id"
    }
}
