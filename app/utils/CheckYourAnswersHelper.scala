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

package utils

import java.time.format.DateTimeFormatter

import controllers.routes
import models.HallmarkA._
import models.HallmarkC.C1
import models.HallmarkC1._
import models.HallmarkCategories.{CategoryA, CategoryB}
import models.HallmarkD.D1
import models.HallmarkD1.D1other
import models.{CheckMode, HallmarkA, HallmarkB, UserAnswers}
import pages._
import play.api.i18n.Messages
import uk.gov.hmrc.viewmodels.SummaryList._
import uk.gov.hmrc.viewmodels._

class CheckYourAnswersHelper(userAnswers: UserAnswers)(implicit messages: Messages) {

  val d1OtherVisibleCharacters = 100
  val ellipsis = " ..."

  def hallmarkD1Other: Option[Row] = userAnswers.get(HallmarkD1OtherPage) flatMap {
    answer => userAnswers.get(HallmarkD1Page) match {
      case Some(hallmarkSet) if hallmarkSet.contains(D1other) =>
        Some(Row(
          key = Key(msg"hallmarkD1Other.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
          value = Value(lit"${
            if (answer.length > 100) answer.take(d1OtherVisibleCharacters) + ellipsis else answer
          }"),
          actions = List(
            Action(
              content = msg"site.edit",
              href = routes.HallmarkD1OtherController.onPageLoad(CheckMode).url,
              visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"hallmarkD1Other.checkYourAnswersLabel"))
            )
          )
        ))
      case _ => None
    }
  }

  def mainBenefitPredicate[A](set: Option[Set[A]], elem: A): Boolean = {
    set match {
      case Some(hm) => hm.contains(elem)
      case None => false
    }
  }

  def mainBenefitTest: Option[Row] = if(
    mainBenefitPredicate(userAnswers.get(HallmarkC1Page), C1bi) ||
    mainBenefitPredicate(userAnswers.get(HallmarkC1Page), C1c) ||
    mainBenefitPredicate(userAnswers.get(HallmarkC1Page), C1d) ||
    mainBenefitPredicate(userAnswers.get(HallmarkCategoriesPage), CategoryA) ||
    mainBenefitPredicate(userAnswers.get(HallmarkCategoriesPage), CategoryB)) {

    userAnswers.get(MainBenefitTestPage) map {
      answer =>
        Row(
          key     = Key(msg"mainBenefitTest.checkYourAnswersLabel"),
          value   = Value(yesOrNo(answer)),
          actions = List(
            Action(
              content            = msg"site.edit",
              href               = routes.MainBenefitTestController.onPageLoad(CheckMode).url,
              visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"mainBenefitTest.checkYourAnswersLabel"))
            )
          )
        )
    }
  } else{
   None
}

  def hallmarkCategories: Option[Row] = userAnswers.get(HallmarkCategoriesPage) map {
    answer =>
      Row(
        key     = Key(msg"hallmarkCategories.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-third")),
        value   = Value(Html(answer.map(a => msg"hallmarkCategories.$a".resolve).mkString(",<br>"))),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = routes.HallmarkCategoriesController.onPageLoad(CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"hallmarkCategories.checkYourAnswersLabel"))
          )
        )
      )
  }

  def buildHallmarksRow(ua: UserAnswers): Row = {

    val hallmarkCPage = ua.get(HallmarkCPage)  match {
      case Some(set) if set.contains(C1) && set.size == 1 => None
      case Some(set) if set.contains(C1) => Some(set.filter(_ != C1))
      case hallmarkSet => hallmarkSet
    }

    val hallmarkDPage = ua.get(HallmarkDPage)  match {
      case Some(set) if set.contains(D1) && set.size == 1 => None
      case Some(set) if set.contains(D1) => Some(set.filter(_ != D1))
      case hallmarkSet => hallmarkSet
    }

    val hallmarkPages = Seq(
      ua.get(HallmarkAPage),
      ua.get(HallmarkBPage),
      ua.get(HallmarkC1Page),
      ua.get(HallmarkD1Page),
      hallmarkCPage,
      hallmarkDPage,
      ua.get(HallmarkEPage)
    )

    val selectedHallmarkParts = hallmarkPages.collect{ case Some(value) => value }

    val hallmarksList = for {
      selectedHallmark <- selectedHallmarkParts
    } yield {
      selectedHallmark.map(_.toString).toList.sorted.map(hallmark => msg"$hallmark".resolve).mkString(", ")
    }

    Row(
      key     = Key(msg"checkYourAnswers.selectedHallmarks.label"),
      value   = Value(msg"${hallmarksList.mkString(", ")}"),
      actions = List(
        Action(
          content            = msg"site.edit",
          href               = routes.HallmarkCategoriesController.onPageLoad(CheckMode).url,
          visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"checkYourAnswers.selectedHallmarks.label"))
        )
      )
    )

  }

  private def yesOrNo(answer: Boolean): Content =
    if (answer) {
      msg"site.yes"
    } else {
      msg"site.no"
    }
}

object CheckYourAnswersHelper {
  private val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
}
