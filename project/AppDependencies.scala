import sbt._

object AppDependencies {
  import play.core.PlayVersion

  val compile = Seq(
    play.sbt.PlayImport.ws,
    "org.reactivemongo" %% "play2-reactivemongo"              % "0.18.6-play27",
    "uk.gov.hmrc"       %% "logback-json-logger"              % "4.8.0",
    "uk.gov.hmrc"       %% "play-health"                      % "3.16.0-play-27",
    "uk.gov.hmrc"       %% "play-conditional-form-mapping"    % "1.5.0-play-27",
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-27"       % "3.2.0",
    "uk.gov.hmrc"       %% "play-nunjucks"                    % "0.23.0-play-26",
    "uk.gov.hmrc"       %% "play-nunjucks-viewmodel"          % "0.9.0-play-26",
    "uk.gov.hmrc"       %% "play-frontend-hmrc"               % "0.44.0-play-27",
    "uk.gov.hmrc"       %% "emailaddress"                     % "3.5.0",
    "org.webjars.npm"   %  "govuk-frontend"                   % "3.9.1",
    "org.webjars.npm"   %   "hmrc-frontend"                   % "1.19.1"
  )

  val test = Seq(
    "org.scalatest"               %% "scalatest"             % "3.0.8",
    "org.scalatestplus.play"      %% "scalatestplus-play"    % "4.0.3",
    "org.pegdown"                 %  "pegdown"               % "1.6.0",
    "org.jsoup"                   %  "jsoup"                 % "1.10.3",
    "com.typesafe.play"           %% "play-test"             % PlayVersion.current,
    "org.mockito"                 %  "mockito-all"           % "1.10.19",
    "org.scalacheck"              %% "scalacheck"            % "1.14.0",
    "com.github.tomakehurst"      %  "wiremock-standalone"   % "2.25.0",
    "wolfendale"                  %% "scalacheck-gen-regexp" % "0.1.2"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test
}
