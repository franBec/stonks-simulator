Feature: Portfolio

  Background:
    * url baseUrl

  Scenario: Get initial portfolio
    Given path 'api/portfolio'
    When method get
    Then status 200
    And match response.status == 200
    And match response.data.cashBalance == 10000.0
    And match response.data.positions == '#[]'
    And match response.data.unrealizedPnl == 0.0

  Scenario: Get portfolio after a trade
    * def buyResult = call read('classpath:features/helpers/buy-gmee.feature')
    Given path 'api/portfolio'
    When method get
    Then status 200
    And match response.status == 200
    And match response.data.cashBalance == '#number'
    And match response.data.positions == '#[]'
    * def gmeePos = karate.jsonPath(response, "$.data.positions[?(@.symbol=='GMEE')]")
    * assert gmeePos.length > 0
    # costBasis was always 0 before the fix; now properly populated from DB
    * assert gmeePos[0].costBasis > 0
    * assert gmeePos[0].marketValue > 0
    * assert gmeePos[0].currentPrice > 0
    * assert gmeePos[0].quantity == 5
    And match response.data.unrealizedPnl == '#number'
