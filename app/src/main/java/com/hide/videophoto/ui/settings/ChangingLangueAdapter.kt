package com.hide.videophoto.ui.settings

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hide.videophoto.R
import com.hide.videophoto.data.model.LanguageModel

open class ChangingLangueAdapter(
    private val ctx: Context, private val arrLanguage: List<LanguageModel>
) : RecyclerView.Adapter<ChangingLangueAdapter.ItemViewHolder>() {

    private val layoutInflater by lazy { LayoutInflater.from(ctx) }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = layoutInflater.inflate(R.layout.item_language, parent, false)
        return ItemViewHolder(view)
    }

    override fun getItemCount(): Int {
        return arrLanguage.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        if (itemCount > 0) {
            val fileModel = arrLanguage[position]
            holder.bind(fileModel)
        }
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val lblName: TextView = itemView.findViewById(R.id.lbl_name)

        fun bind(model: LanguageModel) {
            with(model) {
                lblName.text = name

                if (isSelected) {
                    itemView.setBackgroundResource(R.drawable.btn_accent20)
                } else {
                    itemView.setBackgroundResource(R.drawable.btn_primary)
                }

                itemView.setOnClickListener {
                    clickOnItem(bindingAdapterPosition)
                }
            }
        }
    }

    private fun clickOnItem(position: Int) {
        val model = arrLanguage[position]
        if (!model.isSelected) {
            val oldSelectedModel = arrLanguage.find { it.isSelected }
            oldSelectedModel?.run {
                isSelected = false
                val index = arrLanguage.indexOf(oldSelectedModel)
                notifyItemChanged(index)
            }

            model.isSelected = !model.isSelected
            notifyItemChanged(position)
        }
    }
}