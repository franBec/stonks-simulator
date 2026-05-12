       IDENTIFICATION DIVISION.
       PROGRAM-ID. PRICE-ENGINE.
      * COMPUTES NEW STOCK PRICES USING RANDOM WALK WITH TREND BIAS
      * READS JSON FROM STDIN, WRITES JSON RESPONSE TO STDOUT

       DATA DIVISION.
       WORKING-STORAGE SECTION.

       COPY pricetyp.

       01 WS-JSON-RESPONSE      PIC X(4096).
       01 WS-DISPLAY-PRICE      PIC -(9)9.99.
       01 WS-DATE-STR           PIC X(21).
       01 WS-SEED-VAL           PIC 9(14).
       01 WS-INPUT-LENGTH       PIC 9(4).
       01 WS-INPUT-BUFFER       PIC X(4096).
       01 WS-TOKEN-POOL.
          05 WS-TKN             PIC X(30) OCCURS 8 TIMES.
       01 WS-TOKEN-COUNT        PIC 99 VALUE 0.
       01 WS-I                  PIC 99.
       01 WS-FOUND-KEY          PIC X(30).

       PROCEDURE DIVISION.
       MAIN-LOGIC.
           PERFORM SEED-RANDOM
           PERFORM READ-INPUT
           PERFORM PARSE-JSON
           PERFORM COMPUTE-NEW-PRICE
           PERFORM BUILD-RESPONSE
           DISPLAY FUNCTION TRIM(WS-JSON-RESPONSE)
           STOP RUN.

       SEED-RANDOM.
           IF NOT WS-SEED-DONE
               MOVE FUNCTION CURRENT-DATE TO WS-DATE-STR
               MOVE FUNCTION NUMVAL(
                   WS-DATE-STR(1:14)) TO WS-SEED-VAL
               COMPUTE WS-PRANDOM =
                   FUNCTION RANDOM(WS-SEED-VAL)
               SET WS-SEED-DONE TO TRUE
           END-IF.

       READ-INPUT.
           ACCEPT WS-INPUT-BUFFER.

       PARSE-JSON.
           INSPECT WS-INPUT-BUFFER
               REPLACING ALL '"' BY ' '
           INSPECT WS-INPUT-BUFFER
               REPLACING ALL '{' BY ' '
           INSPECT WS-INPUT-BUFFER
               REPLACING ALL '}' BY ' '
           INSPECT WS-INPUT-BUFFER
               REPLACING ALL ':' BY ' '
           INSPECT WS-INPUT-BUFFER
               REPLACING ALL ',' BY ' '

           INITIALIZE WS-TOKEN-POOL
           MOVE 0 TO WS-TOKEN-COUNT
           UNSTRING WS-INPUT-BUFFER
               DELIMITED BY ALL SPACES
               INTO WS-TKN(1) WS-TKN(2) WS-TKN(3)
                    WS-TKN(4) WS-TKN(5) WS-TKN(6)
                    WS-TKN(7) WS-TKN(8)
               COUNT IN WS-TOKEN-COUNT

           PERFORM VARYING WS-I FROM 1 BY 1
               UNTIL WS-I > 7
               MOVE FUNCTION TRIM(WS-TKN(WS-I))
                   TO WS-FOUND-KEY
               EVALUATE WS-FOUND-KEY
                   WHEN 'currentPrice'
                       ADD 1 TO WS-I
                       MOVE FUNCTION NUMVAL(
                           FUNCTION TRIM(WS-TKN(WS-I)))
                           TO WS-PREQ-CURRENT
                   WHEN 'volatility'
                       ADD 1 TO WS-I
                       MOVE FUNCTION NUMVAL(
                           FUNCTION TRIM(WS-TKN(WS-I)))
                           TO WS-PREQ-VOLATILITY
                   WHEN 'trend'
                       ADD 1 TO WS-I
                       MOVE FUNCTION TRIM(WS-TKN(WS-I))
                           TO WS-PREQ-TREND
               END-EVALUATE
           END-PERFORM.

       COMPUTE-NEW-PRICE.
      *    MAP TREND STRING TO NUMERIC BIAS
           MOVE 0 TO WS-TREND-BIAS
           EVALUATE WS-PREQ-TREND
               WHEN 'BULL'
                   MOVE 0.003 TO WS-TREND-BIAS
               WHEN 'BEAR'
                   MOVE -0.003 TO WS-TREND-BIAS
               WHEN 'MOON'
                   MOVE 0.01 TO WS-TREND-BIAS
               WHEN 'CHAOS'
                   COMPUTE WS-PRANDOM = FUNCTION RANDOM
                   COMPUTE WS-TREND-BIAS =
                       (WS-PRANDOM - 0.5) * 0.04
               WHEN 'CRASH'
                   MOVE -0.05 TO WS-TREND-BIAS
           END-EVALUATE

      *    GENERATE RANDOM WALK SHOCK
           COMPUTE WS-PRANDOM = FUNCTION RANDOM
           COMPUTE WS-RANDOM-SHOCK =
               (WS-PRANDOM - 0.5) * 2.0 * WS-PREQ-VOLATILITY

      *    COMPUTE NEW PRICE
           COMPUTE WS-NEW-PRICE-F =
               WS-PREQ-CURRENT *
               (1.0 + WS-TREND-BIAS + WS-RANDOM-SHOCK)

      *    APPLY CIRCUIT BREAKER: MAX 15% SINGLE-STEP CHANGE
           MOVE WS-PREQ-CURRENT TO WS-CURRENT-F
           COMPUTE WS-STEP-CHANGE =
               (WS-NEW-PRICE-F - WS-CURRENT-F) / WS-CURRENT-F
           IF WS-STEP-CHANGE > 0.15
               COMPUTE WS-CLAMPED-PRICE =
                   WS-CURRENT-F * 1.15
           ELSE
               IF WS-STEP-CHANGE < -0.15
                   COMPUTE WS-CLAMPED-PRICE =
                       WS-CURRENT-F * 0.85
               ELSE
                   MOVE WS-NEW-PRICE-F TO WS-CLAMPED-PRICE
               END-IF
           END-IF

      *    APPLY ABSOLUTE PRICE FLOOR AND CEILING
           IF WS-CLAMPED-PRICE > 500.00
               MOVE 500.00 TO WS-CLAMPED-PRICE
           END-IF
           IF WS-CLAMPED-PRICE < 0.10
               MOVE 0.10 TO WS-CLAMPED-PRICE
           END-IF

           MOVE WS-CLAMPED-PRICE TO WS-PRES-NEW-PRICE.

       BUILD-RESPONSE.
           MOVE WS-PRES-NEW-PRICE TO WS-DISPLAY-PRICE
           STRING
               '{"newPrice":'
                   DELIMITED BY SIZE
               FUNCTION TRIM(WS-DISPLAY-PRICE)
                   DELIMITED BY SIZE
               '}'
                   DELIMITED BY SIZE
               INTO WS-JSON-RESPONSE
           END-STRING.
