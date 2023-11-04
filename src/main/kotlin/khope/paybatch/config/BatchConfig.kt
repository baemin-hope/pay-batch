package khope.paybatch.config

import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BatchConfig: DefaultBatchConfigurer() {

    @Bean
    fun jobBuilderFactory(): JobBuilderFactory {
        return JobBuilderFactory(jobRepository)
    }

    @Bean
    fun stepBuilderFactory(): StepBuilderFactory {
        return StepBuilderFactory(jobRepository, transactionManager)
    }
}