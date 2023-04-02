package com.nima.bluetoothchatapp.message

data class Child(
    var childs : List<Message>
) {
    override fun toString(): String {
        return "Child(childs=$childs)"
    }
}