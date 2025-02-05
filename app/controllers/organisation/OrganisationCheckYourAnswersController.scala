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

package controllers.organisation

import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import handlers.ErrorHandler
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.{NunjucksSupport, SummaryList}
import utils.CheckYourAnswersHelper

import scala.concurrent.ExecutionContext

class OrganisationCheckYourAnswersController @Inject()(
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    errorHandler: ErrorHandler,
    val controllerComponents: MessagesControllerComponents,
    renderer: Renderer
)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with NunjucksSupport {

  def onPageLoad(id: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val helper = new CheckYourAnswersHelper(request.userAnswers)

      val organisationDetails: Seq[SummaryList.Row] =
        helper.organisationName(id).toSeq ++
          helper.buildOrganisationAddressGroup(id) ++
          helper.buildOrganisationEmailAddressGroup(id)

      val countryDetails: Seq[SummaryList.Row] =
        helper.buildTaxResidencySummaryForOrganisation(id)

      renderer.render(
        "organisation/check-your-answers-organisation.njk",
        Json.obj("organisationSummary" -> organisationDetails,
          "countrySummary" -> countryDetails
        )
      ).map(Ok(_))
  }

}
