/*
 * Copyright 2018 HM Revenue & Customs
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
import play.api.libs.json.Json
import uk.gov.hmrc.play.test.UnitSpec

class CustomerInformationSpec extends UnitSpec {

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

    "parse to JSON" when {

      "all fields are present" in {
        val result = Json.toJson(fullCustomerInfoModel)
        result shouldBe fullCustomerInfoJson
      }

      "the minimum number of fields are present" in {
        val result = Json.toJson(minCustomerInfoModel)
        result shouldBe minCustomerInfoJson
      }
    }
  }
}
