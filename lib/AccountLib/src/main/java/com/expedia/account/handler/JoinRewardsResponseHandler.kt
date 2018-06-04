package com.expedia.account.handler

import com.expedia.account.data.JoinRewardsResponse
import com.expedia.account.AccountService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

class JoinRewardsResponseHandler(private val accountService: AccountService) {

    fun joinRewards(joinRewardsListener: PublishSubject<JoinRewardsResponse>) {
        accountService.joinRewards().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(joinRewardsListener)
    }
}
