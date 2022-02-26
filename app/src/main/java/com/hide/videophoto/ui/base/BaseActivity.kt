package com.hide.videophoto.ui.base

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.WindowInsetsControllerCompat
import com.hide.videophoto.R
import com.hide.videophoto.common.ext.*
import com.hide.videophoto.common.util.CommonUtil
import com.hide.videophoto.common.util.PermissionUtil
import java.util.*

abstract class BaseActivity<V : BaseView, P : BasePresenterImp<V>> : AppCompatActivity(), BaseView {

    private val toolbarBase by lazy { findViewById<Toolbar>(R.id.toolbar_base) }
    private val frlBase by lazy { findViewById<FrameLayout>(R.id.frl_base) }

    private lateinit var toolbarInUse: Toolbar

    protected val self by lazy { this }
    protected lateinit var presenter: P

    override fun attachBaseContext(newBase: Context?) {
        appSettingsModel.defaultLanguage = Locale.getDefault().language
        val context = newBase?.let {
            updateLocale(newBase, appSettingsModel.appLanguage)
        } ?: newBase
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = initPresenter()
        presenter.attachView(initView())
        getLayoutId()?.run {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            setContentView(R.layout.activity_base)
            layoutInflater.inflate(this, frlBase)

            // Base method
            applyToolbar()

            // Close keyboard when user touches outside
            CommonUtil.closeKeyboardWhileClickOutSide(self, contentView)
        }

        /* Base methods */
        initWidgets()
    }

    override fun onDestroy() {
        presenter.detachView()
        super.onDestroy()
    }

    /*
    * return view
    * */
    abstract fun initView(): V

    /*
    * Return presenter
    * */
    abstract fun initPresenter(): P

    /*
    * Return activity's layout id
    * */
    abstract fun getLayoutId(): Int?

    /*
    * Set up widgets such as EditText, TextView, RecyclerView, etc
    * */
    abstract fun initWidgets()

    protected fun translucentStatusBar() {
//        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    }

    protected fun hideNavigationBar() {
        if (PermissionUtil.isApi30orHigher()) {
            window.decorView.windowInsetsController?.apply {
                systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                hide(WindowInsets.Type.navigationBars())
            }
        } else {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
    }

    protected fun showNavigationBar() {
        if (PermissionUtil.isApi30orHigher()) {
            window.decorView.windowInsetsController?.show(WindowInsets.Type.navigationBars())
        } else {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_VISIBLE
        }
    }

    protected fun hideSystemBar() {
        if (PermissionUtil.isApi30orHigher()) {
            window.decorView.windowInsetsController?.apply {
                systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                hide(WindowInsets.Type.systemBars())
            }
        } else {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
        }
    }

    protected fun showSystemBar() {
        if (PermissionUtil.isApi30orHigher()) {
            window.decorView.windowInsetsController?.show(WindowInsets.Type.systemBars())
        } else {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        }
    }

    protected fun applyToolbar(toolbar: Toolbar = toolbarBase, background: Int? = null) {
        toolbarInUse = toolbar
        if (toolbarInUse != toolbarBase) {
            hideToolbarBase()
        }
        setSupportActionBar(toolbarInUse)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        background?.also { bg ->
            toolbar.setBackgroundResource(bg)
        }
    }

    protected fun hideToolbarBase() {
        toolbarBase.gone()
    }

    protected fun showToolbarBase() {
        toolbarBase.visible()
    }

    protected fun getToolbar(): Toolbar {
        return toolbarInUse
    }

    // Set title
    protected fun showTitle(title: Any? = null) {
        // Set title
        val result = when (title) {
            is CharSequence -> title.toString()
            is String -> title
            is Int -> getString(title)
            else -> title.toString()
        }
        toolbarInUse.title = result
    }

    // Show Back icon
    protected fun enableHomeAsUp(icon: Int = R.drawable.ic_back_white, up: () -> Unit) {
        toolbarInUse.run {
            setNavigationIcon(icon)
            setNavigationOnClickListener { up() }
        }
    }
}
