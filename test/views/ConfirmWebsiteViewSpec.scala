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

package views

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.ConfirmWebsiteView

class ConfirmWebsiteViewSpec extends ViewBaseSpec {

  val injectedView: ConfirmWebsiteView = injector.instanceOf[ConfirmWebsiteView]

  object Selectors {
    val heading = "h1"
    val backLink = "#content > article > a"
    val continueButton = ".button"
    val editLink = "#content > article > p:nth-child(5) > a"
    val newWebsite = "#content > article > p"
  }

  "The Confirm Website view" should {
    lazy val view = injectedView(testWebsite)
    lazy implicit val document: Document = Jsoup.parse(view.body)

    "have the correct title" in {
      document.title shouldBe "Confirm the website address"
    }

    "have the correct heading" in {
      elementText(Selectors.heading) shouldBe "Confirm the website address"
    }

    "have a back link" which {

      "should have the correct text" in {
        elementText(Selectors.backLink) shouldBe "Back"
      }

      "should have the correct back link" in {
        element(Selectors.backLink).attr("href") shouldBe controllers.routes.CaptureWebsiteController.show().url
      }
    }

    "have the website address the user provided" in {
      elementText(Selectors.newWebsite) shouldBe "The new website address is " + testWebsite
    }

    "have a link to edit website address" which {

      "has the correct text" in {
        elementText(Selectors.editLink) shouldBe "Change the website address"
      }

      "has the correct link" in {
        element(Selectors.editLink).attr("href") shouldBe controllers.routes.CaptureWebsiteController.show().url
      }

    }

    "have a continue button" which {

      "has the correct text" in {
        elementText(Selectors.continueButton) shouldBe "Confirm and continue"
      }

    }
  }
}
