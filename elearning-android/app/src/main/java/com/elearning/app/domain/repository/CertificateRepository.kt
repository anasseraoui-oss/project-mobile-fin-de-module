package com.elearning.app.domain.repository

import com.elearning.app.domain.model.Result

data class Certificate(
    val id: String,
    val formationId: String?,
    val learnerName: String,
    val formationTitle: String,
    val score: Int,
    val maxScore: Int,
    val certificateUrl: String?,
    val verificationCode: String?
)

interface CertificateRepository {
    suspend fun getCertificates(): Result<List<Certificate>>
    suspend fun getCertificateDownloadUrl(id: String): Result<String>
}
