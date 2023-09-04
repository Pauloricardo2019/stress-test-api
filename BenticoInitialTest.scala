package Bentico

import io.gatling.core.Predef._
import io.gatling.core.structure.{ChainBuilder, ScenarioBuilder}
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder
import scala.concurrent.duration._

import scala.util.Random
import scala.concurrent.duration.DurationInt

class LoginBenticoSimulation extends Simulation {

  var myToken: String = ""
  
  //1. HTTP Configuration
  val httpProtocol: HttpProtocolBuilder = http
    .baseUrl("http://localhost:8181")
    .header("Accept", "application/json")

  
  def login: ChainBuilder = {
    exec(
      http("Login")
        .post("/bentico/v1/login")
        .body(StringBody("""{"cpf":"10325200947","password":"Ex@mple123"}""")).asJson
        .check(status.is(201))
        .check(jsonPath("$.token").saveAs("token"))
    )
  }

  def getUser: ChainBuilder = {      
    exec(
      http("Get User")
        .get("/bentico/v1/user/1")
        .header("Authorization", session => "Bearer " + session("token").as[String])
        .check(status.is(200))
    )
  }

  val loginScn: ScenarioBuilder = scenario("Login Scenario")
  .exec(login)
  .pause(3)
  .exec(getUser)
  .pause(3)      


  setUp(

    loginScn.inject(
      atOnceUsers(1),
      rampUsers(100).during(10.seconds), // Use rampUsers para definir a taxa de rampa
      rampUsers(500).during(15.seconds),
      rampUsers(1000).during(1.minute) // Use rampUsers para definir a tax
    ),      
      
      )
      .protocols(httpProtocol)
  
}