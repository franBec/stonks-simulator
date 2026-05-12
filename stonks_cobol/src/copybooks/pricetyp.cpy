      * PRICE ENGINE TYPES COPYBOOK
      * Shared record layouts for PRICE-ENGINE program
      * Covers request/response for price simulation

      * REQUEST FIELDS
       01 WS-PRICE-REQUEST.
           05 WS-PREQ-CURRENT    PIC 9(7)V99.
           05 WS-PREQ-VOLATILITY PIC V99.
           05 WS-PREQ-TREND      PIC X(8).

      * RESPONSE FIELDS
       01 WS-PRICE-RESPONSE.
           05 WS-PRES-NEW-PRICE  PIC 9(7)V99.

      * WORK FIELDS
       01 WS-PRICE-WORK.
           05 WS-PRANDOM         PIC 9(9)V9(9).
           05 WS-TREND-BIAS      PIC S9(3)V9(6).
           05 WS-RANDOM-SHOCK    PIC S9(3)V9(6).
           05 WS-STEP-CHANGE     PIC S9(3)V9(6).
           05 WS-CLAMPED-PRICE   PIC 9(7)V99.
           05 WS-NEW-PRICE-F     PIC 9(7)V99.
           05 WS-MAX-MOVE        PIC 9(3)V9(6).
           05 WS-CURRENT-F       PIC 9(7)V99.
           05 WS-SEEDED          PIC X VALUE 'N'.
              88 WS-SEED-DONE    VALUE 'Y'.
