package khope.paybatch.domain.pay

import org.springframework.data.jpa.repository.JpaRepository

interface PayRepository : JpaRepository<Pay, Long> {
    fun findByIsSuccess(isSuccess: Boolean): List<Pay>
}