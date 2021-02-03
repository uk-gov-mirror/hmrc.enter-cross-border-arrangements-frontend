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

package pages.enterprises

import models.enterprises.YouHaveNotAddedAnyAssociatedEnterprises
import models.{Address, Country, LoopDetails, Name, SelectType, TaxReferenceNumbers, UnsubmittedDisclosure, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours
import pages.disclosure.DisclosureDetailsPage
import pages.individual._
import pages.organisation._
import pages.unsubmitted.UnsubmittedDisclosurePage

import java.time.LocalDate

class YouHaveNotAddedAnyAssociatedEnterprisesPageSpec extends PageBehaviours {

  "YouHaveNotAddedAnyAssociatedEnterprisesPage" - {

    beRetrievable[YouHaveNotAddedAnyAssociatedEnterprises](YouHaveNotAddedAnyAssociatedEnterprisesPage)

    beSettable[YouHaveNotAddedAnyAssociatedEnterprises](YouHaveNotAddedAnyAssociatedEnterprisesPage)

    beRemovable[YouHaveNotAddedAnyAssociatedEnterprises](YouHaveNotAddedAnyAssociatedEnterprisesPage)

    "must remove previous answers and individual journey pages if Individual was selected" in {

      forAll(arbitrary[Country], arbitrary[Address], arbitrary[IndexedSeq[LoopDetails]], arbitrary[TaxReferenceNumbers]) {
        (country, address, loopDetails, taxRefNumbers) =>
          val result = UserAnswers("id")
            .setBase(UnsubmittedDisclosurePage, Seq(UnsubmittedDisclosure("1", "My First"))).success.value
            .set(SelectAnyTaxpayersThisEnterpriseIsAssociatedWithPage, 0, List("Taxpayer"))
            .success.value
            .set(IndividualNamePage, 0, Name("First", "Last"))
            .success.value
            .set(IndividualDateOfBirthPage, 0, LocalDate.now())
            .success.value
            .set(IsIndividualPlaceOfBirthKnownPage, 0, true)
            .success.value
            .set(IsIndividualDateOfBirthKnownPage, 0, true)
            .success.value
            .set(IndividualPlaceOfBirthPage, 0, "Place of birth")
            .success.value
            .set(IsIndividualAddressKnownPage, 0, true)
            .success.value
            .set(IsIndividualAddressUkPage, 0, true)
            .success.value
            .set(IndividualUkPostcodePage, 0, "ZZ1 1ZZ")
            .success.value
            .set(IndividualSelectAddressPage, 0, "Some address")
            .success.value
            .set(IndividualAddressPage, 0, address)
            .success.value
            .set(EmailAddressQuestionForIndividualPage, 0, true)
            .success.value
            .set(EmailAddressForIndividualPage, 0, "email@email.com")
            .success.value
            .set(WhichCountryTaxForIndividualPage, 0, country)
            .success.value
            .set(DoYouKnowAnyTINForUKIndividualPage, 0, true)
            .success.value
            .set(WhatAreTheTaxNumbersForUKIndividualPage, 0, taxRefNumbers)
            .success.value
            .set(IsIndividualResidentForTaxOtherCountriesPage, 0, true)
            .success.value
            .set(DoYouKnowTINForNonUKIndividualPage, 0, true)
            .success.value
            .set(WhatAreTheTaxNumbersForNonUKIndividualPage, 0, taxRefNumbers)
            .success.value
            .set(IndividualLoopPage, 0, loopDetails)
            .success.value
            .set(IsAssociatedEnterpriseAffectedPage, 0, true)
            .success.value
            .set(YouHaveNotAddedAnyAssociatedEnterprisesPage, 0, YouHaveNotAddedAnyAssociatedEnterprises.YesAddNow)
            .success.value
            .set(AssociatedEnterpriseTypePage, 0, SelectType.Individual)
            .success.value

          result.get(IndividualNamePage, 0) mustBe None
          result.get(IndividualDateOfBirthPage, 0) mustBe None
          result.get(IsIndividualPlaceOfBirthKnownPage, 0) mustBe None
          result.get(IsIndividualDateOfBirthKnownPage, 0) mustBe None
          result.get(IndividualPlaceOfBirthPage, 0) mustBe None
          result.get(IsIndividualAddressKnownPage, 0) mustBe None
          result.get(IsIndividualAddressUkPage, 0) mustBe None
          result.get(IndividualUkPostcodePage, 0) mustBe None
          result.get(IndividualSelectAddressPage, 0) mustBe None
          result.get(IndividualAddressPage, 0) mustBe None
          result.get(EmailAddressQuestionForIndividualPage, 0) mustBe None
          result.get(EmailAddressForIndividualPage, 0) mustBe None
          result.get(WhichCountryTaxForIndividualPage, 0) mustBe None
          result.get(DoYouKnowAnyTINForUKIndividualPage, 0) mustBe None
          result.get(WhatAreTheTaxNumbersForUKIndividualPage, 0) mustBe None
          result.get(IsIndividualResidentForTaxOtherCountriesPage, 0) mustBe None
          result.get(DoYouKnowTINForNonUKIndividualPage, 0) mustBe None
          result.get(WhatAreTheTaxNumbersForNonUKIndividualPage, 0) mustBe None
          result.get(IndividualLoopPage, 0) mustBe None
          result.get(IsAssociatedEnterpriseAffectedPage, 0) mustBe None
      }
    }

    "must remove previous answers and organisation journey pages if Organisation was selected" in {

      forAll(arbitrary[Country], arbitrary[Address], arbitrary[IndexedSeq[LoopDetails]], arbitrary[TaxReferenceNumbers]) {
        (country, address, loopDetails, taxRefNumbers) =>
          val result = UserAnswers("id")
            .setBase(UnsubmittedDisclosurePage, Seq(UnsubmittedDisclosure("1", "My First"))).success.value
            .set(SelectAnyTaxpayersThisEnterpriseIsAssociatedWithPage, 0, List("Taxpayer"))
            .success.value
            .set(OrganisationNamePage, 0, "Organisation name")
            .success.value
            .set(IsOrganisationAddressKnownPage, 0, true)
            .success.value
            .set(IsOrganisationAddressUkPage, 0, true)
            .success.value
            .set(SelectAddressPage, 0, "Some address")
            .success.value
            .set(PostcodePage, 0, "ZZ1 1ZZ")
            .success.value
            .set(OrganisationAddressPage, 0, address)
            .success.value
            .set(EmailAddressQuestionForOrganisationPage, 0, true)
            .success.value
            .set(EmailAddressForOrganisationPage, 0, "email@email.com")
            .success.value
            .set(WhichCountryTaxForOrganisationPage, 0, country)
            .success.value
            .set(DoYouKnowAnyTINForUKOrganisationPage, 0, true)
            .success.value
            .set(WhatAreTheTaxNumbersForUKOrganisationPage, 0, taxRefNumbers)
            .success.value
            .set(IsOrganisationResidentForTaxOtherCountriesPage, 0, true)
            .success.value
            .set(DoYouKnowTINForNonUKOrganisationPage, 0, true)
            .success.value
            .set(WhatAreTheTaxNumbersForNonUKOrganisationPage, 0, taxRefNumbers)
            .success.value
            .set(OrganisationLoopPage, 0, loopDetails)
            .success.value
            .set(IsAssociatedEnterpriseAffectedPage, 0, false)
            .success.value
            .set(YouHaveNotAddedAnyAssociatedEnterprisesPage, 0, YouHaveNotAddedAnyAssociatedEnterprises.YesAddNow)
            .success.value
            .set(AssociatedEnterpriseTypePage, 0, SelectType.Organisation)
            .success.value

          result.get(OrganisationNamePage, 0) mustBe None
          result.get(IsOrganisationAddressKnownPage, 0) mustBe None
          result.get(IsOrganisationAddressUkPage, 0) mustBe None
          result.get(SelectAddressPage, 0) mustBe None
          result.get(PostcodePage, 0) mustBe None
          result.get(OrganisationAddressPage, 0) mustBe None
          result.get(EmailAddressQuestionForOrganisationPage, 0) mustBe None
          result.get(EmailAddressForOrganisationPage, 0) mustBe None
          result.get(WhichCountryTaxForOrganisationPage, 0) mustBe None
          result.get(DoYouKnowAnyTINForUKOrganisationPage, 0) mustBe None
          result.get(WhatAreTheTaxNumbersForUKOrganisationPage, 0) mustBe None
          result.get(IsOrganisationResidentForTaxOtherCountriesPage, 0) mustBe None
          result.get(DoYouKnowTINForNonUKOrganisationPage, 0) mustBe None
          result.get(WhatAreTheTaxNumbersForNonUKOrganisationPage, 0) mustBe None
          result.get(OrganisationLoopPage, 0) mustBe None
          result.get(IsAssociatedEnterpriseAffectedPage, 0) mustBe None
      }
    }
  }
}
