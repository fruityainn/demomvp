package com.hide.videophoto.ui.vault

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.Toolbar
import com.hide.videophoto.R
import com.hide.videophoto.common.Constants
import com.hide.videophoto.common.ext.addFragment
import com.hide.videophoto.common.ext.ctx
import com.hide.videophoto.common.ext.setOnSafeClickListener
import com.hide.videophoto.common.ext.visible
import com.hide.videophoto.common.util.CommonUtil
import com.hide.videophoto.data.mapper.convertToFileModel
import com.hide.videophoto.data.model.FileModel
import com.hide.videophoto.ui.base.BaseActivity

class VaultActivity : BaseActivity<VaultActView, VaultActPresenterImp>(), VaultActView,
    VaultFragment.ModeListener {

    private val toolbarVault by lazy { findViewById<Toolbar>(R.id.toolbar_vault) }
    private val lblTitle by lazy { findViewById<TextView>(R.id.lbl_title_vault) }
    private val spnBucket by lazy { findViewById<AppCompatSpinner>(R.id.spn_bucket) }

    private var vaultFragment: VaultFragment? = null
    private var fileModel: FileModel? = null
    private var isShowingFileByFolder: Boolean = false
    private var isShowingFileByType: Boolean = false
    private var dataType: String? = null
    private var isChoosingFileToHide: Boolean = false
    private var isSelectable = false
    private var bucketAdapter: BucketAdapter? = null

    companion object {
        private const val PARAM_IS_SHOWING_FILE_BY_FOLDER = "showing_file_by_folder"
        private const val PARAM_FILE_MODEL = "param_file_model"
        private const val PARAM_IS_SHOWING_FILE_BY_TYPE = "showing_file_by_type"
        private const val PARAM_DATA_TYPE = "param_data_type"
        private const val PARAM_IS_CHOOSING_FILE_TO_HIDE = "choosing_file_to_hide"

        fun start(
            ctx: Context,
            isShowingFileByFolder: Boolean? = null,
            fileModel: FileModel? = null,
            isShowingFileByType: Boolean? = null,
            dataType: String? = null,
            isChoosingFileToHide: Boolean? = null
        ) {
            Intent(ctx, VaultActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                isShowingFileByFolder?.also { flag ->
                    putExtra(PARAM_IS_SHOWING_FILE_BY_FOLDER, flag)
                }
                fileModel?.also { model ->
                    putExtra(PARAM_FILE_MODEL, model.toJson())
                }
                isShowingFileByType?.also { flag ->
                    putExtra(PARAM_IS_SHOWING_FILE_BY_TYPE, flag)
                }
                dataType?.also { type ->
                    putExtra(PARAM_DATA_TYPE, type)
                }
                isChoosingFileToHide?.also { flag ->
                    putExtra(PARAM_IS_CHOOSING_FILE_TO_HIDE, flag)
                }
            }.run {
                ctx.startActivity(this)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get extra values
        intent?.run {
            extras?.also { bundle ->
                if (bundle.containsKey(PARAM_IS_SHOWING_FILE_BY_FOLDER)
                    && bundle.containsKey(PARAM_FILE_MODEL)
                ) {
                    isShowingFileByFolder = bundle.getBoolean(PARAM_IS_SHOWING_FILE_BY_FOLDER)
                    fileModel = bundle.getString(PARAM_FILE_MODEL)?.convertToFileModel()
                }
                if (bundle.containsKey(PARAM_DATA_TYPE)) {
                    dataType = bundle.getString(PARAM_DATA_TYPE)
                }
                if (bundle.containsKey(PARAM_IS_SHOWING_FILE_BY_TYPE)) {
                    isShowingFileByType = bundle.getBoolean(PARAM_IS_SHOWING_FILE_BY_TYPE)
                }
                if (bundle.containsKey(PARAM_IS_CHOOSING_FILE_TO_HIDE)) {
                    isChoosingFileToHide = bundle.getBoolean(PARAM_IS_CHOOSING_FILE_TO_HIDE)
                }
            }
        }

        // Add fragment
        vaultFragment = VaultFragment.newInstance(
            isShowingFileByFolder = isShowingFileByFolder,
            fileModel = fileModel,
            isShowingFileByType = isShowingFileByType,
            dataType = dataType,
            isChoosingFileToHide = isChoosingFileToHide
        )
        vaultFragment?.also { fragment ->
            addFragment(R.id.frl_vault, fragment)
        }

        // Setup title
        if (!isChoosingFileToHide) {
            val title = when {
                isShowingFileByFolder -> {
                    fileModel?.name
                }
                isShowingFileByType -> {
                    when (dataType) {
                        Constants.DataType.VIDEO -> getString(R.string.video)
                        Constants.DataType.IMAGE -> getString(R.string.photo)
                        Constants.DataType.NOTE -> getString(R.string.note)
                        else -> getString(R.string.other)
                    }
                }
                else -> {
                    null
                }
            }
            lblTitle.text = title
        } else {
            lblTitle.maxWidth = CommonUtil.convertDpToPixel(ctx, R.dimen.dimen_200)
        }
    }

    override fun onBackPressed() {
        if (shouldResetMode()) {
            isSelectable = false
            setToolbarHomeIcon(R.drawable.ic_back_white)
            vaultFragment?.resetMode()
        } else {
            super.onBackPressed()
        }
    }

    override fun initView(): VaultActView {
        return this
    }

    override fun initPresenter(): VaultActPresenterImp {
        return VaultActPresenterImp(ctx)
    }

    override fun getLayoutId(): Int? {
        return R.layout.activity_vault
    }

    override fun initWidgets() {
        // Init toolbar
        applyToolbar(toolbarVault)
        enableHomeAsUp {
            onBackPressed()
        }
    }

    override fun onModeChanged(selectable: Boolean) {
        isSelectable = selectable
        if (shouldResetMode()) {
            setToolbarHomeIcon(R.drawable.ic_menu_close)
        }
    }

    override fun onMediaFilesLoadedSuccess(buckets: ArrayList<FileModel>) {
        // Init bucket spinner
        spnBucket.visible()
        val title = when (dataType) {
            Constants.DataType.VIDEO, Constants.DataType.IMAGE, Constants.DataType.AUDIO -> getString(
                R.string.all
            )
            else -> null
        }
        lblTitle.apply {
            text = title
            setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_dropdown_white, 0)

            setOnSafeClickListener {
                spnBucket.performClick()
            }
        }

        if (bucketAdapter == null && buckets.isNotEmpty()) {
            buckets.add(0, FileModel().apply {
                bucketName = title
                name = buckets[0].name
                originalFolder = buckets[0].originalFolder
                buckets.map {
                    itemQuantity += it.itemQuantity
                }
            })
            bucketAdapter = BucketAdapter(ctx, buckets, dataType)
            spnBucket.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    lblTitle.text = buckets[position].bucketName
                    vaultFragment?.onBucketSelected(buckets[position])
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

            }
            spnBucket.adapter = bucketAdapter
        }
    }

    private fun setToolbarHomeIcon(resIcon: Int) {
        getToolbar().setNavigationIcon(resIcon)
    }

    private fun shouldResetMode(): Boolean {
        return isSelectable && !isChoosingFileToHide
    }
}
