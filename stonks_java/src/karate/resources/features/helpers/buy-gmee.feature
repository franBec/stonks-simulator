# Helper: Execute a BUY trade for 5 shares of GMEE.
# Returns the trade execution result so callers can chain assertions.

Feature: Helper — Execute BUY 5 GMEE

  Background:
    * url baseUrl

  Scenario: Execute BUY
    Given path 'api/trades'
    And request
      """
      {
        "action": "BUY",
        "symbol": "GMEE",
        "quantity": 5
      }
      """
    When method post
    Then status 200
    And match response.status == 200
    And match response.data.status == 'ACCEPTED'
    And match response.data.message == '#regex ^TRADE EXECUTED - BUY.*'
    And match response.data.newCashBalance == '#number'
    And match response.data.newQuantity == 5
    And match response.data.totalCost == '#number'
    And match response.data.errorCode == null
    * assert response.data.newCashBalance < 10000.0
    * assert response.data.totalCost > 0
    # Expose trade result to caller
    * def tradeResult = response.data
