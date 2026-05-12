COBC = cobc
COBC_FLAGS = -x -I src/copybooks
SRC = src
BIN = bin

all: $(BIN)/trade-validator $(BIN)/price-engine $(BIN)/catalog

$(BIN):
	mkdir -p $(BIN)

$(BIN)/trade-validator: $(SRC)/trade-validator.cob $(SRC)/copybooks/stkinit.cpy $(SRC)/copybooks/stkdata.cpy $(SRC)/copybooks/trdtype.cpy | $(BIN)
	$(COBC) $(COBC_FLAGS) -o $@ $(SRC)/trade-validator.cob

$(BIN)/price-engine: $(SRC)/price-engine.cob $(SRC)/copybooks/pricetyp.cpy | $(BIN)
	$(COBC) $(COBC_FLAGS) -o $@ $(SRC)/price-engine.cob

$(BIN)/catalog: $(SRC)/catalog.cob $(SRC)/copybooks/stkinit.cpy $(SRC)/copybooks/stkdata.cpy | $(BIN)
	$(COBC) $(COBC_FLAGS) -o $@ $(SRC)/catalog.cob

test-valid-buy: $(BIN)/trade-validator
	@echo "--- Test: Valid BUY ---"
	@cat test/valid-buy.json | $(BIN)/trade-validator

test-insufficient-funds: $(BIN)/trade-validator
	@echo "--- Test: Insufficient Funds ---"
	@cat test/insufficient-funds.json | $(BIN)/trade-validator

test-invalid-symbol: $(BIN)/trade-validator
	@echo "--- Test: Invalid Symbol ---"
	@cat test/invalid-symbol.json | $(BIN)/trade-validator

test-invalid-quantity: $(BIN)/trade-validator
	@echo "--- Test: Invalid Quantity ---"
	@cat test/invalid-quantity.json | $(BIN)/trade-validator

test-invalid-action: $(BIN)/trade-validator
	@echo "--- Test: Invalid Action ---"
	@echo '{"action":"HOLD","symbol":"GMEE","quantity":10,"price":45.00,"cashBalance":10000.00}' | $(BIN)/trade-validator

test: test-valid-buy test-insufficient-funds test-invalid-symbol test-invalid-quantity test-invalid-action test-price-bull test-price-crash test-price-moon test-catalog

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

clean:
	rm -rf $(BIN)

lint:
	@./scripts/cobol-lint.sh

.PHONY: all test test-valid-buy test-insufficient-funds test-invalid-symbol test-invalid-quantity test-invalid-action test-price-bull test-price-crash test-price-moon test-catalog lint clean