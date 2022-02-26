package com.hide.videophoto.ui.vault

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.hide.videophoto.R
import com.hide.videophoto.common.Constants
import com.hide.videophoto.common.ext.loadImage
import com.hide.videophoto.common.util.CommonUtil
import com.hide.videophoto.data.model.FileModel

class BucketAdapter(
    private val ctx: Context,
    private val buckets: List<FileModel>,
    private val type: String?
) :
    BaseAdapter() {

    private val layoutInflater by lazy { ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater }
    private val imageCornerRadius4 by lazy { CommonUtil.convertDpToPixel(ctx, R.dimen.dimen_4) }

    override fun getCount(): Int {
        return buckets.size
    }

    override fun getItem(position: Int): Any? {
        if (buckets.isNotEmpty()) {
            return buckets[position]
        }

        return null
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val holder: BucketHolder
        if (convertView == null) {
            view = layoutInflater.inflate(R.layout.item_choosing_bucket_to_filter, parent, false)
            holder = BucketHolder(view)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as BucketHolder
        }

        if (count > 0) {
            val model = buckets[position]
            holder.bind(model, imageCornerRadius4, type)
        }

        return view
    }

    private class BucketHolder(itemView: View) {
        private val imgBucket: ImageView = itemView.findViewById(R.id.img_bucket)
        private val lblName: TextView = itemView.findViewById(R.id.lbl_name)
        private val lblQuantity: TextView = itemView.findViewById(R.id.lbl_quantity)

        fun bind(model: FileModel, radius: Int, type: String?) {
            with(model) {
                when (type) {
                    Constants.DataType.VIDEO -> {
                        imgBucket.loadImage(getOriginalPath(), radius, R.drawable.ic_video)
                    }
                    Constants.DataType.IMAGE -> {
                        imgBucket.loadImage(getOriginalPath(), radius)
                    }
                    Constants.DataType.AUDIO -> {
                        imgBucket.setImageResource(R.drawable.ic_headphones)
                    }
                }
                lblName.text = bucketName
                lblQuantity.text = itemQuantity.toString()
            }
        }
    }
}