package com.expedia.bookings.services

import com.expedia.account.data.AccountResponse
import com.expedia.account.data.FacebookLinkResponse
import com.expedia.account.data.JoinRewardsResponse
import com.expedia.account.server.ExpediaAccountApi
import io.reactivex.Observable

class MockJoinRewardsApi(private val isSuccess: Boolean) : ExpediaAccountApi {

    override fun signIn(email: String?, password: String?, staySignedIn: Boolean, recaptchaResponseToken: String?, extraParams: MutableMap<String, String>?): Observable<AccountResponse> {
        return Observable.just(AccountResponse())
    }

    override fun signInProfileOnly(profileOnly: Boolean, extraParams: MutableMap<String, String>?): Observable<AccountResponse> {
        return Observable.just(AccountResponse())
    }

    override fun createUser(email: String?, password: String?, firstName: String?, lastName: String?, expediaEmailOptin: Boolean, staySignedIn: Boolean, enrollInLoyalty: Boolean, recaptchaResponseToken: String?, extraParams: MutableMap<String, String>?): Observable<AccountResponse> {
        return Observable.just(AccountResponse())
    }

    override fun joinRewards(): Observable<JoinRewardsResponse> {
        val response = JoinRewardsResponse()

        if (isSuccess) {
            response.loyaltyMembershipActive = true
        } else {
            val mobileError = JoinRewardsResponse.MobileError()
            response.errors = listOf(mobileError)
        }

        return Observable.just(response)
    }

    override fun facebookAutoLogin(provider: String?, userId: String?, accessToken: String?): Observable<FacebookLinkResponse> {
        return Observable.just(FacebookLinkResponse())
    }

    override fun facebookLinkNewAccount(provider: String?, facebookUserId: String?, facebookAccessToken: String?, facebookEmailAddress: String?): Observable<FacebookLinkResponse> {
        return Observable.just(FacebookLinkResponse())
    }

    override fun facebookLinkExistingAccount(provider: String?, facebookUserId: String?, facebookAccessToken: String?, expediaEmailAddress: String?, expediaPassword: String?): Observable<FacebookLinkResponse> {
        return Observable.just(FacebookLinkResponse())
    }
}
