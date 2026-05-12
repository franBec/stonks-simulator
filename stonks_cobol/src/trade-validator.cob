       IDENTIFICATION DIVISION.
       PROGRAM-ID. TRADE-VALIDATOR.
      * VALIDATES BUY/SELL TRADE REQUESTS FOR STONKS SIMULATOR
      * READS JSON FROM STDIN, WRITES JSON RESPONSE TO STDOUT

       DATA DIVISION.
       WORKING-STORAGE SECTION.

       COPY stkdata.
       COPY trdtype.

       01 WS-JSON-RESPONSE      PIC X(4096).
       01 WS-DISPLAY-COST       PIC -(9)9.99.
       01 WS-DISPLAY-REMAINING  PIC -(9)9.99.
       01 WS-TOKEN-POOL.
          05 WS-TKN              PIC X(30) OCCURS 12 TIMES.
       01 WS-TOKEN-COUNT         PIC 99 VALUE 0.
       01 WS-I                   PIC 99.
       01 WS-FOUND-KEY           PIC X(30).

       PROCEDURE DIVISION.
       MAIN-LOGIC.
           PERFORM INITIALIZE-STOCKS
           PERFORM READ-INPUT
           PERFORM PARSE-JSON
           PERFORM VALIDATE-TRADE
           PERFORM BUILD-RESPONSE
           DISPLAY FUNCTION TRIM(WS-JSON-RESPONSE)
           STOP RUN.

       COPY stkinit.

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
                    WS-TKN(7) WS-TKN(8) WS-TKN(9)
                    WS-TKN(10) WS-TKN(11) WS-TKN(12)
               COUNT IN WS-TOKEN-COUNT

           PERFORM VARYING WS-I FROM 1 BY 1
               UNTIL WS-I > 11
               MOVE FUNCTION TRIM(WS-TKN(WS-I))
                   TO WS-FOUND-KEY
               EVALUATE WS-FOUND-KEY
                   WHEN 'action'
                       ADD 1 TO WS-I
                       MOVE FUNCTION TRIM(WS-TKN(WS-I))
                           TO WS-REQ-ACTION
                   WHEN 'symbol'
                       ADD 1 TO WS-I
                       MOVE FUNCTION TRIM(WS-TKN(WS-I))
                           TO WS-REQ-SYMBOL
                   WHEN 'quantity'
                       ADD 1 TO WS-I
                       MOVE FUNCTION NUMVAL(
                           FUNCTION TRIM(WS-TKN(WS-I)))
                           TO WS-REQ-QUANTITY
                   WHEN 'price'
                       ADD 1 TO WS-I
                       MOVE FUNCTION NUMVAL(
                           FUNCTION TRIM(WS-TKN(WS-I)))
                           TO WS-REQ-PRICE
                   WHEN 'cashBalance'
                       ADD 1 TO WS-I
                       MOVE FUNCTION NUMVAL(
                           FUNCTION TRIM(WS-TKN(WS-I)))
                           TO WS-REQ-CASH
               END-EVALUATE
           END-PERFORM.

       VALIDATE-TRADE.
           MOVE 'ACCEPTED' TO WS-RES-STATUS
           MOVE SPACES TO WS-RES-ERROR-CODE
           MOVE 'TRADE VALIDATED - PROCEED TO EXECUTION'
               TO WS-RES-MESSAGE
           COMPUTE WS-TOTAL-COST =
               WS-REQ-QUANTITY * WS-REQ-PRICE
           COMPUTE WS-REMAINING-CASH =
               WS-REQ-CASH - WS-TOTAL-COST
           MOVE WS-TOTAL-COST TO WS-RES-TOTAL-COST
           MOVE WS-REMAINING-CASH TO WS-RES-REMAINING

           IF WS-REQ-ACTION NOT = 'BUY' AND
              WS-REQ-ACTION NOT = 'SELL'
               MOVE 'REJECTED' TO WS-RES-STATUS
               MOVE WS-ERR-INVALID-ACTION
                   TO WS-RES-ERROR-CODE
               MOVE 'JOB ABEND S225 - INVALID ACTION'
                   TO WS-RES-MESSAGE
           END-IF

           IF WS-RES-STATUS = 'ACCEPTED'
               SET STOCK-NOT-FOUND TO TRUE
               PERFORM VARYING WS-STOCK-INDEX FROM 1 BY 1
                   UNTIL WS-STOCK-INDEX > WS-STOCK-COUNT
                   IF WS-REQ-SYMBOL =
                       WS-STK-SYMBOL(WS-STOCK-INDEX)
                       SET STOCK-FOUND TO TRUE
                       MOVE WS-STOCK-COUNT
                           TO WS-STOCK-INDEX
                   END-IF
               END-PERFORM
               IF STOCK-NOT-FOUND
                   MOVE 'REJECTED' TO WS-RES-STATUS
                   MOVE WS-ERR-INVALID-SYMBOL
                       TO WS-RES-ERROR-CODE
                   MOVE SPACES TO WS-RES-MESSAGE
                   STRING
                       'JOB ABEND S001 - UNKNOWN SYMBOL '
                           DELIMITED BY SIZE
                       FUNCTION TRIM(WS-REQ-SYMBOL)
                           DELIMITED BY SIZE
                       INTO WS-RES-MESSAGE
                   END-STRING
               END-IF
           END-IF

           IF WS-RES-STATUS = 'ACCEPTED'
               IF WS-REQ-QUANTITY = 0
                   MOVE 'REJECTED' TO WS-RES-STATUS
                   MOVE WS-ERR-INVALID-QTY
                       TO WS-RES-ERROR-CODE
                   MOVE 'JOB ABEND S224 - INVALID QTY'
                       TO WS-RES-MESSAGE
               END-IF
           END-IF

           IF WS-RES-STATUS = 'ACCEPTED'
               IF WS-REQ-PRICE = 0
                   MOVE 'REJECTED' TO WS-RES-STATUS
                   MOVE WS-ERR-INVALID-PRICE
                       TO WS-RES-ERROR-CODE
                   MOVE 'JOB ABEND S226 - INVALID PRICE'
                       TO WS-RES-MESSAGE
               END-IF
           END-IF

           IF WS-RES-STATUS = 'ACCEPTED'
               IF WS-REQ-ACTION = 'BUY'
                   IF WS-REMAINING-CASH < 0
                       MOVE 'REJECTED'
                           TO WS-RES-STATUS
                       MOVE WS-ERR-INSUFF-FUNDS
                           TO WS-RES-ERROR-CODE
                       MOVE 'JOB ABEND S222 - INSUFF FUNDS'
                           TO WS-RES-MESSAGE
                   END-IF
               END-IF
           END-IF.

       BUILD-RESPONSE.
           MOVE WS-TOTAL-COST TO WS-DISPLAY-COST
           MOVE WS-REMAINING-CASH TO WS-DISPLAY-REMAINING
           IF WS-RES-STATUS = 'ACCEPTED'
               STRING
                   '{"status":"ACCEPTED",'
                       DELIMITED BY SIZE
                   '"errorCode":null,'
                       DELIMITED BY SIZE
                   '"message":"TRADE VALIDATED - '
                       DELIMITED BY SIZE
                   'PROCEED TO EXECUTION",'
                       DELIMITED BY SIZE
                   '"totalCost":'
                       DELIMITED BY SIZE
                   FUNCTION TRIM(WS-DISPLAY-COST)
                       DELIMITED BY SIZE
                   ',"remainingCash":'
                       DELIMITED BY SIZE
                   FUNCTION TRIM(WS-DISPLAY-REMAINING)
                       DELIMITED BY SIZE
                   '}'
                       DELIMITED BY SIZE
                   INTO WS-JSON-RESPONSE
               END-STRING
           ELSE
               STRING
                   '{"status":"REJECTED",'
                       DELIMITED BY SIZE
                   '"errorCode":"'
                       DELIMITED BY SIZE
                   FUNCTION TRIM(WS-RES-ERROR-CODE)
                       DELIMITED BY SIZE
                   '",'
                       DELIMITED BY SIZE
                   '"message":"'
                       DELIMITED BY SIZE
                   FUNCTION TRIM(WS-RES-MESSAGE)
                       DELIMITED BY SIZE
                   '",'
                       DELIMITED BY SIZE
                   '"totalCost":'
                       DELIMITED BY SIZE
                   FUNCTION TRIM(WS-DISPLAY-COST)
                       DELIMITED BY SIZE
                   ',"remainingCash":'
                       DELIMITED BY SIZE
                   FUNCTION TRIM(WS-DISPLAY-REMAINING)
                       DELIMITED BY SIZE
                   '}'
                       DELIMITED BY SIZE
                   INTO WS-JSON-RESPONSE
               END-STRING.
