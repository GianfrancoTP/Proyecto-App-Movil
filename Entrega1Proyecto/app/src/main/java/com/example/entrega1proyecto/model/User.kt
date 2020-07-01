package com.example.entrega1proyecto.model

import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import com.example.entrega1proyecto.model.ItemBDD.Companion.TABLE2_NAME
import com.example.entrega1proyecto.model.ItemBddErased.Companion.TABLE_NAME_OFFLINE_ITEM
import com.example.entrega1proyecto.model.ListBDD.Companion.TABLE_NAME
import com.example.entrega1proyecto.model.ListBddErased.Companion.TABLE_NAME_OFFLINE
import com.example.entrega1proyecto.model.UserBBDD.Companion.TABLE3_NAME
import java.io.Serializable


// Here we set the User we want to get from the URL given
data class User(var email:String, var first_name:String, var last_name:String, var phone:String, var profile_photo:String): Serializable

@Dao
interface ListDao{
    // Esto es para crear, updatear, obtener o eliminar un usuario
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(data: UserBBDD): Long

    @Update
    fun updateUser(data: UserBBDD)

    @Query("SELECT * FROM $TABLE3_NAME")
    fun getUser(): UserBBDD

    // Esto es para crear, updatear, obtener o eliminar una lista
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertList(data: ListBDD): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun eraseList(data: ListBddErased)

    @Delete
    fun deleteListDeleted(data: ListBddErased)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun eraseItem(data: ItemBddErased)

    @Delete
    fun deleteItemDeleted(data: ItemBddErased)

    @Query("SELECT * FROM $TABLE_NAME_OFFLINE")
    fun getAllListsErased(): List<Long>

    @Query("SELECT * FROM $TABLE_NAME_OFFLINE_ITEM")
    fun getAllItemsErased(): List<Long>

    @Query("SELECT * FROM $TABLE2_NAME WHERE online = :bool")
    fun getAllItemsOffline(bool: Boolean = false): List<ItemBDD>

    @Query("SELECT * FROM $TABLE_NAME WHERE online = :bool")
    fun getAllListsOffline(bool: Boolean = false): List<ListBDD>

    @Query("SELECT * FROM $TABLE3_NAME WHERE online = :bool")
    fun getUserOffline(bool: Boolean = false): User

    @Update
    fun updateList(data: ListBDD)

    @Transaction
    @Query("SELECT * FROM $TABLE_NAME ORDER BY position")
    fun getListWithItems(): List<ListWithItems>

    @Transaction
    @Query("SELECT * FROM $TABLE_NAME Where id = :id")
    fun getSpecificList(id: Long): ListWithItems

    @Delete
    fun deleteList(data: ListBDD)

    @Query("DELETE FROM ItemBDD WHERE id_list = :id")
    fun deleteListItems(id: Long)

    // Esto es para crear, updatear, obtener o eliminar un item
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertItem(data: ItemBDD): Long

    @Update
    fun updateItem(data:ItemBDD)

    @Transaction
    @Query("SELECT * FROM $TABLE2_NAME Where id = :id")
    fun getSpecificItem(id: Long): ItemBDD

    @Query("SELECT * FROM $TABLE2_NAME")
    fun getAllItems(): List<ItemBDD>

    @Delete
    fun deleteItem(data: ItemBDD)
}

@Entity(tableName = TABLE_NAME)
data class ListBDD(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = ID)
    var id: Long,
    @ColumnInfo(name = NAME)
    var name: String,
    @ColumnInfo(name= POSITION)
    var position: Int,
    @ColumnInfo(name= UPDATEDAT)
    var updated_at: String,
    @ColumnInfo(name= ONLINE)
    var isOnline: Boolean
):Serializable{
    companion object{
        const val TABLE_NAME = "ListBDD"
        const val ID = "id"
        const val NAME = "title"
        const val POSITION = "position"
        const val UPDATEDAT = "updated_at"
        const val ONLINE = "online"
    }
}

@Entity(tableName = TABLE_NAME_OFFLINE)
data class ListBddErased(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = ID)
    var id: Long
):Serializable{
    companion object{
        const val TABLE_NAME_OFFLINE = "ListBddOffline"
        const val ID = "id"
    }
}

@Entity(tableName = TABLE2_NAME)
data class ItemBDD(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = ID)
    var id: Long,
    @ColumnInfo(name = LIST_ID)
    var list_id: Long,
    @ColumnInfo(name = NAME_ITEMS)
    var name: String,
    @ColumnInfo(name = ESTADO)
    var done: Boolean,
    @ColumnInfo(name = PRIORIDAD)
    var starred: Boolean,
    @ColumnInfo(name = PLAZO)
    var due_date: String?,
    @ColumnInfo(name = NOTAS_ITEM)
    var notes: String?,
    @ColumnInfo(name = FECHA_CREACION)
    var created_at: String,
    @ColumnInfo(name = POSITION)
    var position: Int,
    @ColumnInfo(name = IS_SHOWN)
    var isShown: Boolean,
    @ColumnInfo(name= UPDATEDAT)
    var updated_at: String,
    @ColumnInfo(name= ONLINE)
    var isOnline: Boolean,
    @ColumnInfo(name= LONG)
    var longitud: Double,
    @ColumnInfo(name= LAT)
    var lat: Double
):Serializable{
    companion object{
        const val TABLE2_NAME = "ItemBDD"
        const val ID = "id"
        const val LIST_ID = "id_list"
        const val NAME_ITEMS = "item"
        const val ESTADO = "estado"
        const val PRIORIDAD = "prioridad"
        const val PLAZO = "plazo"
        const val NOTAS_ITEM = "notasItem"
        const val FECHA_CREACION = "fechaCreacion"
        const val POSITION = "position"
        const val IS_SHOWN = "isShown"
        const val UPDATEDAT = "updated_at"
        const val ONLINE = "online"
        const val LONG = "long"
        const val LAT = "lat"
    }
}

@Entity(tableName = TABLE_NAME_OFFLINE_ITEM)
data class ItemBddErased(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = ID)
    var id: Long
):Serializable{
    companion object{
        const val TABLE_NAME_OFFLINE_ITEM = "ItemBddOffline"
        const val ID = "id"
    }
}

data class ListWithItems(
    @Embedded val list: ListBDD,
    @Relation(
        parentColumn = "id",
        entityColumn = "id_list"
    )
    val items: List<ItemBDD>?
):Serializable

@Entity(tableName = TABLE3_NAME)
data class UserBBDD(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = ID)
    var id: Long,
    @ColumnInfo(name = FIRSTNAME)
    var first_name: String,
    @ColumnInfo(name = LASTNAME)
    var last_name: String,
    @ColumnInfo(name = EMAIL)
    var email: String,
    @ColumnInfo(name = PHONE)
    var phone: String,
    @ColumnInfo(name = PROFILEPHOTO)
    var profile_photo: String,
    @ColumnInfo(name= UPDATEDAT)
    var updated_at: String,
    @ColumnInfo(name= ONLINE)
    var isOnline: Boolean
):Serializable{
    companion object{
        const val TABLE3_NAME = "UserBDD"
        const val ID = "id"
        const val FIRSTNAME = "first_name"
        const val LASTNAME = "last_name"
        const val EMAIL = "email"
        const val PHONE = "phone"
        const val PROFILEPHOTO = "profile_photo"
        const val UPDATEDAT = "updated_at"
        const val ONLINE = "online"
    }
}
