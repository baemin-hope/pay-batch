package khope.paybatch.jobs.pay

import khope.paybatch.domain.pay.Pay
import khope.paybatch.jobs.pay.PayPagingFailJobConfiguration.Companion.JOB_NAME
import org.slf4j.LoggerFactory
import org.springframework.batch.core.*
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.database.JpaItemWriter
import org.springframework.batch.item.database.JpaPagingItemReader
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import javax.persistence.EntityManagerFactory


@Configuration
@ConditionalOnProperty(name = ["job.name"], havingValue = JOB_NAME)
class PayPagingFailJobConfiguration(
    private val entityManagerFactory: EntityManagerFactory,
    private val stepBuilderFactory: StepBuilderFactory,
    private val jobBuilderFactory: JobBuilderFactory,
) {

    private val log = LoggerFactory.getLogger(javaClass.simpleName)

    companion object {
        const val JOB_NAME = "payPagingFail"
        const val CHUNK_SIZE = 10
    }

    @Bean
    @JobScope
    fun parameter(
        @Value("#{jobParameters[VERSION]}") version: Int,
        @Value("#{jobParameters[SHOP]}") shop: String
    ): PayPagingFailJobParameter = PayPagingFailJobParameter(version, shop)


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
            .listener(object: StepExecutionListener {
                override fun beforeStep(stepExecution: StepExecution) {
                    log.info("before Step payPagingStep")
                }

                override fun afterStep(stepExecution: StepExecution): ExitStatus? {
                    val exitCode = stepExecution.exitStatus.exitCode
                    log.info("afterStep payPagingStep - Status {}", exitCode)
                    if(exitCode == ExitStatus.FAILED.exitCode) {
                        log.error("payPagingStep FAILED!!")
                    }
                    return null
                }
            })
            .build()
    }

    @Bean
    @JobScope
    fun payPagingClearStep(): Step {
        return stepBuilderFactory.get(JOB_NAME + "ClearStep")
            .chunk<Pay, Pay>(CHUNK_SIZE)
            .reader(payPagingReader())
            .processor(payClearProcessor())
            .writer(payPagingWriter())
            .listener(object: StepExecutionListener {
                override fun beforeStep(stepExecution: StepExecution) {
                    log.info("before Step payClearStep")
                }

                override fun afterStep(stepExecution: StepExecution): ExitStatus? {
                    val exitCode = stepExecution.exitStatus.exitCode
                    log.info("afterStep payClearStep - Status {}", exitCode)
                    if(exitCode == ExitStatus.FAILED.exitCode) {
                        log.error("payClearStep FAILED!!")
                    }
                    return null
                }
            })
            .build()
    }

    @Bean
    @StepScope
    fun payPagingReader(): JpaPagingItemReader<Pay> {
        val reader: JpaPagingItemReader<Pay> = object : JpaPagingItemReader<Pay>() {
            override fun getPage(): Int {
                return 0
            }
        }

        // shop을 paramter로 받아 쿼리 작업을 수행하는 기능을 향후 추가합니다.
        reader.setQueryString("SELECT p FROM Pay p WHERE p.successStatus = false ORDER BY p.orderDate DESC")
        reader.pageSize = CHUNK_SIZE
        reader.setEntityManagerFactory(entityManagerFactory)
        reader.name = JOB_NAME + "Reader"

        return reader
    }

    @Bean
    @StepScope
    fun payPagingProcessor(): ItemProcessor<Pay, Pay> = ItemProcessor { newSuccessPay(it) }

    @Bean
    @StepScope
    fun payClearProcessor(): ItemProcessor<Pay, Pay> = ItemProcessor { clearPay(it) }

    @Bean
    @StepScope
    fun payPagingWriter(): JpaItemWriter<Pay> {
        val writer = JpaItemWriter<Pay>()
        writer.setEntityManagerFactory(entityManagerFactory)
        return writer
    }

    private fun newSuccessPay(pay: Pay): Pay = Pay(
        id = pay.id,
        amount = pay.amount,
        order = pay.order,
        isSuccess = true,
        orderDate = pay.orderDate
    )

    private fun clearPay(pay: Pay) = Pay(
        id = pay.id,
        amount = pay.amount,
        order = pay.order,
        isSuccess = false,
        orderDate = pay.orderDate
    )

}