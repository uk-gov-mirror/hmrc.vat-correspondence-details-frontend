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

package controllers

import audit.AuditingService
import common.SessionKeys
import config.{AppConfig, ErrorHandler}
import controllers.predicates.{AuthPredicateComponents, InFlightPPOBPredicate}
import forms.WebsiteForm.websiteForm
import javax.inject.{Inject, Singleton}
import play.api.mvc._
import services.VatSubscriptionService
import views.html.CaptureWebsiteView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CaptureWebsiteController @Inject()(val authComps: AuthPredicateComponents,
                                       val inflightCheck: InFlightPPOBPredicate,
                                       override val mcc: MessagesControllerComponents,
                                       val vatSubscriptionService: VatSubscriptionService,
                                       val errorHandler: ErrorHandler,
                                       val auditService: AuditingService,
                                         captureWebsiteView: CaptureWebsiteView,
                                       implicit val appConfig: AppConfig) extends BaseController(mcc, authComps) {

  implicit val ec: ExecutionContext = mcc.executionContext

  def show: Action[AnyContent] = (allowAgentPredicate andThen inflightCheck).async { implicit user =>
    val validationWebsite: Future[Option[String]] = user.session.get(SessionKeys.validationWebsiteKey) match {
      case Some(website) => Future.successful(Some(website))
      case _ =>
        vatSubscriptionService.getCustomerInfo(user.vrn) map {
          case Right(details) => Some(details.ppob.websiteAddress.getOrElse(""))
          case _ => None
        }
    }

    val prepopulationWebsite: Future[String] = validationWebsite map { validation =>
      user.session.get(SessionKeys.prepopulationWebsiteKey)
        .getOrElse(validation.getOrElse(""))
    }

    for {
      validation <- validationWebsite
      prepopulation <- prepopulationWebsite
    } yield {
      validation match {
        case Some(valWebsite) =>
          Ok(captureWebsiteView(websiteForm(valWebsite).fill(prepopulation), false, valWebsite))
            .addingToSession(SessionKeys.validationWebsiteKey -> valWebsite)
        case _ => errorHandler.showInternalServerError
      }
    }
  }

  def submit: Action[AnyContent] = (allowAgentPredicate andThen inflightCheck).async { implicit user =>
    val validationWebsite: Option[String] = user.session.get(SessionKeys.validationWebsiteKey)

    validationWebsite match {
      case Some(validation) => websiteForm(validation).bindFromRequest.fold(
        errorForm => {
          val notChanged: Boolean = errorForm.errors.head.message == user.messages.apply("captureWebsite.error.notChanged")
          Future.successful(BadRequest(captureWebsiteView(errorForm, notChanged, validation)))

        },
        website     => {
          Future.successful(Redirect(controllers.routes.ConfirmWebsiteController.show())
            .addingToSession(SessionKeys.prepopulationWebsiteKey -> website))
        }
      )
      case None => Future.successful(errorHandler.showInternalServerError)
    }
  }

}
