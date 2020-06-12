package com.example.entrega1proyecto.model

import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import com.example.entrega1proyecto.model.ItemBDD.Companion.TABLE2_NAME
import com.example.entrega1proyecto.model.ListBDD.Companion.TABLE_NAME
import java.io.Serializable


// Here we set the User we want to get from the URL given
data class User(var email:String, var first_name:String, var last_name:String, var phone:String, var profile_photo:String): Serializable

@Dao
interface ListDao{
    // Esto es para crear, updatear, obtener o eliminar una lista
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertList(data: ListBDD): Long

    @Update
    fun updateList(data: ListBDD)

    @Transaction
    @Query("SELECT * FROM $TABLE_NAME")
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
    var name: String
):Serializable{
    companion object{
        const val TABLE_NAME = "ListBDD"
        const val ID = "id"
        const val NAME = "title"
    }
}

@Entity(tableName = TABLE2_NAME)
data class ItemBDD(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = ID)
    var id: Long,
    @ColumnInfo(name = LIST_ID)
    var listID: Long,
    @ColumnInfo(name = NAME_ITEMS)
    var nameItem: String,
    @ColumnInfo(name = ESTADO)
    var estado: Boolean,
    @ColumnInfo(name = PRIORIDAD)
    var prioridad: Boolean,
    @ColumnInfo(name = PLAZO)
    var plazo: String,
    @ColumnInfo(name = NOTAS_ITEM)
    var notasItem: String,
    @ColumnInfo(name = FECHA_CREACION)
    var fechaCreacion: String,
    @ColumnInfo(name = IS_SHOWN)
    var isShown: Boolean
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
        const val IS_SHOWN = "isShown"
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