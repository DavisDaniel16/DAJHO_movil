package com.example.dajho.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.dajho.data.local.dao.BusinessDao
import com.example.dajho.data.local.dao.CashDao
import com.example.dajho.data.local.dao.ClientSupplierDao
import com.example.dajho.data.local.dao.ExpenseDao
import com.example.dajho.data.local.dao.LocalImageDao
import com.example.dajho.data.local.dao.ProductDao
import com.example.dajho.data.local.dao.SaleDao
import com.example.dajho.data.local.dao.SyncConflictDao
import com.example.dajho.data.local.dao.SyncOperationDao
import com.example.dajho.data.local.dao.UserProfileDao
import com.example.dajho.data.local.entity.BusinessEntity
import com.example.dajho.data.local.entity.CashMovementEntity
import com.example.dajho.data.local.entity.CashSessionEntity
import com.example.dajho.data.local.entity.ClientEntity
import com.example.dajho.data.local.entity.DebtPaymentEntity
import com.example.dajho.data.local.entity.ExpenseEntity
import com.example.dajho.data.local.entity.LocalImageEntity
import com.example.dajho.data.local.entity.PaymentEntity
import com.example.dajho.data.local.entity.ProductEntity
import com.example.dajho.data.local.entity.ProductVariantEntity
import com.example.dajho.data.local.entity.SaleEntity
import com.example.dajho.data.local.entity.SaleItemEntity
import com.example.dajho.data.local.entity.SupplierEntity
import com.example.dajho.data.local.entity.SyncConflictEntity
import com.example.dajho.data.local.entity.SyncOperationEntity
import com.example.dajho.data.local.entity.UserProfileEntity

@Database(
    entities = [
        BusinessEntity::class,
        UserProfileEntity::class,
        ProductEntity::class,
        ProductVariantEntity::class,
        ClientEntity::class,
        SupplierEntity::class,
        SaleEntity::class,
        SaleItemEntity::class,
        PaymentEntity::class,
        DebtPaymentEntity::class,
        ExpenseEntity::class,
        CashSessionEntity::class,
        CashMovementEntity::class,
        SyncOperationEntity::class,
        LocalImageEntity::class,
        SyncConflictEntity::class
    ],
    version = 7,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun businessDao(): BusinessDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun productDao(): ProductDao
    abstract fun clientSupplierDao(): ClientSupplierDao
    abstract fun saleDao(): SaleDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun cashDao(): CashDao
    abstract fun syncOperationDao(): SyncOperationDao
    abstract fun localImageDao(): LocalImageDao
    abstract fun syncConflictDao(): SyncConflictDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Instancia única (Singleton) de la base de datos Room
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dajho_database.db"
                )
                    .addMigrations(MIGRATION_6_7)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // Migración para optimizar índices de sincronización
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_operaciones_sincronizacion_createdAt " +
                        "ON operaciones_sincronizacion(createdAt)"
                )
            }
        }
    }
}
