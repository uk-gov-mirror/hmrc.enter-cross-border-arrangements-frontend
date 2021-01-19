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

package helpers.xml

import models.IsExemptionKnown.{No, Unknown, Yes}
import models.individual.Individual
import models.intermediaries.WhatTypeofIntermediary.IDoNotKnow
import models.intermediaries.{ExemptCountries, Intermediary}
import models.organisation.Organisation
import models.reporter.RoleInArrangement
import models.{ReporterOrganisationOrIndividual, UserAnswers}
import pages.intermediaries.IntermediaryLoopPage
import pages.reporter.{ReporterOrganisationOrIndividualPage, RoleInArrangementPage}

import scala.util.Try
import scala.xml.{Elem, Node, NodeSeq}

object IntermediariesXMLSection extends XMLBuilder {

  private[xml] def buildReporterAsIntermediary(userAnswers: UserAnswers): NodeSeq = {
    userAnswers.get(RoleInArrangementPage) match {
      case Some(RoleInArrangement.Intermediary) =>

        userAnswers.get(ReporterOrganisationOrIndividualPage) match {

          case Some(ReporterOrganisationOrIndividual.Organisation) =>
            val organisationDetailsForReporter = Organisation.buildOrganisationDetailsForReporter(userAnswers)

            <Intermediary>
              {OrganisationXMLSection.buildIDForOrganisation(organisationDetailsForReporter)}
              {DisclosingXMLSection.buildReporterCapacity(userAnswers)}
              {DisclosingXMLSection.buildReporterExemptions(userAnswers)}
            </Intermediary>

          case _ =>
            val individualDetailsForReporter = Individual.buildIndividualDetailsForReporter(userAnswers)

            <Intermediary>
              {IndividualXMLSection.buildIDForIndividual(individualDetailsForReporter)}
              {DisclosingXMLSection.buildReporterCapacity(userAnswers)}
              {DisclosingXMLSection.buildReporterExemptions(userAnswers)}
            </Intermediary>
        }
      case _ => NodeSeq.Empty
    }
  }

  private[xml] def getIntermediaryCapacity(intermediary: Intermediary): NodeSeq = {
    if (intermediary.whatTypeofIntermediary.equals(IDoNotKnow)) {
      NodeSeq.Empty
    } else {
      <Capacity>{intermediary.whatTypeofIntermediary.toString}</Capacity>
    }
  }

  private[xml] def buildNationalExemption(intermediary: Intermediary): NodeSeq = {

    val countryExemptions = intermediary.isExemptionCountryKnown.fold(NodeSeq.Empty) {
      case false => NodeSeq.Empty
      case true =>
        val getCountries = intermediary.exemptCountries.fold(NodeSeq.Empty)(setOfCountries =>
          setOfCountries.toList.map((country: ExemptCountries) =>
              <CountryExemption>{country}</CountryExemption>))

        <CountryExemptions>
          {getCountries}
        </CountryExemptions>
    }

    val nationalExemption = intermediary.isExemptionKnown match {
      case Unknown => NodeSeq.Empty
      case No =>
        <NationalExemption>
          <Exemption>false</Exemption>
        </NationalExemption>
      case Yes =>
        <NationalExemption>
          <Exemption>true</Exemption>
          {countryExemptions}
        </NationalExemption>
    }
    nationalExemption
  }

  private[xml] def getIntermediaries(userAnswers: UserAnswers): Seq[Node] = {

    userAnswers.get(IntermediaryLoopPage) match {
      case Some(intermediariesList) =>
        intermediariesList.map {
          intermediary =>
            if (intermediary.individual.isDefined) {
              <Intermediary>
                {IndividualXMLSection.buildIDForIndividual(intermediary.individual.get) ++
                getIntermediaryCapacity(intermediary) ++
                buildNationalExemption(intermediary)}
              </Intermediary>
            } else {
              <Intermediary>
                {OrganisationXMLSection.buildIDForOrganisation(intermediary.organisation.get) ++
                getIntermediaryCapacity(intermediary) ++
                buildNationalExemption(intermediary)}
              </Intermediary>
            }
        }
      case _ => NodeSeq.Empty
    }
  }

  override def toXml(userAnswers: UserAnswers): Either[Throwable, Elem] = {
    Try {
      <Intermediaries>
        {buildReporterAsIntermediary(userAnswers) ++ getIntermediaries(userAnswers)}
      </Intermediaries>
    }.toEither
  }
}