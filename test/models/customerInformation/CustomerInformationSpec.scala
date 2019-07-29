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

package models.customerInformation

import assets.CustomerInfoConstants._
import uk.gov.hmrc.play.test.UnitSpec

class CustomerInformationSpec extends UnitSpec {

  val modelNoPending = CustomerInformation(PPOB(
    minPPOBAddressModel,
    None,
    None
  ), None, None, None, None, None)

  "CustomerInformation" should {

    "parse from JSON" when {

      "all fields are present" in {
        val result = fullCustomerInfoJson.as[CustomerInformation]
        result shouldBe fullCustomerInfoModel
      }

      "the minimum number of fields are present" in {
        val result = minCustomerInfoJson.as[CustomerInformation]
        result shouldBe minCustomerInfoModel
      }
    }

    "pendingEmailAddress" when {

      "there are no pending changes" should {

        "return false" in {
          modelNoPending.pendingEmailAddress shouldBe false
        }
      }

      "pending email and approved email are not present" should {

        val model = CustomerInformation(
          PPOB(
            minPPOBAddressModel,
            None,
            None
          ),
          Some(PendingChanges(
            Some(PPOB(
              minPPOBAddressModel,
              None,
              None
            ))
          )),
          None, None, None, None
        )

        "return false" in {
          model.pendingEmailAddress shouldBe false
        }
      }

      "pending email does not match approved email" should {

        val model = CustomerInformation(
          PPOB(
            minPPOBAddressModel,
            Some(ContactDetails(None, None, None, Some("email"), None)),
            None
          ),
          Some(PendingChanges(
            Some(PPOB(
              minPPOBAddressModel,
              Some(ContactDetails(None, None, None, Some("different email"), None)),
              None
            ))
          )),
          None, None, None, None
        )

        "return true" in {
          model.pendingEmailAddress shouldBe true
        }
      }

      "pending email matches approved email" should {

        val model = CustomerInformation(
          PPOB(
            minPPOBAddressModel,
            Some(ContactDetails(None, None, None, Some("email"), None)),
            None
          ),
          Some(PendingChanges(
            Some(PPOB(
              minPPOBAddressModel,
              Some(ContactDetails(None, None, None, Some("email"), None)),
              None
            ))
          )),
          None, None, None, None
        )

        "return false" in {
          model.pendingEmailAddress shouldBe false
        }
      }
    }

    "pendingPPOBAddress" when {

      "there are no pending changes" should {

        "return false" in {
          modelNoPending.pendingPPOBAddress shouldBe false
        }
      }

      "pending PPOB does not match approved PPOB" should {

        val model = CustomerInformation(
          PPOB(
            PPOBAddress("Add", None, None, None, None, None, ""),
            None,
            None
          ),
          Some(PendingChanges(
            Some(PPOB(
              PPOBAddress("Address", None, None, None, None, None, ""),
              None,
              None
            ))
          )),
          None, None, None, None
        )

        "return true" in {
          model.pendingPPOBAddress shouldBe true
        }
      }

      "pending PPOB matches approved PPOB" should {

        val model = CustomerInformation(
          PPOB(
            minPPOBAddressModel,
            None,
            None
          ),
          Some(PendingChanges(
            Some(PPOB(
              minPPOBAddressModel,
              None,
              None
            ))
          )),
          None, None, None, None
        )

        "return false" in {
          model.pendingPPOBAddress shouldBe false
        }
      }
    }
  }

  "Calling .entityName" when {

    "the model contains a trading name" should {

      "return the trading name" in {
        val result: Option[String] = fullCustomerInfoModel.entityName
        result shouldBe Some("PepsiMac")
      }
    }

    "the model does not contain a trading name or organisation name" should {

      "return the first and last name" in {
        val customerInfoSpecific = fullCustomerInfoModel.copy(tradingName = None, organisationName = None)
        val result: Option[String] = customerInfoSpecific.entityName
        result shouldBe Some("Pepsi Mac")
      }
    }

    "the model does not contain a trading name, first name or last name" should {

      "return the organisation name" in {
        val customerInfoSpecific = fullCustomerInfoModel.copy(tradingName = None, firstName = None, lastName = None)
        val result: Option[String] = customerInfoSpecific.entityName
        result shouldBe Some("PepsiMac Ltd")
      }
    }

    "the model does not contains a trading name, organisation name, or individual names" should {

      "return None" in {
        val result: Option[String] = minCustomerInfoModel.entityName
        result shouldBe None
      }
    }
  }
}
