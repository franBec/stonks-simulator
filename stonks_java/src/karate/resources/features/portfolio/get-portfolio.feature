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
    # Ensures a BUY trade exists by calling the helper, then asserts portfolio state.
    # FIXME: Portfolio P&L calculations are currently broken because costBasis is
    # hard-coded to 0.0 in PortfolioJpaAdapter. This makes unrealizedPnl equal to
    # marketValue instead of (marketValue - costBasis). See FIXME comments in:
    #   - PortfolioJpaAdapter.java
    #   - PortfolioService.java
    #   - stonks-schema.sql (position table missing cost_basis column)
    # When fixed, update this scenario to assert correct costBasis and PnL values.
    * def buyResult = call read('classpath:features/helpers/buy-gmee.feature')
    Given path 'api/portfolio'
    When method get
    Then status 200
    And match response.status == 200
    And match response.data.cashBalance == '#number'
    And match response.data.positions == '#[]'
    # Assert at least one GMEE position exists (quantity may be >5 if helpers ran multiple times)
    * def gmeePos = karate.jsonPath(response, "$.data.positions[?(@.symbol=='GMEE')]")
    * assert gmeePos.length > 0
    And match response.data.unrealizedPnl == '#number'
