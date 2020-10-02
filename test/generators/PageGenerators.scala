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

package generators

import org.scalacheck.Arbitrary
import pages._

trait PageGenerators {

  implicit lazy val arbitraryHallmarkEPage: Arbitrary[HallmarkEPage.type] =
    Arbitrary(HallmarkEPage)

  implicit lazy val arbitraryHallmarkC1Page: Arbitrary[HallmarkC1Page.type] =
    Arbitrary(HallmarkC1Page)

  implicit lazy val arbitraryHallmarkCPage: Arbitrary[HallmarkCPage.type] =
    Arbitrary(HallmarkCPage)

  implicit lazy val arbitraryHallmarkD1OtherPage: Arbitrary[HallmarkD1OtherPage.type] =
    Arbitrary(HallmarkD1OtherPage)

  implicit lazy val arbitraryHallmarkD1Page: Arbitrary[HallmarkD1Page.type] =
    Arbitrary(HallmarkD1Page)

  implicit lazy val arbitraryHallmarkDPage: Arbitrary[HallmarkDPage.type] =
    Arbitrary(HallmarkDPage)

  implicit lazy val arbitraryHallmarkBPage: Arbitrary[HallmarkBPage.type] =
    Arbitrary(HallmarkBPage)

  implicit lazy val arbitraryMeetMainBenefitTestPage: Arbitrary[MainBenefitTestPage.type] =
    Arbitrary(MainBenefitTestPage)

  implicit lazy val arbitraryHallmarkAPage: Arbitrary[HallmarkAPage.type] =
    Arbitrary(HallmarkAPage)

  implicit lazy val arbitraryHallmarkCategoriesPage: Arbitrary[HallmarkCategoriesPage.type] =
    Arbitrary(HallmarkCategoriesPage)
}
