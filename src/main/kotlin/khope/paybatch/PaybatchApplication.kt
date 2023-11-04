package khope.paybatch

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableBatchProcessing
class PaybatchApplication

fun main(args: Array<String>) {
    runApplication<PaybatchApplication>(*args)
}
