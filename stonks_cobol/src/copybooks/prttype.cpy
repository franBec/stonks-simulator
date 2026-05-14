      * PORTFOLIO-MGR TYPES COPYBOOK
      * Request/response extensions for PORTFOLIO-MGR program
      * Supplements trdtype.cpy with portfolio-specific fields

      * REQUEST EXTENSION
       01 WS-PMG-REQUEST-EXT.
          05 WS-PMG-REQ-HOLDING-QTY  PIC 9(8).

      * RESPONSE FIELDS
       01 WS-PMG-RESPONSE.
          05 WS-PMG-RES-NEW-CASH     PIC S9(9)V99.
          05 WS-PMG-RES-NEW-QTY      PIC 9(8).
          05 WS-PMG-RES-TOTAL-COST   PIC 9(9)V99.

      * DISPLAY FIELDS
       01 WS-DISPLAY-NEW-CASH        PIC -(9)9.99.
       01 WS-DISPLAY-NEW-QTY         PIC Z(7)9.
       01 WS-DISPLAY-TOTAL-COST      PIC -(9)9.99.
