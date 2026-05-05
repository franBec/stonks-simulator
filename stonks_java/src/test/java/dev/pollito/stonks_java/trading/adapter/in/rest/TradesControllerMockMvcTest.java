package dev.pollito.stonks_java.trading.adapter.in.rest;

import static dev.pollito.stonks_java.test.util.MockMvcResultMatchers.hasErrorFields;
import static dev.pollito.stonks_java.test.util.MockMvcResultMatchers.hasStandardApiResponseFields;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.pollito.stonks_java.config.web.ControllerAdvice;
import dev.pollito.stonks_java.trading.adapter.in.rest.mapper.TradeRestMapper;
import dev.pollito.stonks_java.trading.adapter.in.rest.mapper.TradeRestMapperImpl;
import dev.pollito.stonks_java.trading.application.port.in.ValidateTradeUseCase;
import dev.pollito.stonks_java.trading.domain.Trade;
import dev.pollito.stonks_java.trading.domain.TradeValidation;
import dev.pollito.stonks_java.trading.domain.ValidationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TradesController.class)
@Import({ControllerAdvice.class, TradeRestMapperImpl.class})
class TradesControllerMockMvcTest {

  public static final String PATH = "/api/trades/validate";
  public static final String CONTENT_BODY =
      """
      {
        "action": "BUY",
        "symbol": "GMEE",
        "quantity": 10,
        "price": 45.0,
        "cashBalance": 10000.0
      }
      """;

  public static final String INVALID_CONTENT_BODY =
      """
      {
        "action": null,
        "symbol": "TOOLONGSYMBOL",
        "quantity": -1,
        "price": -1.0,
        "cashBalance": -1.0
      }
      """;

  @Autowired private MockMvc mockMvc;

  @MockitoBean private ValidateTradeUseCase validateTradeUseCase;

  @MockitoSpyBean private TradeRestMapper tradeRestMapper;

  @Test
  void validateTradeReturnsOK() throws Exception {
    TradeValidation domainResult =
        new TradeValidation(ValidationStatus.ACCEPTED, null, "TRADE VALIDATED", 450.0, 9550.0);

    when(validateTradeUseCase.validateTrade(any(Trade.class))).thenReturn(domainResult);

    mockMvc
        .perform(
            post(PATH).contentType(APPLICATION_JSON).content(CONTENT_BODY).accept(APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(hasStandardApiResponseFields(PATH, OK))
        .andExpect(jsonPath("$.data.status").value("ACCEPTED"))
        .andExpect(jsonPath("$.data.totalCost").value(450.0))
        .andExpect(jsonPath("$.data.remainingCash").value(9550.0));
  }

  @Test
  void validateTradeReturnsOKWithNullStatus() throws Exception {
    TradeValidation domainResult =
        new TradeValidation(null, null, "TRADE VALIDATED", 450.0, 9550.0);

    when(validateTradeUseCase.validateTrade(any(Trade.class))).thenReturn(domainResult);

    mockMvc
        .perform(
            post(PATH).contentType(APPLICATION_JSON).content(CONTENT_BODY).accept(APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(hasStandardApiResponseFields(PATH, OK))
        .andExpect(jsonPath("$.data.status").value((String) null))
        .andExpect(jsonPath("$.data.totalCost").value(450.0))
        .andExpect(jsonPath("$.data.remainingCash").value(9550.0));
  }

  @Test
  void validateTradeReturnsOKWhenUseCaseReturnsNull() throws Exception {
    when(validateTradeUseCase.validateTrade(any(Trade.class))).thenReturn(null);

    mockMvc
        .perform(
            post(PATH).contentType(APPLICATION_JSON).content(CONTENT_BODY).accept(APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(hasStandardApiResponseFields(PATH, OK))
        .andExpect(jsonPath("$.data").value((String) null));
  }

  @Test
  void validateTradeReturnsBAD_REQUEST() throws Exception {
    mockMvc
        .perform(
            post(PATH)
                .contentType(APPLICATION_JSON)
                .content(INVALID_CONTENT_BODY)
                .accept(APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(hasStandardApiResponseFields(PATH, BAD_REQUEST))
        .andExpect(hasErrorFields(BAD_REQUEST));
  }
}
