package com.arshdeep.td

interface Updateanddelete {
    fun modifyItem(itemUID :String ,isDone :Boolean)
    fun onItemDelete(itemUID: String)
}