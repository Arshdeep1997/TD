package com.arshdeep.td

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue


class MainActivity : AppCompatActivity() ,Updateanddelete {
    private lateinit var dbRef:DatabaseReference
    var toDoList:MutableList<ToDoModel>? = null
    lateinit var adapter: ToDoAdapter
    private var listViewItem : ListView?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val fab = findViewById<FloatingActionButton>(R.id.fab) as FloatingActionButton
        listViewItem = findViewById(R.id.item_listview) as ListView

        dbRef = FirebaseDatabase.getInstance().getReference("Todo")
        fab.setOnClickListener { view -> }
        val alertDialog = AlertDialog.Builder(this)
        val textEditText = EditText(this)
        alertDialog.setMessage("Add TODO item")
        alertDialog.setTitle("Enter to do item")
        alertDialog.setView(textEditText)
        alertDialog.setPositiveButton("Add") { dialog, i ->
            val todoItemData = ToDoModel.createList()
            todoItemData.itemDataText = textEditText.text.toString()
            todoItemData.done = false

            val newItemData = dbRef.child("todo").push()
            todoItemData.UID = newItemData.key

            newItemData.setValue(todoItemData)

            dialog.dismiss()
            Toast.makeText(this, "item saved", Toast.LENGTH_LONG).show()

        }
        alertDialog.show()

        toDoList= mutableListOf<ToDoModel>()
        adapter= ToDoAdapter(this, toDoList!!)
        listViewItem!!.adapter=adapter
        dbRef.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError){
                Toast.makeText(applicationContext, "No item Added", Toast.LENGTH_LONG).show()

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                toDoList!!.clear()
                addItemToList(snapshot)

            }
        })
    }

    private fun addItemToList(snapshot: DataSnapshot) {
        val item=snapshot.children.iterator()
        if (item.hasNext()){

            val toDoIndexedValue = item.next()
            val itemIterator = toDoIndexedValue.children.iterator()

            while (itemIterator.hasNext()){
                val currentItem = itemIterator.next()
                val toDoItemData= ToDoModel.createList()
                val map =currentItem.getValue() as HashMap<String, Any>
                toDoItemData.UID = currentItem.key
                toDoItemData.done=map.get("done") as Boolean?
                toDoItemData.itemDataText=map.get("itemDataText") as String?
                toDoList!!.add(toDoItemData)
            }

        }
        adapter.notifyDataSetChanged()

    }

    override fun modifyItem(itemUID: String, isDone: Boolean) {
        val itemReference =dbRef.child("todo").child(itemUID)
        itemReference.child("done").setValue(isDone)
    }

    override fun onItemDelete(itemUID: String) {
        val itemReference =dbRef.child("todo").child(itemUID)
        itemReference.removeValue()
        adapter.notifyDataSetChanged()
    }
}
