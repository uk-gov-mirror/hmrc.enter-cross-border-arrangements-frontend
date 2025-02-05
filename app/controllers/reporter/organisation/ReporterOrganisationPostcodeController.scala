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

package controllers.reporter.organisation

import connectors.AddressLookupConnector
import controllers.actions._
import controllers.mixins.{CheckRoute, RoutingSupport}
import forms.PostcodeFormProvider
import helpers.JourneyHelpers.getReporterDetailsOrganisationName
import javax.inject.Inject
import models.Mode
import navigation.NavigatorForReporter
import pages.AddressLookupPage
import pages.reporter.organisation.ReporterOrganisationPostcodePage
import play.api.data.FormError
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.NunjucksSupport

import scala.concurrent.{ExecutionContext, Future}

class ReporterOrganisationPostcodeController @Inject()(
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: NavigatorForReporter,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: PostcodeFormProvider,
  addressLookupConnector: AddressLookupConnector,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer
)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with NunjucksSupport with RoutingSupport {

  private val form = formProvider()

  private def manualAddressURL(id: Int, mode: Mode): String = routes.ReporterOrganisationAddressController.onSubmit(id, mode).url

  private def actionUrl(id: Int, mode: Mode) = routes.ReporterOrganisationPostcodeController.onSubmit(id, mode).url

  def onPageLoad(id: Int, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val preparedForm = request.userAnswers.get(ReporterOrganisationPostcodePage, id) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      val json = Json.obj(
        "form" -> preparedForm,
        "displayName" -> getReporterDetailsOrganisationName(request.userAnswers, id),
        "manualAddressURL" -> manualAddressURL(id, mode),
        "actionUrl" -> actionUrl(id, mode),
        "individual" -> false,
        "mode" -> mode
      )

      renderer.render("postcode.njk", json).map(Ok(_))
  }

  def redirect(id: Int, checkRoute: CheckRoute, value: Option[String], index: Int = 0): Call =
    navigator.routeMap(ReporterOrganisationPostcodePage)(checkRoute)(id)(value)(index)

  def onSubmit(id: Int, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

    val formReturned = form.bindFromRequest()

    formReturned.fold(
        formWithErrors => {

          val json = Json.obj(
            "form" -> formWithErrors,
            "displayName" -> getReporterDetailsOrganisationName(request.userAnswers, id),
            "manualAddressURL" -> manualAddressURL(id, mode),
            "actionUrl" -> actionUrl(id, mode),
            "individual" -> false,
            "mode" -> mode
          )

          {for {
            updatedAnswers <- Future.fromTry(request.userAnswers.remove(ReporterOrganisationPostcodePage, id))
            _              <- sessionRepository.set(updatedAnswers)
          } yield renderer.render("postcode.njk", json).map(BadRequest(_))}.flatten
        },
      postCode => {
        addressLookupConnector.addressLookupByPostcode(postCode).flatMap {
          case Nil =>
            val formError = formReturned.withError(FormError("postcode", List("postcode.error.notFound")))

            val json = Json.obj(
              "form" -> formError,
              "displayName" -> getReporterDetailsOrganisationName(request.userAnswers, id),
              "manualAddressURL" -> manualAddressURL(id, mode),
              "actionUrl" -> actionUrl(id, mode),
              "individual" -> false,
              "mode" -> mode
            )

            {for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(ReporterOrganisationPostcodePage, id, postCode))
              _              <- sessionRepository.set(updatedAnswers)
            } yield renderer.render("postcode.njk", json).map(BadRequest(_))}.flatten
          case addresses =>
            for {
              updatedAnswers              <- Future.fromTry(request.userAnswers.set(ReporterOrganisationPostcodePage, id, postCode))
              updatedAnswersWithAddresses <- Future.fromTry(updatedAnswers.set(AddressLookupPage, id, addresses))
              _                           <- sessionRepository.set(updatedAnswersWithAddresses)
              checkRoute                   =  toCheckRoute(mode, updatedAnswers, id)
            } yield Redirect(redirect(id, checkRoute, Some(postCode)))
        }
      }
    )
  }
}