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

package controllers.predicates

import assets.BaseTestConstants.internalServerErrorTitle
import common.SessionKeys
import mocks.MockAuth
import org.jsoup.Jsoup
import play.api.http.Status
import play.api.mvc.Results.Ok
import play.api.mvc.{Action, AnyContent}
import play.api.test.FakeRequest
import utils.MaterializerSupport

import scala.concurrent.Future

class AuthPredicateSpec extends MockAuth with MaterializerSupport {

  def target: Action[AnyContent] = mockAuthPredicate.async {
    implicit request => Future.successful(Ok("test"))
  }

  "The AuthPredicateSpec" when {

    "the user is an Agent" when {

      "the Agent has an active HMRC-AS-AGENT enrolment" when {

        "the agent access feature is enabled" when {

          "the request URI is allowed for agents" should {

            "return OK (200)" in {
              mockConfig.features.agentAccessEnabled(true)
              mockAgentAuthorised()
              status(target(fakeRequestWithClientsVRN)) shouldBe Status.OK
            }
          }

          "the request URI is blocked for agents" should {

            lazy val fakeRequest = FakeRequest("GET", "/agent-denied")
              .withSession(SessionKeys.clientVrn -> "123456789")
            lazy val result = await(target(fakeRequest))

            "return Unauthorized (401)" in {
              mockAgentAuthorised()
              status(result) shouldBe Status.UNAUTHORIZED
            }

            "show the agent journey disabled page" in {
              messages(Jsoup.parse(bodyOf(result)).title) shouldBe "You cannot change your client’s correspondence details yet"
            }
          }

          "the Agent does NOT have an Active HMRC-AS-AGENT enrolment" should {

            lazy val result = await(target(fakeRequestWithClientsVRN))

            "return Forbidden" in {
              mockConfig.features.agentAccessEnabled(true)
              mockAgentWithoutEnrolment()
              status(result) shouldBe Status.FORBIDDEN
            }

            "render the Unauthorised Agent page" in {
              messages(Jsoup.parse(bodyOf(result)).title) shouldBe "You can not use this service yet"
            }
          }
        }

        "the agent access feature is disabled" should {

          lazy val result = await(target(fakeRequestWithClientsVRN))

          "return Unauthorized (401)" in {
            mockConfig.features.agentAccessEnabled(false)
            mockAgentAuthorised()
            status(result) shouldBe Status.UNAUTHORIZED
          }

          "show the agent journey disabled page" in {
            messages(Jsoup.parse(bodyOf(result)).title) shouldBe "You cannot change your client’s correspondence details yet"
          }
        }
      }

      "the agent does not have an affinity group" should {

        "return ISE (500)" in {
          mockUserWithoutAffinity()
          status(target(request)) shouldBe Status.INTERNAL_SERVER_ERROR
        }
      }

      "a no active session result is returned from Auth" should {

        lazy val result = await(target(fakeRequestWithClientsVRN))

        "return Unauthorised (401)" in {
          mockMissingBearerToken()
          status(result) shouldBe Status.UNAUTHORIZED
        }

        "render the Unauthorised page" in {
          messages(Jsoup.parse(bodyOf(result)).title) shouldBe "Your session has timed out"
        }
      }

      "an authorisation exception is returned from Auth" should {

        lazy val result = await(target(fakeRequestWithClientsVRN))

        "return Internal Server Error (500)" in {
          mockAuthorisationException()
          status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        }

        "render the Unauthorised page" in {
          messages(Jsoup.parse(bodyOf(result)).title) shouldBe internalServerErrorTitle
        }
      }
    }

    "the user is an Individual (Principle Entity)" when {

      "they have an active HMRC-MTD-VAT enrolment" should {

        "return OK (200)" in {
          mockIndividualAuthorised()
          status(target(request)) shouldBe Status.OK
        }
      }

      "they do NOT have an active HMRC-MTD-VAT enrolment" should {

        lazy val result = await(target(request))

        "return Forbidden (403)" in {
          mockIndividualWithoutEnrolment()
          status(result) shouldBe Status.FORBIDDEN
        }

        "render the Not Signed Up page" in {
          messages(Jsoup.parse(bodyOf(result)).title) shouldBe "You can not use this service yet"
        }
      }
    }
  }
}
