package com.expedia.bookings.test.stepdefs.phone.account

import com.expedia.bookings.test.pagemodels.appengagement.LogInScreen
import com.expedia.bookings.test.support.User
import com.expedia.bookings.test.support.Users
import cucumber.api.java.en.And

class AccountScreenSteps {

    @And("^I login with \"(.*?)\" tier user$")
    @Throws(Throwable::class)
    fun logInGivenUserTier(userTier: String) {
        val user = Users().findUser("tier", userTier)
        logInToTheApp(user!!)
    }

    @And("^I login with user, which has$")
    @Throws(Throwable::class)
    fun logInGivenParameters(searchParams: Map<String, String>) {
        val user = Users().findUser(searchParams)
        logInToTheApp(user!!)
    }

    @And("^I login to Expedia with credentials \"(.*?)\":\"(.*?)\" $")
    @Throws(Throwable::class)
    fun iLogInWithCredentials(email: String, password: String) {
        logInToTheApp(User(email, password))
    }

    fun logInToTheApp(user: User) {
        if (user.type.toLowerCase() == "facebook") {
            LogInScreen.clickSignInWithExpediaButton()
            LogInScreen.clickSignInWithFacebookButton()
            LogInScreen.FacebookWebSignIn.waitForViewToLoad()
            LogInScreen.FacebookWebSignIn.typeInEmail(user.email)
            LogInScreen.FacebookWebSignIn.typeInPassword(user.password)
            LogInScreen.FacebookWebSignIn.clickLogIn()
            LogInScreen.FacebookWebConfirmLogin.waitForViewToLoad()
            LogInScreen.FacebookWebConfirmLogin.clickContinue()
        } else if (user.type.toLowerCase() == "expedia") {
            LogInScreen.clickSignInWithExpediaButton()
            LogInScreen.typeTextEmailEditText(user.email)
            LogInScreen.typeTextPasswordEditText(user.password)
            LogInScreen.clickOnLoginButton()
        }
    }
}
