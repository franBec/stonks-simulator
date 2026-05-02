      * STOCK DATA COPYBOOK
      * 10 Meme stocks for STONKS Simulator
      * Used by TRADE-VALIDATOR and other programs

       01 WS-STOCK-ENTRY.
          05 WS-STOCK-SYMBOL    PIC X(4).
          05 WS-STOCK-NAME      PIC X(20).
          05 WS-STOCK-BASE-PRICE PIC 9(5)V99.
          05 WS-STOCK-VOLATILITY PIC V99.
          05 WS-STOCK-TREND     PIC X(8).

       01 WS-STOCK-TABLE.
          05 WS-STOCK-ENTRY OCCURS 10 TIMES.
             10 WS-STK-SYMBOL    PIC X(4).
             10 WS-STK-NAME      PIC X(20).
             10 WS-STK-BASE      PIC 9(5)V99.
             10 WS-STK-VOL       PIC V99.
             10 WS-STK-TREND     PIC X(8).
          05 WS-STOCK-COUNT     PIC 9(2) VALUE 10.

       01 WS-STOCK-FOUND       PIC X VALUE 'N'.
          88 STOCK-FOUND        VALUE 'Y'.
          88 STOCK-NOT-FOUND    VALUE 'N'.
       01 WS-STOCK-INDEX       PIC 9(2).
