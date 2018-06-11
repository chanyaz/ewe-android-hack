package com.expedia.account.handler

import com.expedia.account.AccountService
import com.expedia.account.data.JoinRewardsResponse
import io.reactivex.Observer

class JoinRewardsResponseHandler(private val accountService: AccountService) {

    fun joinRewards(joinRewardsListener: Observer<JoinRewardsResponse>) {
        accountService.joinRewards()
                .subscribe(joinRewardsListener)
    }
}
