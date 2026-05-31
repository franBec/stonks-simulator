# Full end-to-end smoke flow that replays the original manual curl test.
# Each step delegates to a modular helper or dedicated feature via `call read`,
# keeping the flow readable while reusing isolated building blocks.

Feature: Full smoke flow

  Background:
    * url baseUrl

  Scenario: The whole stonks app feels alive
    # ------------------------------------------------------------
    # 1. Market catalog & live prices
    # ------------------------------------------------------------
    Given path 'api/stocks'
    When method get
    Then status 200
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
    And match response.data.cashBalance == '#number'
    * assert response.data.cashBalance == 10000.0
    And match response.data.positions == '#[]'
    And match response.data.unrealizedPnl == 0.0

    # ------------------------------------------------------------
    # 3. Current chaos level
    # ------------------------------------------------------------
    Given path 'api/intensity-level'
    When method get
    Then status 200
    And match response.data == 'PAPER_HANDS'

    # ------------------------------------------------------------
    # 4. Trade history (empty at first)
    # ------------------------------------------------------------
    Given path 'api/trades'
    And param page = 0
    And param size = 5
    When method get
    Then status 200
    And match response.data.content == '#[]'
    And match response.data.totalElements == 0

    # ------------------------------------------------------------
    # 5. Execute a BUY (live market price)
    # ------------------------------------------------------------
    * def buyResult = call read('classpath:features/helpers/buy-gmee.feature')
    * assert buyResult.tradeResult.newCashBalance < 10000.0

    # ------------------------------------------------------------
    # 6. Re-check portfolio (now has GMEE position)
    # ------------------------------------------------------------
    Given path 'api/portfolio'
    When method get
    Then status 200
    And match response.data.cashBalance == '#number'
    * def gmeePos = karate.jsonPath(response, "$.data.positions[?(@.symbol=='GMEE')]")
    * assert gmeePos.length > 0
    And match response.data.unrealizedPnl == '#number'

    # ------------------------------------------------------------
    # 7. Re-check history (now has one entry)
    # ------------------------------------------------------------
    Given path 'api/trades'
    And param page = 0
    And param size = 5
    When method get
    Then status 200
    * assert response.data.totalElements > 0
    And match response.data.content[0].action == 'BUY'
    And match response.data.content[0].symbol == 'GMEE'

    # ------------------------------------------------------------
    # 8. Raise chaos level to MODERATE
    # ------------------------------------------------------------
    * call read('classpath:features/helpers/set-chaos-moderate.feature')

    # ------------------------------------------------------------
    # 9. Trigger a manual chaos event
    # ------------------------------------------------------------
    * def eventResult = call read('classpath:features/helpers/trigger-hype-wave.feature')

    # ------------------------------------------------------------
    # 10. List active chaos events
    # ------------------------------------------------------------
    Given path 'api/chaotic-events'
    When method get
    Then status 200
    And match response.data == '#[]'
    * assert response.data.length > 0
    And match response.data[*].eventId == '#[] #uuid'
    And match response.data[*].type == '#[] #notnull'

    # ------------------------------------------------------------
    # 11. SSE stream — skipped (Karate cannot handle long-lived SSE connections) https://github.com/karatelabs/karate/issues/2698
    # ------------------------------------------------------------

    # ------------------------------------------------------------
    # 12. Sell back the GMEE position
    # ------------------------------------------------------------
    * def sellResult = call read('classpath:features/helpers/sell-gmee.feature')
    * assert sellResult.tradeResult.newQuantity == 0
