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

package views.landlineNumber

import assets.BaseTestConstants.testValidationLandline
import controllers.landlineNumber.routes
import forms.LandlineNumberForm.landlineNumberForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.ViewBaseSpec
import views.html.landlineNumber.CaptureLandlineNumberView

class CaptureLandlineNumberViewSpec extends ViewBaseSpec {

  val injectedView: CaptureLandlineNumberView = inject[CaptureLandlineNumberView]

  "The Capture Contact Number page" when {

    "the user is not an agent" when {

      "there are no errors in the form" should {

        val view = injectedView(landlineNumberForm(testValidationLandline), testValidationLandline)(user, messages, mockConfig)
        implicit val document: Document = Jsoup.parse(view.body)
        val fieldLabel: String = "#content > article > form > div > label "

        "have the correct title" in {
          document.title shouldBe "What is the landline number? - Business tax account - GOV.UK"
        }

        "have the correct heading" in {
          elementText("h1") shouldBe "What is the landline number?"
        }

        "have the correct field hint" in {
          elementText(s"$fieldLabel > span.form-hint") shouldBe
            "You need to enter the country code for international numbers, like 00441632 960000. You cannot enter a '+'."
        }

        "have the correct visually hidden text" in {
          elementText(s"$fieldLabel > span.visuallyhidden") shouldBe "What is the landline number?"
        }

        "have a link to remove the landline" which {

          "has the correct text" in {
            elementText("#remove-landline") shouldBe "Remove landline number"
          }

          "has the correct link location" in {
            element("#remove-landline").attr("href") shouldBe routes.ConfirmRemoveLandlineController.show().url
          }
        }

        "have a button" which {

          "has the correct text" in {
            elementText(".button") shouldBe "Continue"
          }

          "has the correct link location" in {
            element("form").attr("action") shouldBe routes.CaptureLandlineNumberController.submit().url
          }
        }
      }

      "there are errors in the form" should {
        val view = injectedView(landlineNumberForm(testValidationLandline).bind(
          Map("landlineNumber" -> testValidationLandline)
        ), testValidationLandline)(user, messages, mockConfig)
        implicit val document: Document = Jsoup.parse(view.body)

        "have the correct document title" in {
          document.title shouldBe "Error: What is the landline number? - Business tax account - GOV.UK"
        }

        "have a form error box" which {

          "has the correct error message" in {
            elementText("#landlineNumber-error-summary") shouldBe "You have not made any changes to the landline number"
          }
        }

        "have the correct error notification text above the input box" in {
          elementText(".error-notification") shouldBe "You have not made any changes to the landline number"
        }

        "display the error summary" in {
          element("#error-summary-heading").text() shouldBe "There is a problem"
        }
      }
    }

    "the user is an agent" when {

      "there are no errors in the form" should {
        val view = injectedView(landlineNumberForm(testValidationLandline), testValidationLandline)(agent, messages, mockConfig)
        implicit val document: Document = Jsoup.parse(view.body)

        "have the correct title" in {
          document.title shouldBe "What is the landline number? - Your client’s VAT details - GOV.UK"
        }
      }
    }
  }
}
