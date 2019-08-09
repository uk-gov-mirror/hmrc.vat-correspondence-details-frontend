/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pages.contactNumbers

import common.SessionKeys.phoneNumberChangeSuccessful
import pages.BasePageISpec
import play.api.http.Status
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import play.api.test.Helpers.OK
import stubs.ContactPreferencesStub

class ContactNumbersChangeSuccessPageSpec extends BasePageISpec {

  val path = "/telephone-numbers-confirmation"

  "Calling the Website Change Success (.show) route" when {

    def show: WSResponse = get(path, Map(phoneNumberChangeSuccessful -> "true"))

    "the user is a authenticated" when {

      "there has been a change made to one of the contact numbers, and a success response is received from Contact Prefs" should {

        "load successfully" in {

          given.user.isAuthenticated

          ContactPreferencesStub.getContactPrefs(OK, Json.obj("preference" -> "DIGITAL"))

          val result = show

          result should have(
            httpStatus(Status.OK),
            pageTitle(messages("contactNumbersChangeSuccess.title"))
          )
        }
      }
    }
  }
}
