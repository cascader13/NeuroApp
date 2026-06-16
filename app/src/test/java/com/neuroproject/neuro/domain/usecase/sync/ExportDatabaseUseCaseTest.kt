package com.neuroproject.neuro.domain.usecase.sync

import com.google.common.truth.Truth.assertThat
import com.neuroproject.neuro.domain.model.Result
import com.neuroproject.neuro.domain.repository.DatabaseExportRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ExportDatabaseUseCaseTest {

    private lateinit var useCase: ExportDatabaseUseCase
    private val databaseExportRepository: DatabaseExportRepository = mock()

    @Before
    fun setup() {
        useCase = ExportDatabaseUseCase(databaseExportRepository)
    }

    @Test
    fun `when repository returns Success then use case returns Success`() = runTest {
        val expectedMessage = "Database exported to Downloads/neuro_database_20240101.db"
        whenever(databaseExportRepository.exportDatabaseToDownloads())
            .thenReturn(Result.Success(expectedMessage))

        val result = useCase()

        assertThat(result).isInstanceOf(Result.Success::class.java)
        assertThat((result as Result.Success).data).isEqualTo(expectedMessage)
    }

    @Test
    fun `when repository returns Error then use case returns Error`() = runTest {
        val expectedError = Result.Error(
            Exception("Database not found"),
            "Файл базы данных не найден"
        )
        whenever(databaseExportRepository.exportDatabaseToDownloads())
            .thenReturn(expectedError)

        val result = useCase()

        assertThat(result).isInstanceOf(Result.Error::class.java)
        assertThat((result as Result.Error).message).isEqualTo("Файл базы данных не найден")
    }

    @Test
    fun `use case delegates to repository exportDatabaseToDownloads`() = runTest {
        whenever(databaseExportRepository.exportDatabaseToDownloads())
            .thenReturn(Result.Success("Exported"))

        useCase()

        org.mockito.kotlin.verify(databaseExportRepository).exportDatabaseToDownloads()
    }

    @Test
    fun `when repository throws exception then use case returns Error`() = runTest {
        whenever(databaseExportRepository.exportDatabaseToDownloads())
            .thenThrow(RuntimeException("Unexpected error"))

        val result = useCase()

        assertThat(result).isInstanceOf(Result.Error::class.java)
    }
}
