package com.tugas.whatsappclone.listener

interface ChatClickListener {
    fun onChatClicked(name: String?, otherUserId: String?, chatsImageUrl: String?, chatsName: String?)
}