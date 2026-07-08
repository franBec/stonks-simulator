       IDENTIFICATION DIVISION.
       PROGRAM-ID. PORTFOLIO-MGR.
      * EXECUTES BUY/SELL TRADES WITH PORTFOLIO STATE MANAGEMENT
      * READS JSON FROM STDIN, WRITES JSON RESPONSE TO STDOUT
      * VALIDATES AND COMPUTES NEW CASH BALANCE, POSITION QUANTITY

       DATA DIVISION.
       WORKING-STORAGE SECTION.

       COPY stkdata.
       COPY trdtype.
       COPY prttype.

       01 WS-JSON-RESPONSE      PIC X(4096).
       01 WS-DISPLAY-COST       PIC -(9)9.99.
       01 WS-DISPLAY-REMAINING  PIC -(9)9.99.
        01 WS-TOKEN-POOL.
           05 WS-TKN              PIC X(30) OCCURS 16 TIMES.
        01 WS-TOKEN-COUNT         PIC 99 VALUE 0.
       01 WS-I                   PIC 99.
       01 WS-FOUND-KEY           PIC X(30).

       PROCEDURE DIVISION.
       MAIN-LOGIC.
           PERFORM INITIALIZE-STOCKS
           PERFORM READ-INPUT
           PERFORM PARSE-JSON
           PERFORM EXECUTE-TRADE
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
                     WS-TKN(13) WS-TKN(14) WS-TKN(15)
                     WS-TKN(16)
                COUNT IN WS-TOKEN-COUNT

            PERFORM VARYING WS-I FROM 1 BY 1
                UNTIL WS-I > 15
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
                    WHEN 'holdingQty'
                        ADD 1 TO WS-I
                        MOVE FUNCTION NUMVAL(
                            FUNCTION TRIM(WS-TKN(WS-I)))
                            TO WS-PMG-REQ-HOLDING-QTY
                    WHEN 'feeRate'
                        ADD 1 TO WS-I
                        MOVE FUNCTION NUMVAL(
                            FUNCTION TRIM(WS-TKN(WS-I)))
                            TO WS-PMG-REQ-FEE-RATE
                END-EVALUATE
           END-PERFORM.

       EXECUTE-TRADE.
           MOVE 'ACCEPTED' TO WS-RES-STATUS
           MOVE SPACES TO WS-RES-ERROR-CODE
           MOVE SPACES TO WS-RES-MESSAGE
           COMPUTE WS-TOTAL-COST =
               WS-REQ-QUANTITY * WS-REQ-PRICE

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
                    COMPUTE WS-PMG-FEE =
                        WS-TOTAL-COST * WS-PMG-REQ-FEE-RATE
                    COMPUTE WS-REMAINING-CASH =
                        WS-REQ-CASH - WS-TOTAL-COST - WS-PMG-FEE
                    IF WS-REMAINING-CASH < 0
                        MOVE 'REJECTED'
                            TO WS-RES-STATUS
                        MOVE WS-ERR-INSUFF-FUNDS
                            TO WS-RES-ERROR-CODE
                        MOVE 'JOB ABEND S222 - INSUFF FUNDS'
                            TO WS-RES-MESSAGE
                    END-IF
                END-IF
            END-IF

           IF WS-RES-STATUS = 'ACCEPTED'
               IF WS-REQ-ACTION = 'SELL'
                   IF WS-PMG-REQ-HOLDING-QTY <
                      WS-REQ-QUANTITY
                       MOVE 'REJECTED'
                           TO WS-RES-STATUS
                       MOVE WS-ERR-INSUFF-SHARES
                           TO WS-RES-ERROR-CODE
                       MOVE 'JOB ABEND S223 - INSUFF SHARES'
                           TO WS-RES-MESSAGE
                   END-IF
               END-IF
           END-IF

            IF WS-RES-STATUS = 'ACCEPTED'
                IF WS-REQ-ACTION = 'BUY'
                    COMPUTE WS-PMG-FEE =
                        WS-TOTAL-COST * WS-PMG-REQ-FEE-RATE
                    COMPUTE WS-PMG-RES-NEW-CASH =
                        WS-REQ-CASH - WS-TOTAL-COST - WS-PMG-FEE
                    COMPUTE WS-PMG-RES-NEW-QTY =
                        WS-PMG-REQ-HOLDING-QTY +
                        WS-REQ-QUANTITY
                ELSE
                    COMPUTE WS-PMG-FEE =
                        WS-TOTAL-COST * WS-PMG-REQ-FEE-RATE
                    COMPUTE WS-PMG-RES-NEW-CASH =
                        WS-REQ-CASH + WS-TOTAL-COST - WS-PMG-FEE
                    COMPUTE WS-PMG-RES-NEW-QTY =
                        WS-PMG-REQ-HOLDING-QTY -
                        WS-REQ-QUANTITY
                END-IF
                MOVE WS-TOTAL-COST TO WS-PMG-RES-TOTAL-COST
                MOVE WS-PMG-FEE TO WS-PMG-FEE-DISPLAY
                STRING
                    'TRADE EXECUTED - '
                        DELIMITED BY SIZE
                    FUNCTION TRIM(WS-REQ-ACTION)
                        DELIMITED BY SIZE
                    ' '
                        DELIMITED BY SIZE
                    FUNCTION TRIM(WS-REQ-SYMBOL)
                        DELIMITED BY SIZE
                    ' [FEE: $'
                        DELIMITED BY SIZE
                    FUNCTION TRIM(WS-PMG-FEE-DISPLAY)
                        DELIMITED BY SIZE
                    ']'
                        DELIMITED BY SIZE
                    INTO WS-RES-MESSAGE
                END-STRING
           ELSE
               MOVE WS-REQ-CASH TO WS-PMG-RES-NEW-CASH
               MOVE WS-PMG-REQ-HOLDING-QTY
                   TO WS-PMG-RES-NEW-QTY
               MOVE 0 TO WS-PMG-RES-TOTAL-COST
           END-IF.

       BUILD-RESPONSE.
           MOVE WS-PMG-RES-NEW-CASH TO WS-DISPLAY-NEW-CASH
           MOVE WS-PMG-RES-NEW-QTY TO WS-DISPLAY-NEW-QTY
           MOVE WS-PMG-RES-TOTAL-COST TO WS-DISPLAY-TOTAL-COST
           IF WS-RES-STATUS = 'ACCEPTED'
               STRING
                   '{"status":"ACCEPTED",'
                       DELIMITED BY SIZE
                   '"errorCode":null,'
                       DELIMITED BY SIZE
                   '"message":"'
                       DELIMITED BY SIZE
                   FUNCTION TRIM(WS-RES-MESSAGE)
                       DELIMITED BY SIZE
                   '",'
                       DELIMITED BY SIZE
                   '"newCashBalance":'
                       DELIMITED BY SIZE
                   FUNCTION TRIM(WS-DISPLAY-NEW-CASH)
                       DELIMITED BY SIZE
                   ',"newQuantity":'
                       DELIMITED BY SIZE
                   FUNCTION TRIM(WS-DISPLAY-NEW-QTY)
                       DELIMITED BY SIZE
                   ',"totalCost":'
                       DELIMITED BY SIZE
                   FUNCTION TRIM(WS-DISPLAY-TOTAL-COST)
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
                   '"newCashBalance":'
                       DELIMITED BY SIZE
                   FUNCTION TRIM(WS-DISPLAY-NEW-CASH)
                       DELIMITED BY SIZE
                   ',"newQuantity":'
                       DELIMITED BY SIZE
                   FUNCTION TRIM(WS-DISPLAY-NEW-QTY)
                       DELIMITED BY SIZE
                   ',"totalCost":'
                       DELIMITED BY SIZE
                   FUNCTION TRIM(WS-DISPLAY-TOTAL-COST)
                       DELIMITED BY SIZE
                   '}'
                       DELIMITED BY SIZE
                   INTO WS-JSON-RESPONSE
               END-STRING.
