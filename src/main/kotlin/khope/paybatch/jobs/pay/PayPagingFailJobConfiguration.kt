package khope.paybatch.jobs.pay

import khope.paybatch.jobs.pay.PayPagingFailJobConfiguration.Companion.JOB_NAME
import org.slf4j.LoggerFactory
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import javax.persistence.EntityManagerFactory


@Configuration
@ConditionalOnProperty(name = ["job.name"], havingValue = JOB_NAME)
class PayPagingFailJobConfiguration(
    private val entityManagerFactory: EntityManagerFactory,
    private val stepBuilderFactory: StepBuilderFactory,
    private val jobBuilderFactory: JobBuilderFactory
) {

    private val log = LoggerFactory.getLogger(javaClass.simpleName)

    companion object {
        const val JOB_NAME = "payPagingFailJob"
        const val CHUNK_SIZE = 10
    }

}