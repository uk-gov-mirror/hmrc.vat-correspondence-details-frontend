/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers.changePref

import common.SessionKeys
import config.{AppConfig, ErrorHandler}
import controllers.BaseController
import controllers.predicates.AuthPredicateComponents
import controllers.predicates.inflight.InFlightPredicateComponents
import forms.YesNoForm
import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.VatSubscriptionService
import views.html.changePref.EmailToUseView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EmailToUseController @Inject()(val vatSubscriptionService: VatSubscriptionService,
                                     val errorHandler: ErrorHandler,
                                     emailToUseView: EmailToUseView
                                    )(implicit val appConfig: AppConfig,
                                      mcc: MessagesControllerComponents,
                                      authComps: AuthPredicateComponents,
                                      inFlightComps: InFlightPredicateComponents) extends BaseController {

  implicit val ec: ExecutionContext = mcc.executionContext

  def show: Action[AnyContent] = (blockAgentPredicate andThen inFlightEmailPredicate).async { implicit user =>
    val validationEmail: Future[Option[String]] = user.session.get(SessionKeys.validationEmailKey) match {
      case Some(email) => Future.successful(Some(email))
      case _ =>
        vatSubscriptionService.getCustomerInfo(user.vrn) map {
          case Right(details) => Some(details.ppob.contactDetails.flatMap(_.emailAddress).getOrElse(""))
          case _ => None
        }
    }

    validationEmail map {
      case Some(email) => Ok(emailToUseView(YesNoForm.yesNoForm("emailToUse.error"), email))
        .addingToSession(SessionKeys.validationEmailKey -> email)
        .addingToSession(SessionKeys.prepopulationEmailKey -> email)
      case _ => errorHandler.showInternalServerError
    }
  }



}