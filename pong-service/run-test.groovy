@Grab(group='org.spockframework', module='spock-core', version='2.3-groovy-4.0')
@Grab(group='org.springframework.boot', module='spring-boot-starter-webflux', version='3.1.5')
@Grab(group='io.projectreactor', module='reactor-test', version='3.5.11')
@Grab(group='io.github.resilience4j', module='resilience4j-ratelimiter', version='2.1.0')

import org.junit.runner.JUnitCore
import org.junit.runner.Result
import com.example.pingpong.pong.controller.PongControllerSpec

Result result = JUnitCore.runClasses(PongControllerSpec)
println "Tests run: ${result.runCount}, Failed: ${result.failureCount}"
result.failures.each { failure ->
    println failure.toString()
}
