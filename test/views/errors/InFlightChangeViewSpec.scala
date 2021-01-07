/*
 * Copyright 2021 HM Revenue & Customs
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

package views.errors

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.ViewBaseSpec
import views.html.errors.InFlightChangeView

class InFlightChangeViewSpec extends ViewBaseSpec {

  val injectedView: InFlightChangeView = injector.instanceOf[InFlightChangeView]

  object Selectors {
    val heading = "h1"
    val paragraphOne = "article > p:nth-child(2)"
    val paragraphTwo = "article > p:nth-child(3)"
    val paragraphThree = "article > p:nth-child(4)"
    val backToAccountDetails = "article > a"
  }

  "The Inflight change pending view" when {

    lazy val view = injectedView()
    lazy implicit val document: Document = Jsoup.parse(view.body)

    "have the correct title" in {
      document.title shouldBe "There is already a change pending - VAT - GOV.UK"
    }

    "have the correct heading" in {
      elementText(Selectors.heading) shouldBe "There is already a change pending"
    }

    "have the correct information in the first paragraph" in {
      elementText(Selectors.paragraphOne) shouldBe
        "We are dealing with a recent request to change something on this VAT account."
    }

    "have the correct information in the second paragraph" in {
      elementText(Selectors.paragraphTwo) shouldBe
        "Until we accept that request, you cannot make a further change."
    }

    "have the correct information in the third paragraph" in {
      elementText(Selectors.paragraphThree) shouldBe
        "HMRC accepts or rejects changes to VAT accounts within 2 working days."
    }

    "have a link" which {

      "has the correct text" in {
        elementText(Selectors.backToAccountDetails) shouldBe "Back to account details"
      }

      "has the correct href" in {
        element(Selectors.backToAccountDetails).attr("href") shouldBe mockConfig.btaAccountDetailsUrl
      }
    }
  }
}
