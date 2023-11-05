package khope.paybatch.jobs.pay

import khope.paybatch.common.BaseJobParameter

data class PayPagingFailJobParameter(
    override val version: Int,
    val shop: String
) : BaseJobParameter(version)
