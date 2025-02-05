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

package controllers.mixins

import models.{Country, LoopDetails, UserAnswers}
import pages.QuestionPage
import play.api.libs.json.{JsObject, Json}

trait CountrySupport {

  def getCountry[A](userAnswers: UserAnswers, id:Int, page: QuestionPage[IndexedSeq[LoopDetails]], index: Int): Option[Country] = for {
    loopPage <- userAnswers.get(page, id)
    loopDetails <- loopPage.lift(index)
    country <- loopDetails.whichCountry
  } yield country

  def countryJsonList(value: Map[String, String], countries: Seq[Country]): Seq[JsObject] = {
    def containsCountry(country: Country): Boolean =
      value.get("country") match {
        case Some(countryCode) => countryCode == country.code
        case _ => false
      }

    val countryJsonList = countries.map {
      country =>
        Json.obj("text" -> country.description, "value" -> country.code, "selected" -> containsCountry(country))
    }

    Json.obj("value" -> "", "text" -> "") +: countryJsonList
  }

}
