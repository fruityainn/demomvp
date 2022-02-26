package com.hide.videophoto.ui.security.question

import android.content.Context
import com.hide.videophoto.common.ext.addToCompositeDisposable
import com.hide.videophoto.common.ext.applyIOWithAndroidMainThread
import com.hide.videophoto.common.ext.authModel
import com.hide.videophoto.data.interactor.DBInteractor
import com.hide.videophoto.ui.base.BasePresenterImp

class QuestionPresenterImp(private val ctx: Context) : BasePresenterImp<QuestionView>(ctx) {

    private val dbInteractor by lazy { DBInteractor(ctx) }

    fun setSecurityQuestion(question: String, answer: String) {
        view?.also { v ->
            ctx.authModel.apply {
                securityQuestion = question
                securityAnswer = answer.lowercase()
            }
            dbInteractor.updateUser(ctx.authModel)
                .applyIOWithAndroidMainThread()
                .subscribe(
                    {
                        if (it > 0) {
                            v.onSettingQuestionSuccess()
                        } else {
                            ctx.authModel.apply {
                                securityQuestion = null
                                securityAnswer = null
                            }

                            v.onQueryDbError()
                        }
                    },
                    {
                        ctx.authModel.apply {
                            securityQuestion = null
                            securityAnswer = null
                        }

                        v.onQueryDbError()
                    }
                )
                .addToCompositeDisposable(compositeDisposable)
        }
    }
}