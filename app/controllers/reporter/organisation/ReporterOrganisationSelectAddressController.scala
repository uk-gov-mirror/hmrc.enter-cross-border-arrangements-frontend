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
import forms.SelectAddressFormProvider
import helpers.JourneyHelpers.{getReporterDetailsOrganisationName, hasValueChanged, pageHeadingProvider}
import javax.inject.Inject
import models.{AddressLookup, Mode}
import navigation.NavigatorForOrganisation
import pages.reporter.ReporterSelectedAddressLookupPage
import pages.reporter.organisation.{ReporterOrganisationPostcodePage, ReporterOrganisationSelectAddressPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.{NunjucksSupport, Radios}

import scala.concurrent.{ExecutionContext, Future}

class ReporterOrganisationSelectAddressController @Inject()(
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    navigator: NavigatorForOrganisation,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: SelectAddressFormProvider,
    val controllerComponents: MessagesControllerComponents,
    addressLookupConnector: AddressLookupConnector,
    renderer: Renderer
)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with NunjucksSupport with RoutingSupport {

  private val form = formProvider()

  private def manualAddressURL(mode: Mode): String = routes.ReporterOrganisationAddressController.onPageLoad(mode).url

  private def actionUrl(mode: Mode): String = routes.ReporterOrganisationSelectAddressController.onPageLoad(mode).url

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val postCode = request.userAnswers.get(ReporterOrganisationPostcodePage) match {
        case Some(postCode) => postCode.replaceAll(" ", "").toUpperCase
        case None => ""
      }

      addressLookupConnector.addressLookupByPostcode(postCode) flatMap {
        case Nil => Future.successful(Redirect(manualAddressURL(mode)))
        case addresses =>

          val preparedForm = request.userAnswers.get(ReporterOrganisationSelectAddressPage) match {
            case None => form
            case Some(value) => form.fill(value)
          }
          val addressItems: Seq[Radios.Radio] = addresses.map(address =>
            Radios.Radio(label = msg"${formatAddress(address)}", value = s"${formatAddress(address)}"))
          val radios = Radios(field = preparedForm("value"), items = addressItems)

            val json = Json.obj(
              "form" -> preparedForm,
              "mode" -> mode,
              "manualAddressURL" -> manualAddressURL(mode),
              "displayName" -> getReporterDetailsOrganisationName(request.userAnswers),
              "actionUrl" -> actionUrl(mode),
              "radios" -> radios
            )

            renderer.render("selectAddress.njk", json).map(Ok(_))
      } recover {
        case _: Exception => Redirect(manualAddressURL(mode))
      }
  }

  def redirect(checkRoute: CheckRoute, value: Option[String], isAlt: Boolean): Call =
    if (isAlt) {
      navigator.routeAltMap(ReporterOrganisationSelectAddressPage)(checkRoute)(value)(0)
    }
    else {
      navigator.routeMap(ReporterOrganisationSelectAddressPage)(checkRoute)(value)(0)
    }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val postCode = request.userAnswers.get(ReporterOrganisationPostcodePage) match {
        case Some(postCode) => postCode.replaceAll(" ", "").toUpperCase
        case None => ""
      }

      addressLookupConnector.addressLookupByPostcode(postCode) flatMap {
        addresses =>
        val addressItems: Seq[Radios.Radio] = addresses.map(address =>
          Radios.Radio(label = msg"${formatAddress(address)}", value = s"${formatAddress(address)}")
          )

        form.bindFromRequest().fold(
          formWithErrors => {
            val radios = Radios(field = formWithErrors("value"), items = addressItems)

            val json = Json.obj(
              "form" -> formWithErrors,
              "mode" -> mode,
              "manualAddressURL" -> manualAddressURL(mode),
              "displayName" -> getReporterDetailsOrganisationName(request.userAnswers),
              "actionUrl" -> actionUrl(mode),
              "pageHeading" -> pageHeadingProvider("", getReporterDetailsOrganisationName(request.userAnswers)),
              "radios" -> radios
            )

            renderer.render("selectAddress.njk", json).map(BadRequest(_))
          },
          value => {
            val addressToStore: AddressLookup = addresses.find(formatAddress(_) == value).getOrElse(throw new Exception("Cannot get address"))

            val redirectUsers = hasValueChanged(value, ReporterOrganisationSelectAddressPage, mode, request.userAnswers)

            for {
              updatedAnswers            <- Future.fromTry(request.userAnswers.set(ReporterOrganisationSelectAddressPage, value))
              updatedAnswersWithAddress <- Future.fromTry(updatedAnswers.set(ReporterSelectedAddressLookupPage, addressToStore))
              _                         <- sessionRepository.set(updatedAnswersWithAddress)
              checkRoute                =  toCheckRoute(mode, updatedAnswersWithAddress)
            } yield Redirect(redirect(checkRoute, Some(value), redirectUsers))
          }
        )
      } recover {
        case _: Exception => Redirect(manualAddressURL(mode))
      }
  }

  private def formatAddress(address: AddressLookup): String = {
    val lines = Seq(address.addressLine1, address.addressLine2, address.addressLine3, address.addressLine4).flatten.mkString(", ")
    val county = address.county.fold("")(county => s"$county, ")

    s"$lines, ${address.town}, $county${address.postcode}"
  }
}