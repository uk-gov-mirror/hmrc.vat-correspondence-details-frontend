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

package controllers.predicates.inflight

import common.SessionKeys.inFlightContactDetailsChangeKey
import config.AppConfig
import models.User
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{ActionRefiner, Result}
import play.api.mvc.Results.{Conflict, Redirect}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter
import utils.LoggerUtil.{logDebug, logWarn}

import scala.concurrent.{ExecutionContext, Future}

class InFlightPredicate(inFlightComps: InFlightPredicateComponents,
                        redirectURL: String) extends ActionRefiner[User, User] with I18nSupport {

  implicit val appConfig: AppConfig = inFlightComps.appConfig
  implicit val executionContext: ExecutionContext = inFlightComps.ec
  implicit val messagesApi: MessagesApi = inFlightComps.messagesApi

  override def refine[A](request: User[A]): Future[Either[Result, User[A]]] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))
    implicit val req: User[A] = request

    req.session.get(inFlightContactDetailsChangeKey) match {
      case Some("true") => Future.successful(Left(Conflict(inFlightComps.inFlightChangeView())))
      case Some("false") => Future.successful(Right(req))
      case Some(_) => Future.successful(Left(inFlightComps.errorHandler.showInternalServerError))
      case None => getCustomerInfoCall(req.vrn)
    }
  }

  private def getCustomerInfoCall[A](vrn: String)(implicit hc: HeaderCarrier,
                                                  request: User[A]): Future[Either[Result, User[A]]] =
    inFlightComps.vatSubscriptionService.getCustomerInfo(vrn).map {
      case Right(customerInfo) =>
        customerInfo.pendingChanges match {
          case Some(changes) if changes.ppob.isDefined =>
            (customerInfo.pendingPPOBAddress, customerInfo.pendingEmailAddress) match {
              case (true, false) =>
                logWarn("[InFlightBasePredicate][getCustomerInfoCall] - " +
                  "There is an in-flight PPOB address change. Rendering graceful error page.")
                Left(Conflict(inFlightComps.inFlightChangeView()).addingToSession(inFlightContactDetailsChangeKey -> "true"))
              case (_, true) =>
                logWarn("[InFlightBasePredicate][getCustomerInfoCall] - " +
                  "There is an in-flight email address change. Redirecting to Manage VAT homepage")
                Left(Redirect(inFlightComps.appConfig.manageVatSubscriptionServicePath)
                  .addingToSession(inFlightContactDetailsChangeKey -> "true"))
              case (_, _) =>
                logWarn("[InFlightBasePredicate][getCustomerInfoCall] - There is an in-flight contact details " +
                  "change that is not PPOB or email address. Rendering standard error page.")
                Left(inFlightComps.errorHandler.showInternalServerError
                  .addingToSession(inFlightContactDetailsChangeKey -> "error"))
            }
          case _ =>
            logDebug("[InFlightBasePredicate][getCustomerInfoCall] - There are no in-flight changes. " +
              "Redirecting user to the start of the journey.")
            Left(Redirect(redirectURL).addingToSession(inFlightContactDetailsChangeKey -> "false"))
        }
      case Left(error) =>
        logWarn("[InFlightBasePredicate][getCustomerInfoCall] - " +
          s"The call to the GetCustomerInfo API failed. Error: ${error.message}")
        Left(inFlightComps.errorHandler.showInternalServerError)
    }
}