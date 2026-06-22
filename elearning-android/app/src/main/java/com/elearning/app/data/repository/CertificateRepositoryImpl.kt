package com.elearning.app.data.repository

import com.elearning.app.data.remote.api.ResourceApiService
import com.elearning.app.data.remote.dto.CertificateDto
import com.elearning.app.domain.model.Result
import com.elearning.app.domain.repository.Certificate
import com.elearning.app.domain.repository.CertificateRepository
import javax.inject.Inject

class CertificateRepositoryImpl @Inject constructor(
    private val api: ResourceApiService
) : CertificateRepository {

    override suspend fun getCertificates(): Result<List<Certificate>> {
        return try {
            Result.Success(api.getCertificates().map { it.toDomain() })
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getCertificateDownloadUrl(id: String): Result<String> {
        return try {
            Result.Success(api.getCertificateDownloadUrl(id).url)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    private fun CertificateDto.toDomain() = Certificate(
        id = id,
        formationId = formationId,
        learnerName = learnerName ?: "Apprenant",
        formationTitle = formationTitle ?: "Formation",
        score = score ?: averageScore?.toInt() ?: 0,
        maxScore = maxScore ?: 100,
        certificateUrl = downloadUrl,
        verificationCode = verificationCode
    )
}
