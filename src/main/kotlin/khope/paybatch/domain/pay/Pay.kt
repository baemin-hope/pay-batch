package khope.paybatch.domain.pay

import khope.paybatch.domain.order.Order
import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToOne

@Entity
class Pay(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    val amount: Int,

    @OneToOne
    val order: Order,

    val isSuccess: Boolean,

    val orderDate: LocalDateTime
)