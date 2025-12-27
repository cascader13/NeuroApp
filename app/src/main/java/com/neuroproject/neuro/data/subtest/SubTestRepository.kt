package com.neuroproject.neuro.data.subtest

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubTestRepository @Inject constructor(
    private val dao: SubTestDao
) {
    suspend fun saveResult(result: SubTestResultEntity) {
        dao.insert(result)
    }

    suspend fun getResults(): List<SubTestResultEntity> =
        dao.getAll()
}
