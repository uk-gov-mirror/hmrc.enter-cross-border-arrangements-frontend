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

package navigation

import controllers.routes
import javax.inject.{Inject, Singleton}
import models.HallmarkCategories.{CategoryA, CategoryB, CategoryD, CategoryE}
import models.HallmarkD.D1
import models.HallmarkD1.D1other
import models._
import pages._
import play.api.mvc.Call

@Singleton
class Navigator @Inject()() {

  private val normalRoutes: Page => UserAnswers => Option[Call] = {
    case HallmarkCategoriesPage => hallmarkCategoryRoutes(NormalMode)
    case HallmarkAPage => hallmarkARoutes(NormalMode)
    case HallmarkBPage => hallmarkBRoutes(NormalMode)
    case MainBenefitTestPage => mainBenefitTestRoutes(NormalMode)
    case HallmarkDPage => hallmarkDRoutes(NormalMode)
    case HallmarkD1Page => hallmarkD1Routes(NormalMode)
    case HallmarkD1OtherPage => hallmarkD1OtherRoutes(NormalMode)
    case HallmarkEPage => _ => Some(routes.CheckYourAnswersController.onPageLoad())
    case _ => _ => Some(routes.IndexController.onPageLoad())
  }

  private val checkRouteMap: Page => UserAnswers => Option[Call] = {
    case HallmarkCategoriesPage => hallmarkCategoryRoutes(CheckMode)
    case HallmarkAPage => hallmarkARoutes(CheckMode)
    case HallmarkBPage => hallmarkBRoutes(CheckMode)
    case MainBenefitTestPage => mainBenefitTestRoutes(CheckMode)
    case HallmarkDPage => hallmarkDRoutes(CheckMode)
    case HallmarkD1Page => hallmarkD1Routes(CheckMode)
    case HallmarkD1OtherPage => hallmarkD1OtherRoutes(CheckMode)
    case HallmarkEPage => _ => Some(routes.CheckYourAnswersController.onPageLoad())
    case _ => _ => Some(routes.CheckYourAnswersController.onPageLoad())
  }

  private def hallmarkCategoryRoutes(mode: Mode)(ua: UserAnswers): Option[Call] =
    ua.get(HallmarkCategoriesPage) map {
      case set: Set[HallmarkCategories] if set.head == CategoryA =>
        routes.HallmarkAController.onPageLoad(mode)
      case set: Set[HallmarkCategories] if set.head == CategoryB =>
        routes.HallmarkBController.onPageLoad(mode)
      case set: Set[HallmarkCategories] if set.head == CategoryD =>
        routes.HallmarkDController.onPageLoad(mode)
      case set: Set[HallmarkCategories] if set.head == CategoryE =>
        routes.HallmarkEController.onPageLoad(mode)
    }

  private def hallmarkARoutes(mode: Mode)(ua: UserAnswers): Option[Call] =
    ua.get(HallmarkCategoriesPage) map {
      case set: Set[HallmarkCategories] if set.contains(CategoryB) =>
        routes.HallmarkBController.onPageLoad(mode)
      case _ =>
        routes.MainBenefitTestController.onPageLoad(mode)
    }

  private def hallmarkBRoutes(mode: Mode)(ua: UserAnswers): Option[Call] =
    ua.get(HallmarkCategoriesPage) map {
      case set: Set[HallmarkCategories] if set.contains(CategoryA) => //TODO Route to Category C page when ready. Add UT
        routes.MainBenefitTestController.onPageLoad(mode)
      case _ =>
        routes.MainBenefitTestController.onPageLoad(mode)
    }

  private def mainBenefitTestRoutes(mode: Mode)(ua: UserAnswers): Option[Call] =
    ua.get(MainBenefitTestPage) map {
      case true =>
        ua.get(HallmarkCategoriesPage) match {
          case Some(set) if set.contains(CategoryD) => routes.HallmarkDController.onPageLoad(mode)
          case Some(set) if set.contains(CategoryE) => routes.HallmarkEController.onPageLoad(mode)
          case _ => routes.CheckYourAnswersController.onPageLoad()
        }
      case false => routes.MainBenefitProblemController.onPageLoad()
    }

  private def hallmarkDRoutes(mode: Mode)(ua: UserAnswers): Option[Call] =
    ua.get(HallmarkDPage) flatMap  {
      case set: Set[HallmarkD] if set.contains(D1) => Some(routes.HallmarkD1Controller.onPageLoad(mode))
       case  _ => ua.get(HallmarkCategoriesPage).map {
         case set: Set[HallmarkCategories] if set.contains(CategoryE) =>
           routes.HallmarkEController.onPageLoad(mode)
         case _ => routes.CheckYourAnswersController.onPageLoad()
       }
    }

   private def hallmarkD1Routes(mode: Mode)(ua: UserAnswers): Option[Call] =
    ua.get(HallmarkD1Page) flatMap  {
      case set: Set[HallmarkD1] if set.contains(D1other) => Some(routes.HallmarkD1OtherController.onPageLoad(mode))
      case  _ => ua.get(HallmarkCategoriesPage).map {
        case set: Set[HallmarkCategories] if set.contains(CategoryE) =>
          routes.HallmarkEController.onPageLoad(mode)
        case _ => routes.CheckYourAnswersController.onPageLoad()
      }
    }

   private def hallmarkD1OtherRoutes(mode: Mode)(ua: UserAnswers): Option[Call] =
     ua.get(HallmarkCategoriesPage) map {
       case set: Set[HallmarkCategories] if set.contains(CategoryE) =>
         routes.HallmarkEController.onPageLoad(mode)
       case _ => routes.CheckYourAnswersController.onPageLoad()
     }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers) match {
        case Some(call) => call
        case None => routes.SessionExpiredController.onPageLoad()
      }
    case CheckMode =>
      checkRouteMap(page)(userAnswers) match {
        case Some(call) => call
        case None => routes.SessionExpiredController.onPageLoad()

      }
  }
}
