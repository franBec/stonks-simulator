# Full end-to-end smoke flow that replays the original 14-step manual curl test.
# Each step delegates to a modular helper or dedicated feature via `call read`,
# keeping the flow readable while reusing isolated building blocks.

Feature: Full smoke flow — 14 steps

  Background:
    * url baseUrl

  Scenario: The whole stonks app feels alive
    # ------------------------------------------------------------
    # 1. Market catalog & live prices
    # ------------------------------------------------------------
    Given path 'api/market/stocks'
    When method get
    Then status 200
    And match response.status == 200
    And match response.data == '#[]'
    And match response.data[*].symbol contains [ 'TEND', 'YOLO', 'BUGS', 'JAVA', 'MEME', 'PAPR', 'GMEE', 'COBL', 'DOGE', 'FOMO' ]
    * def gmeePrice = karate.jsonPath(response, "$.data[?(@.symbol=='GMEE')].price")[0]
    * assert gmeePrice > 0

    # ------------------------------------------------------------
    # 2. Current portfolio (empty at first)
    # ------------------------------------------------------------
    Given path 'api/portfolio'
    When method get
    Then status 200
    And match response.status == 200
    And match response.data.cashBalance == 10000.0
    And match response.data.positions == '#[]'
    And match response.data.unrealizedPnl == 0.0

    # ------------------------------------------------------------
    # 3. Current chaos level
    # ------------------------------------------------------------
    Given path 'api/chaos/level'
    When method get
    Then status 200
    And match response.status == 200
    And match response.data == 'PAPER_HANDS'

    # ------------------------------------------------------------
    # 4. Trade history (empty at first)
    # ------------------------------------------------------------
    Given path 'api/trades/history'
    And param page = 0
    And param size = 5
    When method get
    Then status 200
    And match response.status == 200
    And match response.data.content == '#[]'
    And match response.data.totalElements == 0

    # ------------------------------------------------------------
    # 5. Validate a BUY of 5 GMEE at a chosen price
    # ------------------------------------------------------------
    Given path 'api/trades/validate'
    And request
      """
      {
        "action": "BUY",
        "symbol": "GMEE",
        "quantity": 5,
        "price": 150.00,
        "cashBalance": 10000.00
      }
      """
    When method post
    Then status 200
    And match response.status == 200
    And match response.data.status == 'ACCEPTED'
    And match response.data.message == 'TRADE VALIDATED - PROCEED TO EXECUTION'
    And match response.data.totalCost == 750.0
    And match response.data.remainingCash == 9250.0
    And match response.data.errorCode == null

    # ------------------------------------------------------------
    # 6. Execute the same BUY (live market price)
    # ------------------------------------------------------------
    * def buyResult = call read('classpath:features/helpers/buy-gmee.feature')
    * assert buyResult.tradeResult.newCashBalance < 10000.0

    # ------------------------------------------------------------
    # 7. Re-check portfolio (now has GMEE position)
    # ------------------------------------------------------------
    Given path 'api/portfolio'
    When method get
    Then status 200
    And match response.status == 200
    And match response.data.cashBalance == '#number'
    * def gmeePos = karate.jsonPath(response, "$.data.positions[?(@.symbol=='GMEE')]")
    * assert gmeePos.length > 0
    And match response.data.unrealizedPnl == '#number'

    # ------------------------------------------------------------
    # 8. Re-check history (now has one entry)
    # ------------------------------------------------------------
    Given path 'api/trades/history'
    And param page = 0
    And param size = 5
    When method get
    Then status 200
    And match response.status == 200
    And match response.data.totalElements > 0
    And match response.data.content[0].action == 'BUY'
    And match response.data.content[0].symbol == 'GMEE'

    # ------------------------------------------------------------
    # 9. Raise chaos level to MODERATE
    # ------------------------------------------------------------
    * call read('classpath:features/helpers/set-chaos-moderate.feature')

    # ------------------------------------------------------------
    # 10. Trigger a manual chaos event
    # ------------------------------------------------------------
    * def eventResult = call read('classpath:features/helpers/trigger-hype-wave.feature')

    # ------------------------------------------------------------
    # 11. List active chaos events
    # ------------------------------------------------------------
    Given path 'api/chaos/events'
    When method get
    Then status 200
    And match response.status == 200
    And match response.data == '#[]'
    And match response.data[*].eventId == '#[] #uuid'
    And match response.data[*].type == '#[] #notnull'

    # ------------------------------------------------------------
    # 12. SSE stream — verify endpoint is reachable
    # ------------------------------------------------------------
    Given path 'api/stream'
    And header Accept = 'text/event-stream'
    When method get
    Then status 200
    And match header Content-Type contains 'text/event-stream'

    # ------------------------------------------------------------
    # 13. Paper tape retro formatting
    # ------------------------------------------------------------
    Given path 'api/trades/paper-tape'
    And param page = 0
    And param size = 5
    When method get
    Then status 200
    And match response.status == 200
    And match response.data.totalElements > 0
    And match response.data.content[0].formattedLine == '#notnull'
    And match response.data.content[0].sequenceNumber == '#number'

    # ------------------------------------------------------------
    # 14. Sell back the GMEE position
    # ------------------------------------------------------------
    * def sellResult = call read('classpath:features/helpers/sell-gmee.feature')
    * assert sellResult.tradeResult.newQuantity == 0
