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

package controllers.reporter.intermediary

import controllers.actions._
import controllers.mixins.{CheckRoute, RoutingSupport}
import forms.reporter.intermediary.IntermediaryWhyReportInUKFormProvider
import javax.inject.Inject
import models.Mode
import models.reporter.intermediary.IntermediaryWhyReportInUK
import navigation.NavigatorForReporter
import pages.reporter.intermediary.IntermediaryWhyReportInUKPage
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.{Html, NunjucksSupport}

import scala.concurrent.{ExecutionContext, Future}

class IntermediaryWhyReportInUKController @Inject()(
   override val messagesApi: MessagesApi,
   sessionRepository: SessionRepository,
   navigator: NavigatorForReporter,
   identify: IdentifierAction,
   getData: DataRetrievalAction,
   requireData: DataRequiredAction,
   formProvider: IntermediaryWhyReportInUKFormProvider,
   val controllerComponents: MessagesControllerComponents,
   renderer: Renderer
)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with NunjucksSupport with RoutingSupport {

  private val form = formProvider()

  def onPageLoad(id: Int, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val preparedForm = request.userAnswers.get(IntermediaryWhyReportInUKPage, id) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      val json = Json.obj(
        "form"   -> preparedForm,
        "id" -> id,
        "mode"   -> mode,
        "radios"  -> IntermediaryWhyReportInUK.radios(preparedForm),
        "infoWithHint"-> whyReporterInfo
      )

      renderer.render("reporter/intermediary/IntermediaryWhyReportInUK.njk", json).map(Ok(_))
  }

  def redirect(id: Int, checkRoute: CheckRoute, value: Option[IntermediaryWhyReportInUK]): Call =
    navigator.routeMap(IntermediaryWhyReportInUKPage)(checkRoute)(id)(value)(0)

  def onSubmit(id: Int, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors => {

          val json = Json.obj(
            "form"   -> formWithErrors,
            "id" -> id,
            "mode"   -> mode,
            "radios" -> IntermediaryWhyReportInUK.radios(formWithErrors),
            "infoWithHint"-> whyReporterInfo
          )

          renderer.render("reporter/intermediary/IntermediaryWhyReportInUK.njk", json).map(BadRequest(_))
        },
        value => {

          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(IntermediaryWhyReportInUKPage, id, value))
            _              <- sessionRepository.set(updatedAnswers)
            checkRoute     =  toCheckRoute(mode, updatedAnswers, id)
          } yield Redirect(redirect(id, checkRoute, Some(value)))

        }
      )
  }

  private def whyReporterInfo(implicit messages: Messages): Html = {
    Html(s"<p id='roleInfo' class='govuk-body'>${{ messages("whyReportInUK.info") }}</p> ${{ messages("whyReportInUK.hint") }}")
  }
}
