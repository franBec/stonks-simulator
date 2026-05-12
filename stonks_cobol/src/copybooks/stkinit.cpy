      * STOCK INITIALIZATION CODE - SHARED COPYBOOK
      * Provides INITIALIZE-STOCKS paragraph for TRADE-VALIDATOR
      * and CATALOG programs.
      * Requires COPY stkdata in WORKING-STORAGE.

       INITIALIZE-STOCKS.
           MOVE 'COBL' TO WS-STK-SYMBOL(1)
           MOVE 'COBOL Corp           ' TO WS-STK-NAME(1)
           MOVE 00100.00 TO WS-STK-BASE(1)
           MOVE 0.05 TO WS-STK-VOL(1)
           MOVE 'BULL    ' TO WS-STK-TREND(1)

           MOVE 'GMEE' TO WS-STK-SYMBOL(2)
           MOVE 'GameStonks           ' TO WS-STK-NAME(2)
           MOVE 00050.00 TO WS-STK-BASE(2)
           MOVE 0.25 TO WS-STK-VOL(2)
           MOVE 'MOON    ' TO WS-STK-TREND(2)

           MOVE 'DOGE' TO WS-STK-SYMBOL(3)
           MOVE 'DogeCoin Ltd         ' TO WS-STK-NAME(3)
           MOVE 00010.00 TO WS-STK-BASE(3)
           MOVE 0.30 TO WS-STK-VOL(3)
           MOVE 'CHAOS   ' TO WS-STK-TREND(3)

           MOVE 'TEND' TO WS-STK-SYMBOL(4)
           MOVE 'Tendie Inc           ' TO WS-STK-NAME(4)
           MOVE 00025.00 TO WS-STK-BASE(4)
           MOVE 0.20 TO WS-STK-VOL(4)
           MOVE 'BEAR    ' TO WS-STK-TREND(4)

           MOVE 'FOMO' TO WS-STK-SYMBOL(5)
           MOVE 'FOMO Holdings        ' TO WS-STK-NAME(5)
           MOVE 00075.00 TO WS-STK-BASE(5)
           MOVE 0.15 TO WS-STK-VOL(5)
           MOVE 'BULL    ' TO WS-STK-TREND(5)

           MOVE 'PAPR' TO WS-STK-SYMBOL(6)
           MOVE 'Paper Hands          ' TO WS-STK-NAME(6)
           MOVE 00015.00 TO WS-STK-BASE(6)
           MOVE 0.10 TO WS-STK-VOL(6)
           MOVE 'BEAR    ' TO WS-STK-TREND(6)

           MOVE 'YOLO' TO WS-STK-SYMBOL(7)
           MOVE 'YOLO Capital          '
              TO WS-STK-NAME(7)
           MOVE 00020.00 TO WS-STK-BASE(7)
           MOVE 0.50 TO WS-STK-VOL(7)
           MOVE 'CHAOS   ' TO WS-STK-TREND(7)

           MOVE 'MEME' TO WS-STK-SYMBOL(8)
           MOVE 'MemeStonks           ' TO WS-STK-NAME(8)
           MOVE 00010.00 TO WS-STK-BASE(8)
           MOVE 0.20 TO WS-STK-VOL(8)
           MOVE 'MOON    ' TO WS-STK-TREND(8)

           MOVE 'BUGS' TO WS-STK-SYMBOL(9)
           MOVE 'Buggy Software       ' TO WS-STK-NAME(9)
           MOVE 00030.00 TO WS-STK-BASE(9)
           MOVE 0.20 TO WS-STK-VOL(9)
           MOVE 'CRASH   ' TO WS-STK-TREND(9)

           MOVE 'JAVA' TO WS-STK-SYMBOL(10)
           MOVE 'JavaBeans            ' TO WS-STK-NAME(10)
           MOVE 00150.00 TO WS-STK-BASE(10)
           MOVE 0.05 TO WS-STK-VOL(10)
           MOVE 'BULL    ' TO WS-STK-TREND(10).
