package com.expedia.account

import com.expedia.account.server.MockExpediaAccountApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class MockAccountService(siteId: Int, langId: Int, clientId: String) :
        AccountService(MockExpediaAccountApi(), siteId, langId, clientId, Schedulers.io(), AndroidSchedulers.mainThread())
