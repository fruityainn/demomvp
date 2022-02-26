package com.hide.videophoto.ui.note

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.hide.videophoto.R
import com.hide.videophoto.common.Constants
import com.hide.videophoto.common.ext.*
import com.hide.videophoto.common.util.CommonUtil
import com.hide.videophoto.common.util.DateTimeUtil
import com.hide.videophoto.data.model.FileModel

open class NoteAdapter(
    private val ctx: AppCompatActivity,
    private val arrNotes: List<FileModel>,
    private val itemClickListener: (FileModel) -> Unit,
    private val itemLongClickListener: (FileModel) -> Unit,
    private val copyListener: (FileModel) -> Unit,
    private val editListener: (FileModel) -> Unit,
    private val deletingListener: (FileModel) -> Unit,
    private val exportListener: (FileModel) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_NOTE_LIST = 1
    private val VIEW_NOTE_GRID = 2

    private val MODE_NORMAL = "normal"
    private val MODE_SELECTABLE = "selectable"

    private val dateTimeFormat by lazy { ctx.dateTimeFormat }
    private val layoutInflater by lazy { LayoutInflater.from(ctx) }

    private var mode = MODE_NORMAL
    private var layout = ctx.appSettingsModel.layoutTypeNote ?: Constants.Layout.LIST

    private var attachedRcl: RecyclerView? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_NOTE_GRID -> {
                val view = layoutInflater.inflate(R.layout.item_note_grid, parent, false)
                NoteVHGrid(view)
            }
            else -> {
                val view = layoutInflater.inflate(R.layout.item_note_list, parent, false)
                NoteVHList(view)
            }
        }
    }

    override fun getItemCount(): Int {
        return arrNotes.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (itemCount > 0) {
            val fileModel = arrNotes[position]
            when (holder) {
                is NoteVHList -> holder.bind(fileModel)
                is NoteVHGrid -> holder.bind(fileModel)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (layout == Constants.Layout.GRID_NOTE) {
            VIEW_NOTE_GRID
        } else {
            VIEW_NOTE_LIST
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        attachedRcl = recyclerView
    }

    fun switchLayout() {
        layout = if (layout == Constants.Layout.LIST) {
            Constants.Layout.GRID_NOTE
        } else {
            Constants.Layout.LIST
        }
        (attachedRcl?.layoutManager as StaggeredGridLayoutManager?)?.spanCount = layout
        notifyItemRangeChanged(0, itemCount)

        // Remember user's choice
        ctx.appSettingsModel.apply {
            layoutTypeNote = layout
        }.run {
            CommonUtil.saveAppSettingsModel(ctx, this)
        }
    }

    fun switchMode() {
        if (isSelectableMode()) {
            mode = MODE_NORMAL
            arrNotes.map {
                it.isSelected = false
            }
        } else {
            mode = MODE_SELECTABLE
        }
        notifyDataSetChanged()
    }

    fun isSelectableMode(): Boolean {
        return mode == MODE_SELECTABLE
    }

    inner class NoteVHList(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val lblTitle: TextView = itemView.findViewById(R.id.lbl_title)
        private val lblContent: TextView = itemView.findViewById(R.id.lbl_content)
        private val lblTime: TextView = itemView.findViewById(R.id.lbl_time)
        private val imgMode: ImageView = itemView.findViewById(R.id.img_mode)
        private val imgMore: ImageView = itemView.findViewById(R.id.img_more)
        private val vwDivider: View = itemView.findViewById(R.id.vw_divider)

        fun bind(model: FileModel) {
            with(model) {
                if (title?.isNotEmpty() == true) {
                    lblTitle.visible()
                    lblTitle.text = title
                } else {
                    lblTitle.gone()
                }
                if (content?.isNotEmpty() == true) {
                    lblContent.visible()
                    lblContent.text = content
                } else {
                    lblContent.gone()
                }
                lblTime.text = DateTimeUtil.convertTimeStampToDate(modifiedDate, dateTimeFormat)

                if (isSelectableMode()) {
                    imgMore.gone()
                    imgMode.visible()
                    if (isSelected) {
                        imgMode.setImageResource(R.drawable.ic_selected)
                    } else {
                        imgMode.setImageResource(R.drawable.ic_unselected)
                    }
                } else {
                    imgMore.visible()
                    imgMode.gone()
                }

                if (bindingAdapterPosition < arrNotes.size - 1) {
                    vwDivider.visible()
                } else {
                    vwDivider.gone()
                }

                imgMore.setOnSafeClickListener {
                    showPopupMenu(imgMore, model)
                }

                itemView.setOnClickListener {
                    clickOnItem(model, bindingAdapterPosition, itemClickListener)
                }

                itemView.setOnLongClickListener {
                    longClickOnItem(model, bindingAdapterPosition, itemLongClickListener)
                    return@setOnLongClickListener true
                }
            }
        }
    }

    inner class NoteVHGrid(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val lblTitle: TextView = itemView.findViewById(R.id.lbl_title)
        private val lblContent: TextView = itemView.findViewById(R.id.lbl_content)
        private val lblTime: TextView = itemView.findViewById(R.id.lbl_time)
        private val imgMode: ImageView = itemView.findViewById(R.id.img_mode)

        fun bind(model: FileModel) {
            with(model) {
                if (title?.isNotEmpty() == true) {
                    lblTitle.visible()
                    lblTitle.text = title
                } else {
                    lblTitle.gone()
                }
                if (content?.isNotEmpty() == true) {
                    lblContent.visible()
                    lblContent.text = content
                } else {
                    lblContent.gone()
                }
                lblTime.text = DateTimeUtil.convertTimeStampToDate(modifiedDate, dateTimeFormat)

                if (isSelectableMode()) {
                    imgMode.visible()
                    if (isSelected) {
                        imgMode.setImageResource(R.drawable.ic_selected)
                    } else {
                        imgMode.setImageResource(R.drawable.ic_unselected)
                    }
                } else {
                    imgMode.gone()
                }

                itemView.setOnClickListener {
                    clickOnItem(model, bindingAdapterPosition, itemClickListener)
                }

                itemView.setOnLongClickListener {
                    longClickOnItem(model, bindingAdapterPosition, itemLongClickListener)
                    return@setOnLongClickListener true
                }
            }
        }
    }

    private fun clickOnItem(model: FileModel, position: Int, listener: (FileModel) -> Unit) {
        if (isSelectableMode()) {
            model.isSelected = !model.isSelected
            notifyItemChanged(position)
        }
        listener(model)
    }

    private fun longClickOnItem(model: FileModel, position: Int, listener: (FileModel) -> Unit) {
        if (isSelectableMode()) {
            model.isSelected = !model.isSelected
            notifyItemChanged(position)
        } else {
            mode = MODE_SELECTABLE
            model.isSelected = true
            notifyDataSetChanged()
        }
        listener(model)
    }

    private fun showPopupMenu(anchorView: View, model: FileModel) {
        anchorView.showPopupMenu(R.menu.menu_action_note) { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_copy -> {
                    copyListener(model)
                }
                R.id.menu_edit -> {
                    editListener(model)
                }
                R.id.menu_delete -> {
                    deletingListener(model)
                }
                R.id.menu_export -> {
                    exportListener(model)
                }
            }
        }
    }
}