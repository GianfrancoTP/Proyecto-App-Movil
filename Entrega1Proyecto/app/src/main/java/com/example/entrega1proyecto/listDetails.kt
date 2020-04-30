package com.example.entrega1proyecto

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_list_details.*

class listDetails : AppCompatActivity(), OnSpecificItemClickListener {

    //CAMBIAR EL TIPO DE ELEMENTOS DENTRO DE LA LISTA
    val itemsOnList: ArrayList<ListaItem> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_details)
        itemsRecyclerView.adapter = AdaptadorItemsCustom(itemsOnList, this)
        itemsRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    //FALTA IMPLEMENTAR LOS ITEMS DENTRO DE LA LISTA
    override fun onSpecificItemCLicked(result: ListaItem) {
        TODO("Not yet implemented")
    }
}
