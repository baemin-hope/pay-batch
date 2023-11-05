package khope.paybatch.jobs.pay

import khope.paybatch.domain.pay.Pay
import khope.paybatch.domain.pay.PayRepository
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import java.time.LocalDateTime
import javax.batch.runtime.BatchStatus

@RunWith(SpringRunner::class)
@SpringBootTest
@TestPropertySource(properties = ["job.name=" + PayPagingFailJobConfiguration.JOB_NAME])
internal class PayPagingFailJobConfigurationTest(
    @Autowired
    private val jobLauncherTestUtils: JobLauncherTestUtils,

    @Autowired
    private val payRepository: PayRepository,
) {

    @Test
    fun `같은 조건을 읽고 업데이트 할 때`() {
        // given
        for(i in 1 .. 100) {
            payRepository.save(
                Pay(
                    id = i.toLong(),
                    amount = 2,
                    itemName = "item$i",
                    isSuccess = false,
                    orderDate = LocalDateTime.now()
                )
            )
        }

        // when
        val jobExecution = jobLauncherTestUtils.launchJob()

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(payRepository.findByIsSuccess(true).size).isEqualTo(100)

    }
}