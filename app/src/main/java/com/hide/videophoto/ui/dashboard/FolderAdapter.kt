package com.hide.videophoto.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hide.videophoto.R
import com.hide.videophoto.common.Constants
import com.hide.videophoto.common.ext.*
import com.hide.videophoto.common.util.CommonUtil
import com.hide.videophoto.data.model.FileModel

open class FolderAdapter(
    private val ctx: AppCompatActivity,
    private val arrFiles: List<FileModel>,
    private val itemClickListener: (FileModel) -> Unit,
    private val renameListener: (FileModel) -> Unit,
    private val unhideListener: (FileModel) -> Unit,
    private val deletingListener: (FileModel) -> Unit,
    private val detailListener: (FileModel) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_DIRECTORY_LIST = 1
    private val VIEW_DIRECTORY_GRID = 2

    private var attachedRcl: RecyclerView? = null

    private val layoutInflater by lazy { LayoutInflater.from(ctx) }
    private var layout = ctx.appSettingsModel.layoutTypeFolder
    private val itemWidth by lazy {
        (CommonUtil.getRealScreenSizeAsPixels(ctx).x - CommonUtil.convertDpToPixel(
            ctx, R.dimen.dimen_24
        ) * 3) / 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_DIRECTORY_LIST -> {
                val view = layoutInflater.inflate(R.layout.item_folder_list, parent, false)
                DirectoryVHList(view)
            }
            else -> {
                val view = layoutInflater.inflate(R.layout.item_folder_grid, parent, false)
                DirectoryVHGrid(view)
            }
        }
    }

    override fun getItemCount(): Int {
        return arrFiles.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (itemCount > 0) {
            val fileModel = arrFiles[position]
            when (holder) {
                is DirectoryVHList -> holder.bind(fileModel)
                is DirectoryVHGrid -> holder.bind(fileModel)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (layout == Constants.Layout.LIST) {
            VIEW_DIRECTORY_LIST
        } else {
            VIEW_DIRECTORY_GRID
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        attachedRcl = recyclerView
    }

    fun switchLayout() {
        layout = if (layout == Constants.Layout.LIST) {
            Constants.Layout.GRID
        } else {
            Constants.Layout.LIST
        }
        (attachedRcl?.layoutManager as GridLayoutManager?)?.spanCount = layout
        notifyItemRangeChanged(0, itemCount)

        // Remember user's choice
        ctx.appSettingsModel.apply {
            layoutTypeFolder = layout
        }.run {
            CommonUtil.saveAppSettingsModel(ctx, this)
        }
    }

    inner class DirectoryVHList(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val lblName: TextView = itemView.findViewById(R.id.lbl_name)
        private val lblQuantity: TextView = itemView.findViewById(R.id.lbl_quantity)
        private val imgMore: ImageView = itemView.findViewById(R.id.img_more)
        private val vwDivider: View = itemView.findViewById(R.id.vw_divider)

        fun bind(model: FileModel) {
            with(model) {
                lblName.text = name
                if (itemQuantity > 0) {
                    lblQuantity.apply {
                        visible()
                        text = itemQuantity.toString()
                    }
                } else {
                    lblQuantity.gone()
                }

                if (bindingAdapterPosition < arrFiles.size - 1) {
                    vwDivider.visible()
                } else {
                    vwDivider.gone()
                }

                imgMore.setOnSafeClickListener {
                    imgMore.showPopupMenu(R.menu.menu_action_folder) { menuItem ->
                        when (menuItem.itemId) {
                            R.id.menu_rename -> {
                                renameListener(model)
                            }
                            R.id.menu_unhide -> {
                                unhideListener(model)
                            }
                            R.id.menu_delete -> {
                                deletingListener(model)
                            }
                            R.id.menu_detail -> {
                                detailListener(model)
                            }
                        }
                    }
                }

                itemView.setOnClickListener {
                    itemClickListener(model)
                }
            }
        }
    }

    inner class DirectoryVHGrid(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val container: ConstraintLayout = itemView.findViewById(R.id.cst_folder_container)
        private val lblName: TextView = itemView.findViewById(R.id.lbl_name)
        private val lblQuantity: TextView = itemView.findViewById(R.id.lbl_quantity)

        fun bind(model: FileModel) {
            with(model) {
                container.calRatio(itemWidth, 0.75f)
                lblName.text = name
                if (itemQuantity > 0) {
                    lblQuantity.apply {
                        visible()
                        text = itemQuantity.toString()
                    }
                } else {
                    lblQuantity.gone()
                }

                itemView.setOnClickListener {
                    itemClickListener(model)
                }
            }
        }
    }
}