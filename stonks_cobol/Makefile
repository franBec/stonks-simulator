COBC = cobc
COBC_FLAGS = -x -I src/copybooks
SRC = src
BIN = bin

PRICE_TESTS = test-price-bull test-price-crash test-price-moon

PORTFOLIO_TESTS = test-portfolio-valid-buy test-portfolio-valid-sell \
                  test-portfolio-insufficient-funds \
                  test-portfolio-insufficient-shares \
                  test-portfolio-invalid-action test-portfolio-invalid-symbol \
                  test-portfolio-zero-qty test-portfolio-zero-price

all: $(BIN)/price-engine $(BIN)/catalog $(BIN)/portfolio-mgr

$(BIN):
	mkdir -p $(BIN)

$(BIN)/price-engine: $(SRC)/price-engine.cob $(SRC)/copybooks/pricetyp.cpy | $(BIN)
	$(COBC) $(COBC_FLAGS) -o $@ $(SRC)/price-engine.cob

$(BIN)/catalog: $(SRC)/catalog.cob $(SRC)/copybooks/stkinit.cpy $(SRC)/copybooks/stkdata.cpy | $(BIN)
	$(COBC) $(COBC_FLAGS) -o $@ $(SRC)/catalog.cob

$(BIN)/portfolio-mgr: $(SRC)/portfolio-mgr.cob $(SRC)/copybooks/stkinit.cpy $(SRC)/copybooks/stkdata.cpy $(SRC)/copybooks/trdtype.cpy $(SRC)/copybooks/prttype.cpy | $(BIN)
	$(COBC) $(COBC_FLAGS) -o $@ $(SRC)/portfolio-mgr.cob

test-price: $(PRICE_TESTS)

test-portfolio: $(PORTFOLIO_TESTS)

test: test-price test-catalog test-portfolio

test-price-bull: $(BIN)/price-engine
	@echo "--- Test: Price Engine BULL ---"
	@cat test/price-bull.json | $(BIN)/price-engine

test-price-crash: $(BIN)/price-engine
	@echo "--- Test: Price Engine CRASH ---"
	@cat test/price-crash.json | $(BIN)/price-engine

test-price-moon: $(BIN)/price-engine
	@echo "--- Test: Price Engine MOON ---"
	@cat test/price-moon.json | $(BIN)/price-engine

test-catalog: $(BIN)/catalog
	@echo "--- Test: CATALOG ---"
	@$(BIN)/catalog

test-portfolio-valid-buy: $(BIN)/portfolio-mgr
	@echo "--- Test: Portfolio-Mgr Valid BUY ---"
	@cat test/portfolio-valid-buy.json | $(BIN)/portfolio-mgr

test-portfolio-valid-sell: $(BIN)/portfolio-mgr
	@echo "--- Test: Portfolio-Mgr Valid SELL ---"
	@cat test/portfolio-valid-sell.json | $(BIN)/portfolio-mgr

test-portfolio-insufficient-funds: $(BIN)/portfolio-mgr
	@echo "--- Test: Portfolio-Mgr Insufficient Funds ---"
	@cat test/portfolio-insufficient-funds.json | $(BIN)/portfolio-mgr

test-portfolio-insufficient-shares: $(BIN)/portfolio-mgr
	@echo "--- Test: Portfolio-Mgr Insufficient Shares ---"
	@cat test/portfolio-insufficient-shares.json | $(BIN)/portfolio-mgr

test-portfolio-invalid-action: $(BIN)/portfolio-mgr
	@echo "--- Test: Portfolio-Mgr Invalid Action ---"
	@cat test/portfolio-invalid-action.json | $(BIN)/portfolio-mgr

test-portfolio-invalid-symbol: $(BIN)/portfolio-mgr
	@echo "--- Test: Portfolio-Mgr Invalid Symbol ---"
	@cat test/portfolio-invalid-symbol.json | $(BIN)/portfolio-mgr

test-portfolio-zero-qty: $(BIN)/portfolio-mgr
	@echo "--- Test: Portfolio-Mgr Zero Quantity ---"
	@cat test/portfolio-zero-qty.json | $(BIN)/portfolio-mgr

test-portfolio-zero-price: $(BIN)/portfolio-mgr
	@echo "--- Test: Portfolio-Mgr Zero Price ---"
	@cat test/portfolio-zero-price.json | $(BIN)/portfolio-mgr

clean:
	rm -rf $(BIN)

lint:
	@./scripts/cobol-lint.sh

.PHONY: all test test-price test-catalog test-portfolio lint clean \
        $(PRICE_TESTS) $(PORTFOLIO_TESTS)
