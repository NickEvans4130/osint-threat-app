package com.example.osint.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        MaliciousUrl::class,
        MaliciousDomain::class,
        MaliciousIp::class,
        MaliciousHash::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ThreatDatabase : RoomDatabase() {
    abstract fun maliciousUrlDao(): MaliciousUrlDao
    abstract fun maliciousDomainDao(): MaliciousDomainDao
    abstract fun maliciousIpDao(): MaliciousIpDao
    abstract fun maliciousHashDao(): MaliciousHashDao

    companion object {
        @Volatile
        private var INSTANCE: ThreatDatabase? = null

        fun getDatabase(context: Context): ThreatDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ThreatDatabase::class.java,
                    "threat_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
