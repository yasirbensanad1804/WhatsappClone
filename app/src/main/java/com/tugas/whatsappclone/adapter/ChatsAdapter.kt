package com.tugas.whatsappclone.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tugas.whatsappclone.R
import com.tugas.whatsappclone.listener.ChatClickListener
import com.tugas.whatsappclone.util.*
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_chats.*

class ChatsAdapter(val chats: ArrayList<String>) : RecyclerView.Adapter<ChatsViewHolder>() {

    private var chatClickListener: ChatClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatsViewHolder =
        ChatsViewHolder(LayoutInflater.from(parent.context)
            .inflate(
                R.layout.item_chats, parent, false
            )
        )

    override fun onBindViewHolder(holder: ChatsViewHolder, position: Int) {
        holder.bindItem(chats[position], chatClickListener)
    }

    override fun getItemCount() = chats.size

    fun setOnItemClickListener(listener: ChatClickListener) {
        chatClickListener = listener
        notifyDataSetChanged()
    }

    fun updateChats(updatedChats: ArrayList<String>){
        chats.clear()
        chats.addAll(updatedChats)
        notifyDataSetChanged()
    }
}

class ChatsViewHolder(override val containerView: View) :
    RecyclerView.ViewHolder(containerView), LayoutContainer{

    private val firebaseDb = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private var partnerId: String? = null
    private var chatName: String? = null
    private var chatImageUrl: String? = null

    fun bindItem(chatId: String, listener: ChatClickListener?) {
        progress_layout.visibility = View.VISIBLE
        progress_layout.setOnTouchListener { v, event -> true }

        firebaseDb.collection(DATA_CHATS) .document(chatId)
            .get()
            .addOnSuccessListener {
                val chatParticipants = it[DATA_CHAT_PARTICIPANTS]
                if (chatParticipants != null) {
                    for (participants in chatParticipants as ArrayList<String>){
                        if (participants != null && !participants.equals(userId)) {
                            partnerId = participants
                            firebaseDb.collection(DATA_USERS).document(partnerId!!).get()
                                .addOnSuccessListener {
                                    val user = it.toObject(User::class.java)
                                    chatImageUrl = user?.imageUrl
                                    chatName = user?.name
                                    txt_chats.text = user?.name
                                    populateImage(img_chats.context, user?.imageUrl!!,
                                        img_chats,R.drawable. ic_user)
                                    progress_layout.visibility = View.GONE
                                }.addOnFailureListener {e ->
                                    e.printStackTrace()
                                    progress_layout.visibility = View.GONE
                                }
                        }
                    }
                }
                progress_layout.visibility = View.GONE
            }.addOnFailureListener { e ->
                e.printStackTrace()
                progress_layout.visibility = View.GONE
            }
        itemView.setOnClickListener {
            listener?.onChatClicked(chatId, userId, chatImageUrl, chatName)
        }
    }

}
