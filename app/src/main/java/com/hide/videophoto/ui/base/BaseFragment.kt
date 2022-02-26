package com.hide.videophoto.ui.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.hide.videophoto.common.util.CommonUtil

abstract class BaseFragment<V : BaseView, P : BasePresenterImp<V>> : Fragment(), BaseView {

    protected lateinit var parentActivity: AppCompatActivity
    protected val self: Fragment by lazy { this }
    protected lateinit var presenter: P

    override fun onAttach(context: Context) {
        super.onAttach(context)
        parentActivity = context as AppCompatActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = initPresenter()
        presenter.attachView(initView())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(getLayoutId(), container, false)
        initWidgets(rootView)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Close keyboard when user touch outside
        CommonUtil.closeKeyboardWhileClickOutSide(parentActivity, view)
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
    * return layout id
    * */
    abstract fun getLayoutId(): Int

    /*
    * Set up widgets such as EditText, TextView, RecyclerView, etc
    * */
    abstract fun initWidgets(rootView: View)
}
