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

package connectors

import config.FrontendAppConfig
import models.UserAnswers
import org.slf4j.LoggerFactory
import play.api.http.Status.OK
import models.subscription._
import play.api.libs.json.{JsError, JsSuccess}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SubscriptionConnector @Inject()(val config: FrontendAppConfig, val http: HttpClient) {

  private val logger = LoggerFactory.getLogger(getClass)

  def displaySubscriptionDetails(enrolmentID: String)
                                (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[DisplaySubscriptionForDACResponse]] = {

    val submissionUrl = s"${config.crossBorderArrangementsUrl}/disclose-cross-border-arrangements/subscription/retrieve-subscription"

    http.POST[DisplaySubscriptionForDACRequest, HttpResponse](
      submissionUrl,
      DisplaySubscriptionForDACRequest(DisplaySubscriptionDetails.createRequest(enrolmentID))
    ).map {
      response =>
        response.status match {
          case OK => response.json.validate[DisplaySubscriptionForDACResponse] match {
            case JsSuccess(response, _) => Some(response)
            case JsError(errors) =>
              logger.warn("Validation of display subscription payload failed", errors)
              None
          }
          case errorStatus: Int =>
            logger.warn(s"Status $errorStatus has been thrown when display subscription was called")
            None
        }
    }
  }
}
