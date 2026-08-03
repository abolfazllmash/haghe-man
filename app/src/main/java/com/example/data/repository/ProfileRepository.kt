package com.example.data.repository

import com.example.data.db.UserProfileDao
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

class ProfileRepository(private val profileDao: UserProfileDao) {

    val allProfiles: Flow<List<UserProfile>> = profileDao.getAllProfiles()

    suspend fun getProfileById(id: Long): UserProfile? {
        return profileDao.getProfileById(id)
    }

    suspend fun saveProfile(profile: UserProfile): Long {
        return profileDao.insertProfile(profile)
    }

    suspend fun updateProfile(profile: UserProfile) {
        profileDao.updateProfile(profile)
    }

    suspend fun deleteProfile(id: Long) {
        profileDao.deleteProfileById(id)
    }
}
