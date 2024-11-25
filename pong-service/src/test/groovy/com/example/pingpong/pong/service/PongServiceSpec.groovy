package com.example.pingpong.pong.service

import com.example.pingpong.pong.model.PongResponse
import reactor.test.StepVerifier
import spock.lang.Specification

class PongServiceSpec extends Specification {
    PongService pongService

    def setup() {
        pongService = new PongService("test-service")
    }

    def "should create pong response with correct fields"() {
        given:
        def message = "Hello"

        when:
        def result = pongService.createPongResponse(message)

        then:
        StepVerifier.create(result)
            .expectNextMatches { response ->
                response.message == "World!" &&
                response.status == "SUCCESS" &&
                response.pongInstanceId.startsWith("test-service-") &&
                response.responseTime != null
            }
            .verifyComplete()
    }

    def "should create unique instance id for each service instance"() {
        given:
        def service1 = new PongService("test-service")
        def service2 = new PongService("test-service")

        when:
        def response1 = service1.createPongResponse("test")
        def response2 = service2.createPongResponse("test")

        then:
        StepVerifier.create(response1)
            .expectNextMatches { r1 ->
                StepVerifier.create(response2)
                    .expectNextMatches { r2 ->
                        r1.pongInstanceId != r2.pongInstanceId
                    }
                    .verifyComplete()
                true
            }
            .verifyComplete()
    }
}
