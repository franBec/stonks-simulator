Feature: Market catalog and live prices

  Background:
    * url baseUrl

  Scenario: Get all stocks
    Given path 'api/market/stocks'
    When method get
    Then status 200
    And match response.status == 200
    And match response.data == '#[]'
    And match response.data[*].symbol contains [ 'TEND', 'YOLO', 'BUGS', 'JAVA', 'MEME', 'PAPR', 'GMEE', 'COBL', 'DOGE', 'FOMO' ]
    # Capture GMEE price for later use
    * def gmeePrice = karate.jsonPath(response, "$.data[?(@.symbol=='GMEE')].price")[0]
    * assert gmeePrice > 0
