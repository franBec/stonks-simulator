import { useState } from "react"
import { useGetTradeHistory } from "@/api/hooks"
import { unwrap } from "@/api/hooks"
import type { TradeHistoryItem, TradeHistoryResponseData } from "@/api/hooks"

const PAGE_SIZE = 15

export function TradeHistory() {
  const [page, setPage] = useState(0)

  const { data, isLoading } = useGetTradeHistory<TradeHistoryResponseData>(
    { page, size: PAGE_SIZE, sort: "executedAt,desc" },
    { query: { select: unwrap<TradeHistoryResponseData> } },
  )

  const items = data?.content ?? []
  const totalElements = data?.totalElements ?? 0
  const totalPages = Math.max(1, Math.ceil(totalElements / PAGE_SIZE))

  return (
    <div className="terminal-border animate-fade-in p-4 font-mono text-xs">
      <div className="mb-3 border-b border-green-500/10 pb-2 text-muted-foreground">
        TRADE HISTORY
      </div>

      {isLoading ? (
        <div className="cursor-blink py-4 text-center text-muted-foreground">
          LOADING TRADE HISTORY...
        </div>
      ) : items.length === 0 ? (
        <div className="py-4 text-center text-muted-foreground">
          NO TRADES YET. YOLO SOMETHING.
        </div>
      ) : (
        <>
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="border-b border-green-500/10 text-left text-muted-foreground">
                  <th className="whitespace-nowrap px-2 py-1 font-normal">
                    TIME
                  </th>
                  <th className="whitespace-nowrap px-2 py-1 font-normal">
                    ACT
                  </th>
                  <th className="whitespace-nowrap px-2 py-1 font-normal">
                    SYM
                  </th>
                  <th className="whitespace-nowrap px-2 py-1 text-right font-normal">
                    QTY
                  </th>
                  <th className="whitespace-nowrap px-2 py-1 text-right font-normal">
                    PRICE
                  </th>
                  <th className="whitespace-nowrap px-2 py-1 text-right font-normal">
                    TOTAL
                  </th>
                  <th className="whitespace-nowrap px-2 py-1 text-right font-normal">
                    CASH AFTER
                  </th>
                </tr>
              </thead>
              <tbody>
                {items.map((item: TradeHistoryItem) => (
                  <tr
                    key={item.id}
                    className="border-b border-green-500/5 hover:bg-green-500/5"
                  >
                    <td className="whitespace-nowrap px-2 py-1 text-muted-foreground">
                      {new Date(item.executedAt).toLocaleTimeString()}
                    </td>
                    <td className="whitespace-nowrap px-2 py-1">
                      <span
                        className={
                          item.action === "BUY"
                            ? "text-green-400"
                            : "text-red-400"
                        }
                      >
                        {item.action}
                      </span>
                    </td>
                    <td className="whitespace-nowrap px-2 py-1 font-bold text-foreground">
                      {item.symbol}
                    </td>
                    <td className="whitespace-nowrap px-2 py-1 text-right">
                      {item.quantity}
                    </td>
                    <td className="whitespace-nowrap px-2 py-1 text-right">
                      ${item.price.toFixed(2)}
                    </td>
                    <td
                      className={`whitespace-nowrap px-2 py-1 text-right ${
                        item.action === "BUY" ? "text-red-400" : "text-green-400"
                      }`}
                    >
                      ${item.totalCost.toFixed(2)}
                    </td>
                    <td className="whitespace-nowrap px-2 py-1 text-right text-muted-foreground">
                      ${item.cashBalanceAfter.toFixed(2)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {totalPages > 1 && (
            <div className="mt-3 flex items-center justify-between border-t border-green-500/10 pt-2">
              <button
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                disabled={page === 0}
                className="cursor-pointer rounded border border-green-500/15 px-2 py-0.5 text-muted-foreground transition-colors hover:border-green-400/30 hover:text-green-400 disabled:cursor-not-allowed disabled:opacity-30"
              >
                PREV
              </button>
              <span className="text-muted-foreground">
                {page + 1} / {totalPages}
              </span>
              <button
                onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                disabled={page >= totalPages - 1}
                className="cursor-pointer rounded border border-green-500/15 px-2 py-0.5 text-muted-foreground transition-colors hover:border-green-400/30 hover:text-green-400 disabled:cursor-not-allowed disabled:opacity-30"
              >
                NEXT
              </button>
            </div>
          )}
        </>
      )}
    </div>
  )
}
