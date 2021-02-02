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

package navigation

import controllers.affected.routes
import controllers.mixins.{CheckRoute, DefaultRouting}
import models.affected.YouHaveNotAddedAnyAffected.YesAddNow
import models.{CheckMode, NormalMode, SelectType}
import pages.Page
import pages.affected.{AffectedCheckYourAnswersPage, AffectedTypePage, YouHaveNotAddedAnyAffectedPage}
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class NavigatorForAffected @Inject()() extends AbstractNavigator {

  override val routeMap:  Page => CheckRoute => Int => Option[Any] => Int => Call = {

    case YouHaveNotAddedAnyAffectedPage =>
      checkRoute => id => value => _ =>
        value match {
          case Some(YesAddNow) => routes.AffectedTypeController.onPageLoad(id, checkRoute.mode)
          case _               => controllers.routes.DisclosureDetailsController.onPageLoad(id)
        }

    case AffectedTypePage =>
      checkRoute => id => value => _ =>
        value match {
          case Some(SelectType.Organisation) =>
            jumpOrCheckYourAnswers(id, controllers.organisation.routes.OrganisationNameController.onPageLoad(id, checkRoute.mode), checkRoute)
          case Some(SelectType.Individual)   =>
            jumpOrCheckYourAnswers(id, controllers.individual.routes.IndividualNameController.onPageLoad(id, checkRoute.mode), checkRoute)
        }

    case AffectedCheckYourAnswersPage => _=> id => _ => _ => routes.YouHaveNotAddedAnyAffectedController.onPageLoad(id)

    case _ =>
      checkRoute => _ => _ => _ => checkRoute.mode match {
        case NormalMode => indexRoute
        case CheckMode  => controllers.routes.IndexController.onPageLoad()
      }
  }

  override val routeAltMap: Page => CheckRoute => Int => Option[Any] => Int => Call =
    _ => _ => id => _ => _ => routes.AffectedCheckYourAnswersController.onPageLoad(id)

  private[navigation] def jumpOrCheckYourAnswers(id: Int, jumpTo: Call, checkRoute: CheckRoute): Call = {
    checkRoute match {
      case DefaultRouting(CheckMode)         => routes.AffectedCheckYourAnswersController.onPageLoad(id)
      case _                                 => jumpTo
    }
  }
}