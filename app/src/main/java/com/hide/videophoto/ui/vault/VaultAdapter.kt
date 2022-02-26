package com.hide.videophoto.ui.vault

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
import com.hide.videophoto.common.util.DateTimeUtil
import com.hide.videophoto.common.util.NumberUtil
import com.hide.videophoto.data.mapper.getThumbnail
import com.hide.videophoto.data.mapper.isAudio
import com.hide.videophoto.data.mapper.isImage
import com.hide.videophoto.data.mapper.isVideo
import com.hide.videophoto.data.model.FileModel

open class VaultAdapter(
    private val ctx: AppCompatActivity,
    private val arrFiles: List<FileModel>,
    private val itemClickListener: (FileModel) -> Unit,
    private val itemLongClickListener: (FileModel) -> Unit,
    private val movingListener: (FileModel) -> Unit,
    private val renameListener: (FileModel) -> Unit,
    private val unhideListener: (FileModel) -> Unit,
    private val deletingListener: (FileModel) -> Unit,
    private val detailListener: (FileModel) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_PHOTO_LIST = 1
    private val VIEW_PHOTO_GRID = 2
    private val VIEW_VIDEO_LIST = 3
    private val VIEW_VIDEO_GRID = 4
    private val VIEW_AUDIO_LIST = 5
    private val VIEW_AUDIO_GRID = 6
    private val VIEW_OTHER_LIST = 7
    private val VIEW_OTHER_GRID = 8

    private val MODE_NORMAL = "normal"
    private val MODE_SELECTABLE = "selectable"

    private val dateTimeFormat by lazy { ctx.dateTimeFormat }
    private val layoutInflater by lazy { LayoutInflater.from(ctx) }
    private var isHiding = false

    private var mode = MODE_NORMAL
    private var layout = ctx.appSettingsModel.layoutTypeFile ?: Constants.Layout.LIST

    private var attachedRcl: RecyclerView? = null
    private val itemWidth by lazy {
        (CommonUtil.getRealScreenSizeAsPixels(ctx).x - CommonUtil.convertDpToPixel(
            ctx, R.dimen.dimen_16
        ) * 3) / 2
    }
    private val itemWidthVideoList by lazy { CommonUtil.convertDpToPixel(ctx, R.dimen.dimen_120) }
    private val imageCornerRadius4 by lazy { CommonUtil.convertDpToPixel(ctx, R.dimen.dimen_4) }
    private val imageCornerRadius8 by lazy { CommonUtil.convertDpToPixel(ctx, R.dimen.dimen_8) }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_VIDEO_LIST -> {
                val view = layoutInflater.inflate(R.layout.item_video_list, parent, false)
                VideoVHList(view)
            }
            VIEW_VIDEO_GRID -> {
                val view = layoutInflater.inflate(R.layout.item_video_grid, parent, false)
                VideoVHGrid(view)
            }
            VIEW_AUDIO_LIST -> {
                val view = layoutInflater.inflate(R.layout.item_audio_list, parent, false)
                AudioVHList(view)
            }
            VIEW_AUDIO_GRID -> {
                val view = layoutInflater.inflate(R.layout.item_audio_grid, parent, false)
                AudioVHGrid(view)
            }
            VIEW_PHOTO_LIST -> {
                val view = layoutInflater.inflate(R.layout.item_photo_list, parent, false)
                PhotoVHList(view)
            }
            VIEW_PHOTO_GRID -> {
                val view = layoutInflater.inflate(R.layout.item_photo_grid, parent, false)
                PhotoVHGrid(view)
            }
            VIEW_OTHER_LIST -> {
                val view = layoutInflater.inflate(R.layout.item_other_file_list, parent, false)
                OtherVHList(view)
            }
            else -> {
                val view = layoutInflater.inflate(R.layout.item_other_file_grid, parent, false)
                OtherVHGrid(view)
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
                is VideoVHList -> holder.bind(fileModel)
                is VideoVHGrid -> holder.bind(fileModel)
                is AudioVHList -> holder.bind(fileModel)
                is AudioVHGrid -> holder.bind(fileModel)
                is PhotoVHList -> holder.bind(fileModel)
                is PhotoVHGrid -> holder.bind(fileModel)
                is OtherVHList -> holder.bind(fileModel)
                is OtherVHGrid -> holder.bind(fileModel)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val file = arrFiles[position]
        return when {
            file.isImage() -> {
                if (layout == Constants.Layout.LIST) {
                    VIEW_PHOTO_LIST
                } else {
                    VIEW_PHOTO_GRID
                }
            }
            file.isVideo() -> {
                if (layout == Constants.Layout.LIST) {
                    VIEW_VIDEO_LIST
                } else {
                    VIEW_VIDEO_GRID
                }
            }
            file.isAudio() -> {
                if (layout == Constants.Layout.LIST) {
                    VIEW_AUDIO_LIST
                } else {
                    VIEW_AUDIO_GRID
                }
            }
            else -> {
                if (layout == Constants.Layout.LIST) {
                    VIEW_OTHER_LIST
                } else {
                    VIEW_OTHER_GRID
                }
            }
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
            layoutTypeFile = layout
        }.run {
            CommonUtil.saveAppSettingsModel(ctx, this)
        }
    }

    fun switchMode() {
        if (isSelectableMode()) {
            mode = MODE_NORMAL
            arrFiles.map {
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

    fun setHiding(flag: Boolean) {
        isHiding = flag
    }

    inner class VideoVHList(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgVideo: ImageView = itemView.findViewById(R.id.img_video)
        private val lblName: TextView = itemView.findViewById(R.id.lbl_name)
        private val lblInfo: TextView = itemView.findViewById(R.id.lbl_info)
        private val imgMode: ImageView = itemView.findViewById(R.id.img_mode)
        private val imgMore: ImageView = itemView.findViewById(R.id.img_more)
        private val imgPlay: ImageView = itemView.findViewById(R.id.img_play)
        private val vwDivider: View = itemView.findViewById(R.id.vw_divider)

        fun bind(model: FileModel) {
            with(model) {
                imgVideo.calRatio(itemWidthVideoList, 0.75f)
                if (isHiding) {
                    imgVideo.loadImage(getOriginalPath(), imageCornerRadius4, R.drawable.ic_video)
                } else {
                    imgVideo.loadImage(getEncryptedPath(), imageCornerRadius4, R.drawable.ic_video)
                }
                lblName.text = name
                lblInfo.text = String.format(
                    ctx.getString(R.string.file_info),
                    NumberUtil.parseDuration(duration),
                    NumberUtil.parseSize(ctx, size)
                )

                if (isSelectableMode()) {
                    imgPlay.gone()
                    imgMore.gone()
                    imgMode.visible()
                    if (isSelected) {
                        imgMode.setImageResource(R.drawable.ic_selected)
                    } else {
                        imgMode.setImageResource(R.drawable.ic_unselected)
                    }
                } else {
                    imgPlay.visible()
                    imgMore.visible()
                    imgMode.gone()
                }

                if (bindingAdapterPosition < arrFiles.size - 1) {
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

    inner class VideoVHGrid(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgVideo: ImageView = itemView.findViewById(R.id.img_video)
        private val lblDuration: TextView = itemView.findViewById(R.id.lbl_duration)
        private val imgPlay: ImageView = itemView.findViewById(R.id.img_play)
        private val imgMode: ImageView = itemView.findViewById(R.id.img_mode)

        fun bind(model: FileModel) {
            with(model) {
                imgVideo.calRatio(itemWidth, 0.75f)
                if (isHiding) {
                    imgVideo.loadImage(getOriginalPath(), imageCornerRadius8, R.drawable.ic_video)
                } else {
                    imgVideo.loadImage(getEncryptedPath(), imageCornerRadius8, R.drawable.ic_video)
                }
                lblDuration.text = NumberUtil.parseDuration(duration)

                if (isSelectableMode()) {
                    imgPlay.gone()
                    imgMode.visible()
                    if (isSelected) {
                        imgMode.setImageResource(R.drawable.ic_selected)
                    } else {
                        imgMode.setImageResource(R.drawable.ic_unselected)
                    }
                } else {
                    imgMode.gone()
                    imgPlay.visible()
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

    inner class AudioVHList(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val lblName: TextView = itemView.findViewById(R.id.lbl_name)
        private val lblInfo: TextView = itemView.findViewById(R.id.lbl_info)
        private val imgMode: ImageView = itemView.findViewById(R.id.img_mode)
        private val imgMore: ImageView = itemView.findViewById(R.id.img_more)
        private val vwDivider: View = itemView.findViewById(R.id.vw_divider)

        fun bind(model: FileModel) {
            with(model) {
                lblName.text = name
                lblInfo.text = String.format(
                    ctx.getString(R.string.file_info),
                    NumberUtil.parseDuration(duration),
                    NumberUtil.parseSize(ctx, size)
                )

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

                if (bindingAdapterPosition < arrFiles.size - 1) {
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

    inner class AudioVHGrid(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val container: ConstraintLayout = itemView.findViewById(R.id.cst_audio_container)
        private val lblName: TextView = itemView.findViewById(R.id.lbl_name)
        private val imgMode: ImageView = itemView.findViewById(R.id.img_mode)

        fun bind(model: FileModel) {
            with(model) {
                container.calRatio(itemWidth, 1f)
                lblName.text = name

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

    inner class PhotoVHList(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgPhoto: ImageView = itemView.findViewById(R.id.img_photo)
        private val lblName: TextView = itemView.findViewById(R.id.lbl_name)
        private val lblInfo: TextView = itemView.findViewById(R.id.lbl_info)
        private val imgMode: ImageView = itemView.findViewById(R.id.img_mode)
        private val imgMore: ImageView = itemView.findViewById(R.id.img_more)
        private val vwDivider: View = itemView.findViewById(R.id.vw_divider)

        fun bind(model: FileModel) {
            with(model) {
                if (isHiding) {
                    imgPhoto.loadImage(getOriginalPath(), imageCornerRadius4)
                } else {
                    imgPhoto.loadImage(getEncryptedPath(), imageCornerRadius4)
                }
                lblName.text = name
                lblInfo.text = String.format(
                    ctx.getString(R.string.file_info),
                    DateTimeUtil.convertTimeStampToDate(modifiedDate, dateTimeFormat),
                    NumberUtil.parseSize(ctx, size)
                )

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

                if (bindingAdapterPosition < arrFiles.size - 1) {
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

    inner class PhotoVHGrid(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgPhoto: ImageView = itemView.findViewById(R.id.img_photo)
        private val imgMode: ImageView = itemView.findViewById(R.id.img_mode)

        fun bind(model: FileModel) {
            with(model) {
                imgPhoto.calRatio(itemWidth, 1f)
                if (isHiding) {
                    imgPhoto.loadImage(getOriginalPath(), imageCornerRadius8)
                } else {
                    imgPhoto.loadImage(getEncryptedPath(), imageCornerRadius8)
                }

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

    inner class OtherVHList(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgOther: ImageView = itemView.findViewById(R.id.img_other)
        private val lblName: TextView = itemView.findViewById(R.id.lbl_name)
        private val lblInfo: TextView = itemView.findViewById(R.id.lbl_info)
        private val imgMode: ImageView = itemView.findViewById(R.id.img_mode)
        private val imgMore: ImageView = itemView.findViewById(R.id.img_more)
        private val vwDivider: View = itemView.findViewById(R.id.vw_divider)

        fun bind(model: FileModel) {
            with(model) {
                imgOther.loadImage(getThumbnail())
                lblName.text = name
                lblInfo.text = String.format(
                    ctx.getString(R.string.file_info),
                    DateTimeUtil.convertTimeStampToDate(modifiedDate, dateTimeFormat),
                    NumberUtil.parseSize(ctx, size)
                )

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

                if (bindingAdapterPosition < arrFiles.size - 1) {
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

    inner class OtherVHGrid(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgOther: ImageView = itemView.findViewById(R.id.img_other)
        private val container: ConstraintLayout = itemView.findViewById(R.id.cst_other_container)
        private val lblName: TextView = itemView.findViewById(R.id.lbl_name)
        private val imgMode: ImageView = itemView.findViewById(R.id.img_mode)

        fun bind(model: FileModel) {
            with(model) {
                container.calRatio(itemWidth, 1f)
                imgOther.loadImage(getThumbnail())
                lblName.text = name

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
        anchorView.showPopupMenu(R.menu.menu_action_file) { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_move -> {
                    movingListener(model)
                }
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
}