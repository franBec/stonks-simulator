       IDENTIFICATION DIVISION.
       PROGRAM-ID. CATALOG.
      * OUTPUTS THE 10 MEME STOCKS CATALOG AS JSON
      * READS NOTHING, WRITES JSON TO STDOUT
      * SOURCE OF TRUTH FOR STOCK DATA

       DATA DIVISION.
       WORKING-STORAGE SECTION.

       COPY stkdata.

       01 WS-JSON-RESPONSE      PIC X(4096).
       01 WS-I                  PIC 99.
       01 WS-PTR                PIC 9(4).
       01 WS-BASE-DISP          PIC ZZZZZ9.99.
       01 WS-VOL-DISP           PIC 9.99.

       PROCEDURE DIVISION.
       MAIN-LOGIC.
           PERFORM INITIALIZE-STOCKS
           PERFORM BUILD-JSON
           DISPLAY FUNCTION TRIM(WS-JSON-RESPONSE)
           STOP RUN.

        INITIALIZE-STOCKS.
            MOVE 'COBL' TO WS-STK-SYMBOL(1)
            MOVE 'COBOL Corp           ' TO WS-STK-NAME(1)
            MOVE 'Legacy systems never die    ' TO WS-STK-DESC(1)
            MOVE 00100.00 TO WS-STK-BASE(1)
            MOVE 0.05 TO WS-STK-VOL(1)
            MOVE 'BULL    ' TO WS-STK-TREND(1)

            MOVE 'GMEE' TO WS-STK-SYMBOL(2)
            MOVE 'GameStonks           ' TO WS-STK-NAME(2)
            MOVE 'To the moon!                 ' TO WS-STK-DESC(2)
            MOVE 00050.00 TO WS-STK-BASE(2)
            MOVE 0.25 TO WS-STK-VOL(2)
            MOVE 'MOON    ' TO WS-STK-TREND(2)

            MOVE 'DOGE' TO WS-STK-SYMBOL(3)
            MOVE 'DogeCoin Ltd         ' TO WS-STK-NAME(3)
            MOVE 'Much profit, very wow         ' TO WS-STK-DESC(3)
            MOVE 00010.00 TO WS-STK-BASE(3)
            MOVE 0.30 TO WS-STK-VOL(3)
            MOVE 'CHAOS   ' TO WS-STK-TREND(3)

            MOVE 'TEND' TO WS-STK-SYMBOL(4)
            MOVE 'Tendie Inc           ' TO WS-STK-NAME(4)
            MOVE 'WSB favorite                  ' TO WS-STK-DESC(4)
            MOVE 00025.00 TO WS-STK-BASE(4)
            MOVE 0.20 TO WS-STK-VOL(4)
            MOVE 'BEAR    ' TO WS-STK-TREND(4)

            MOVE 'FOMO' TO WS-STK-SYMBOL(5)
            MOVE 'FOMO Holdings        ' TO WS-STK-NAME(5)
            MOVE 'Buy high, sell higher         ' TO WS-STK-DESC(5)
            MOVE 00075.00 TO WS-STK-BASE(5)
            MOVE 0.15 TO WS-STK-VOL(5)
            MOVE 'BULL    ' TO WS-STK-TREND(5)

            MOVE 'PAPR' TO WS-STK-SYMBOL(6)
            MOVE 'Paper Hands          ' TO WS-STK-NAME(6)
            MOVE 'For the weak                  ' TO WS-STK-DESC(6)
            MOVE 00015.00 TO WS-STK-BASE(6)
            MOVE 0.10 TO WS-STK-VOL(6)
            MOVE 'BEAR    ' TO WS-STK-TREND(6)

            MOVE 'YOLO' TO WS-STK-SYMBOL(7)
            MOVE 'YOLO Capital          '
               TO WS-STK-NAME(7)
            MOVE 'You only live once            ' TO WS-STK-DESC(7)
            MOVE 00020.00 TO WS-STK-BASE(7)
            MOVE 0.50 TO WS-STK-VOL(7)
            MOVE 'CHAOS   ' TO WS-STK-TREND(7)

            MOVE 'MEME' TO WS-STK-SYMBOL(8)
            MOVE 'MemeStonks           ' TO WS-STK-NAME(8)
            MOVE 'Viral potential               ' TO WS-STK-DESC(8)
            MOVE 00010.00 TO WS-STK-BASE(8)
            MOVE 0.20 TO WS-STK-VOL(8)
            MOVE 'MOON    ' TO WS-STK-TREND(8)

            MOVE 'BUGS' TO WS-STK-SYMBOL(9)
            MOVE 'Buggy Software       ' TO WS-STK-NAME(9)
            MOVE 'It compiles, ship it!         ' TO WS-STK-DESC(9)
            MOVE 00030.00 TO WS-STK-BASE(9)
            MOVE 0.20 TO WS-STK-VOL(9)
            MOVE 'CRASH   ' TO WS-STK-TREND(9)

            MOVE 'JAVA' TO WS-STK-SYMBOL(10)
            MOVE 'JavaBeans            ' TO WS-STK-NAME(10)
            MOVE 'Write once, run anywhere      ' TO WS-STK-DESC(10)
            MOVE 00150.00 TO WS-STK-BASE(10)
            MOVE 0.05 TO WS-STK-VOL(10)
            MOVE 'BULL    ' TO WS-STK-TREND(10).

       BUILD-JSON.
           MOVE 1 TO WS-PTR
           MOVE SPACES TO WS-JSON-RESPONSE
           STRING '[' DELIMITED BY SIZE
               INTO WS-JSON-RESPONSE
               WITH POINTER WS-PTR
           END-STRING
           PERFORM VARYING WS-I FROM 1 BY 1
               UNTIL WS-I > 10
               IF WS-I > 1
                   STRING ',' DELIMITED BY SIZE
                       INTO WS-JSON-RESPONSE
                       WITH POINTER WS-PTR
                   END-STRING
               END-IF
               MOVE WS-STK-BASE(WS-I) TO WS-BASE-DISP
               MOVE WS-STK-VOL(WS-I) TO WS-VOL-DISP
                STRING
                    '{"symbol":"'
                        DELIMITED BY SIZE
                    FUNCTION TRIM(WS-STK-SYMBOL(WS-I))
                        DELIMITED BY SIZE
                    '","name":"'
                        DELIMITED BY SIZE
                    FUNCTION TRIM(WS-STK-NAME(WS-I))
                        DELIMITED BY SIZE
                    '","description":"'
                        DELIMITED BY SIZE
                    FUNCTION TRIM(WS-STK-DESC(WS-I))
                        DELIMITED BY SIZE
                    '","basePrice":'
                        DELIMITED BY SIZE
                    FUNCTION TRIM(WS-BASE-DISP)
                        DELIMITED BY SIZE
                    ',"volatility":'
                        DELIMITED BY SIZE
                    FUNCTION TRIM(WS-VOL-DISP)
                        DELIMITED BY SIZE
                    ',"trend":"'
                        DELIMITED BY SIZE
                    FUNCTION TRIM(WS-STK-TREND(WS-I))
                        DELIMITED BY SIZE
                    '"}'
                        DELIMITED BY SIZE
                    INTO WS-JSON-RESPONSE
                    WITH POINTER WS-PTR
                END-STRING
           END-PERFORM
           STRING ']' DELIMITED BY SIZE
               INTO WS-JSON-RESPONSE
               WITH POINTER WS-PTR
           END-STRING.
