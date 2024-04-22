package com.example.qup.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user")

class UserRepository(private val context: Context, private val scope: CoroutineScope) {
    private val USER_ID = intPreferencesKey("user_id")
    private val FACILITY_NAME = stringPreferencesKey("facility_name")
    private val BASE_URL = stringPreferencesKey("base_url")
    private val MAP_LAT = doublePreferencesKey("map_lat")
    private val MAP_LNG = doublePreferencesKey("map_lng")

    suspend fun saveExampleData(userId:Int, facilityName:String, baseUrl:String, mapLat:Double, mapLng:Double) {
        scope.launch {
            context.dataStore.edit { preferences ->
                preferences[USER_ID] = userId
                preferences[FACILITY_NAME] = facilityName
                preferences[BASE_URL] = baseUrl
                preferences[MAP_LAT] = mapLat
                preferences[MAP_LNG] = mapLng
            }
        }
    }

    val userId: Flow<Int> = context.dataStore.data
        .map {  preferences ->
            preferences[USER_ID] ?: -1      //default value
        }

    val facilityName: Flow<String> = context.dataStore.data
        .map {  preferences ->
            preferences[FACILITY_NAME] ?: ""
        }

    val baseUrl: Flow<String> = context.dataStore.data
        .map {  preferences ->
            preferences[BASE_URL] ?: "https://failed.com/"
        }

    val mapLat: Flow<Double> = context.dataStore.data
        .map {  preferences ->
            preferences[MAP_LAT] ?: 0.0      //default value
        }

    val mapLng: Flow<Double> = context.dataStore.data
        .map {  preferences ->
            preferences[MAP_LNG] ?: 0.0      //default value
        }

}