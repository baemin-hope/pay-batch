package khope.paybatch.jobs.pay

import khope.paybatch.domain.pay.Pay
import khope.paybatch.jobs.pay.PayPagingFailJobConfiguration.Companion.JOB_NAME
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.database.JpaItemWriter
import org.springframework.batch.item.database.JpaPagingItemReader
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
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
        const val JOB_NAME = "payPagingFail"
        const val CHUNK_SIZE = 10
    }

    @Bean(JOB_NAME)
    fun payPagingFailJob(): Job {
        return jobBuilderFactory.get(JOB_NAME + "Job")
            .start(payPagingStep())
            .build()
    }

    @Bean
    @JobScope
    fun payPagingStep(): Step {
        return stepBuilderFactory.get(JOB_NAME + "Step")
            .chunk<Pay, Pay>(CHUNK_SIZE)
            .reader(payPagingReader())
            .processor(payPagingProcessor())
            .writer(payPagingWriter())
            .build()
    }

    @Bean
    @StepScope
    fun payPagingReader(): JpaPagingItemReader<Pay> {
        return JpaPagingItemReaderBuilder<Pay>()
            .pageSize(CHUNK_SIZE)
            .queryString("SELECT p FROM Pay p WHERE p.isSuccess = false")
            .entityManagerFactory(entityManagerFactory)
            .name(JOB_NAME + "Reader")
            .build()
    }

    @Bean
    @StepScope
    fun payPagingProcessor(): ItemProcessor<Pay, Pay> = ItemProcessor { newSuccessPay(it) }

    @Bean
    @StepScope
    fun payPagingWriter(): JpaItemWriter<Pay> {
        val writer = JpaItemWriter<Pay>()
        writer.setEntityManagerFactory(entityManagerFactory)
        return writer
    }

    private fun newSuccessPay(pay: Pay): Pay{
        return Pay(
            id = pay.id,
            amount = pay.amount,
            order = pay.order,
            isSuccess = true
        )
    }

}