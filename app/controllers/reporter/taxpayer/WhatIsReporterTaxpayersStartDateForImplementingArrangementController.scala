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

package controllers.reporter.taxpayer

import java.time.LocalDate

import controllers.actions._
import controllers.mixins.{CheckRoute, RoutingSupport}
import forms.taxpayer.WhatIsTaxpayersStartDateForImplementingArrangementFormProvider
import helpers.DateHelper.dateFormatterNumericDMY
import helpers.JourneyHelpers._
import javax.inject.Inject
import models.ReporterOrganisationOrIndividual.Individual
import models.{Mode, UserAnswers}
import navigation.NavigatorForReporter
import pages.reporter.ReporterOrganisationOrIndividualPage
import pages.reporter.taxpayer.ReporterTaxpayersStartDateForImplementingArrangementPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.{DateInput, NunjucksSupport}

import scala.concurrent.{ExecutionContext, Future}

class WhatIsReporterTaxpayersStartDateForImplementingArrangementController @Inject()(
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    navigator: NavigatorForReporter,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: WhatIsTaxpayersStartDateForImplementingArrangementFormProvider,
    val controllerComponents: MessagesControllerComponents,
    renderer: Renderer
)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with NunjucksSupport with RoutingSupport {

  val numberOfMonthsToAdd = 6
  val form = formProvider()

  private def redirect(checkRoute: CheckRoute, value: Option[LocalDate], index: Int = 0): Call =
    navigator.routeMap(ReporterTaxpayersStartDateForImplementingArrangementPage)(checkRoute)(value)(index)

  private def actionUrl(mode: Mode): String = routes.WhatIsReporterTaxpayersStartDateForImplementingArrangementController.onSubmit(mode).url

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val preparedForm = request.userAnswers.get(ReporterTaxpayersStartDateForImplementingArrangementPage) match {
        case Some(value) => form.fill(value)
        case None        => form
      }

      val viewModel = DateInput.localDate(preparedForm("value"))

      val json = Json.obj (
        "form" -> preparedForm,
        "mode" -> mode,
        "date" -> viewModel,
        "exampleDate" -> LocalDate.now.plusMonths (numberOfMonthsToAdd).format (dateFormatterNumericDMY),
        "actionUrl" -> actionUrl(mode)
      ) ++ contentProvider(request.userAnswers)

      renderer.render ("implementingArrangementDate.njk", json).map (Ok (_) )

  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>  {

          val viewModel = DateInput.localDate(formWithErrors("value"))

          val json = Json.obj(
            "form" -> formWithErrors,
            "mode" -> mode,
            "date" -> viewModel,
            "exampleDate" -> LocalDate.now.plusMonths(numberOfMonthsToAdd).format(dateFormatterNumericDMY),
            "actionUrl" -> actionUrl(mode)
          ) ++ contentProvider(request.userAnswers)

          renderer.render("implementingArrangementDate.njk", json).map(BadRequest(_))
        },
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(ReporterTaxpayersStartDateForImplementingArrangementPage, value))
            _              <- sessionRepository.set(updatedAnswers)
            checkRoute                    =  toCheckRoute(mode, updatedAnswers)
          } yield Redirect(redirect(checkRoute, Some(value)))
      )
  }

  private def contentProvider(userAnswers: UserAnswers): JsObject = userAnswers.get(ReporterOrganisationOrIndividualPage) match {
     case Some(Individual) => Json.obj(
       "pageTitle" -> "whatIsTaxpayersStartDateForImplementingArrangement.ind.title",
       "pageHeading" -> "whatIsTaxpayersStartDateForImplementingArrangement.ind.heading")
     case _ =>
       Json.obj(
         "pageTitle" -> "whatIsTaxpayersStartDateForImplementingArrangement.org.title",
         "pageHeading" -> "whatIsTaxpayersStartDateForImplementingArrangement.org.heading",
       "name" -> getReporterDetailsOrganisationName(userAnswers))
   }
}

