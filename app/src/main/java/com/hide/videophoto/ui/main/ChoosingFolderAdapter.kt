package com.hide.videophoto.ui.main

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hide.videophoto.R
import com.hide.videophoto.data.model.FileModel

open class ChoosingFolderAdapter(
    private val ctx: Context,
    private val arrFiles: List<FileModel>,
    private val itemClickListener: (FileModel) -> Unit
) : RecyclerView.Adapter<ChoosingFolderAdapter.ItemViewHolder>() {

    private val layoutInflater by lazy { LayoutInflater.from(ctx) }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = layoutInflater.inflate(R.layout.item_choosing_folder_to_save, parent, false)
        return ItemViewHolder(view)
    }

    override fun getItemCount(): Int {
        return arrFiles.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        if (itemCount > 0) {
            val fileModel = arrFiles[position]
            holder.bind(fileModel)
        }
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val lblName: TextView = itemView.findViewById(R.id.lbl_name)

        fun bind(model: FileModel) {
            with(model) {
                lblName.text = name

                if (isSelected) {
                    itemView.setBackgroundResource(R.drawable.btn_accent20)
                } else {
                    itemView.setBackgroundResource(R.drawable.btn_primary)
                }

                itemView.setOnClickListener {
                    clickOnItem(bindingAdapterPosition, itemClickListener)
                }
            }
        }
    }

    private fun clickOnItem(position: Int, listener: (FileModel) -> Unit) {
        val model = arrFiles[position]
        if (!model.isSelected) {
            val oldSelectedModel = arrFiles.find { it.isSelected }
            oldSelectedModel?.run {
                isSelected = false
                val index = arrFiles.indexOf(oldSelectedModel)
                notifyItemChanged(index)
            }

            model.isSelected = !model.isSelected
            notifyItemChanged(position)
            listener(model)
        }
    }
}