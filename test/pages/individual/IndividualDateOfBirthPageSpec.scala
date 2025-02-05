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

package pages.individual

import helpers.data.ValidUserAnswersForSubmission.validIndividual
import org.scalacheck.Arbitrary
import pages.behaviours.PageBehaviours

import java.time.LocalDate

class IndividualDateOfBirthPageSpec extends PageBehaviours {

  "IndividualDateOfBirthPage" - {

    implicit lazy val arbitraryLocalDate: Arbitrary[LocalDate] = Arbitrary {
      datesBetween(LocalDate.of(1900, 1, 1), LocalDate.of(2100, 1, 1))
    }

    beRetrievable[LocalDate](IndividualDateOfBirthPage)

    beSettable[LocalDate](IndividualDateOfBirthPage)

    beRemovable[LocalDate](IndividualDateOfBirthPage)
  }

  "can restore from model " - {

    "- when dob exists " in {

      val dob = LocalDate.of(1900, 1, 2)
      IndividualDateOfBirthPage.getFromModel(validIndividual.copy(birthDate = dob)) mustBe(Some(dob))
    }

    "- when dob is empty (before 1900-01-02) " in {

      IndividualDateOfBirthPage.getFromModel(validIndividual.copy(birthDate = LocalDate.of(1900, 1, 1))) mustBe(None)
    }
  }
}
