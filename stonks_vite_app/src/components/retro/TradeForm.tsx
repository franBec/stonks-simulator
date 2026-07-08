import { useState, useMemo } from "react"
import { useQueryClient } from "@tanstack/react-query"
import { useGetStocks, useExecuteTrade } from "@/api/hooks"
import { unwrap } from "@/api/hooks"
import type { StockPrice, TradeExecutionResult } from "@/api/hooks"
import type { TradeExecutionRequest } from "@/api/hooks"
import { getGetPortfolioQueryKey } from "@/__generated__/api/portfolio/portfolio"
import { getGetTradeHistoryQueryKey } from "@/__generated__/api/trades/trades"

type Action = "BUY" | "SELL"

export function TradeForm() {
  const queryClient = useQueryClient()
  const { data: stocks } = useGetStocks<StockPrice[]>({
    query: { select: unwrap<StockPrice[]>, refetchInterval: 5000 },
  })

  const [action, setAction] = useState<Action>("BUY")
  const [symbol, setSymbol] = useState("")
  const [quantity, setQuantity] = useState(1)
  const [result, setResult] = useState<TradeExecutionResult | null>(null)

  const executeMutation = useExecuteTrade()

  const selectedStock = useMemo(() => {
    if (!stocks || !symbol) return null
    return stocks.find((s) => s.symbol === symbol) ?? null
  }, [stocks, symbol])

  const estimatedCost = selectedStock ? selectedStock.price * quantity : 0

  const handleSubmit = () => {
    if (!selectedStock || quantity < 1) return

    const request: TradeExecutionRequest = {
      action,
      symbol,
      quantity,
    }

    executeMutation.mutate(
      { data: request },
      {
        onSuccess: (response) => {
          const executionResult = unwrap<TradeExecutionResult>(response)
          setResult(executionResult)

          if (executionResult.status === "ACCEPTED") {
            setQuantity(1)
            queryClient.invalidateQueries({
              queryKey: getGetPortfolioQueryKey(),
            })
            queryClient.invalidateQueries({
              queryKey: getGetTradeHistoryQueryKey(),
            })
          }
        },
      },
    )
  }

  const handleSymbolClick = (s: StockPrice) => {
    setSymbol(s.symbol)
    setResult(null)
  }

  return (
    <div className="terminal-border animate-fade-in p-4 font-mono text-xs">
      <div className="mb-3 border-b border-green-500/10 pb-2 text-muted-foreground">
        EXECUTE TRADE
      </div>

      <div className="mb-3 flex gap-2">
        <button
          onClick={() => {
            setAction("BUY")
            setResult(null)
          }}
          className={`cursor-pointer rounded border px-3 py-1 text-xs transition-colors ${
            action === "BUY"
              ? "border-green-400/50 bg-green-500/15 text-green-400"
              : "border-green-500/15 text-muted-foreground hover:border-green-400/30 hover:text-green-400"
          }`}
        >
          BUY
        </button>
        <button
          onClick={() => {
            setAction("SELL")
            setResult(null)
          }}
          className={`cursor-pointer rounded border px-3 py-1 text-xs transition-colors ${
            action === "SELL"
              ? "border-red-400/50 bg-red-500/15 text-red-400"
              : "border-green-500/15 text-muted-foreground hover:border-red-400/30 hover:text-red-400"
          }`}
        >
          SELL
        </button>
      </div>

      {symbol && selectedStock && (
        <div className="mb-3 space-y-0.5 text-muted-foreground">
          <div className="flex justify-between">
            <span>{selectedStock.name}</span>
            <span
              className={
                selectedStock.change >= 0 ? "text-green-400" : "text-red-400"
              }
            >
              ${selectedStock.price.toFixed(2)}
            </span>
          </div>
          <div
            className={
              selectedStock.change >= 0 ? "text-green-400/70" : "text-red-400/70"
            }
          >
            {selectedStock.change >= 0 ? "+" : ""}
            {selectedStock.change.toFixed(2)} (
            {selectedStock.changePercent >= 0 ? "+" : ""}
            {selectedStock.changePercent.toFixed(2)}%)
          </div>
        </div>
      )}

      <div className="mb-3">
        <div className="mb-1 text-muted-foreground">STOCK</div>
        <div className="grid grid-cols-2 gap-1">
          {stocks?.map((s) => (
            <button
              key={s.symbol}
              onClick={() => handleSymbolClick(s)}
              className={`cursor-pointer rounded border px-2 py-1 text-left text-xs transition-colors ${
                symbol === s.symbol
                  ? "border-green-400/40 bg-green-500/10 text-foreground"
                  : "border-green-500/10 text-muted-foreground hover:border-green-400/20 hover:text-foreground"
              }`}
            >
              <span className="font-bold">{s.symbol}</span>{" "}
              <span
                className={
                  s.change >= 0 ? "text-green-400/70" : "text-red-400/70"
                }
              >
                ${s.price.toFixed(2)}
              </span>
            </button>
          ))}
        </div>
      </div>

      <div className="mb-3">
        <label className="mb-1 block text-muted-foreground">QUANTITY</label>
        <input
          type="number"
          min={1}
          value={quantity}
          onChange={(e) => {
            setQuantity(Math.max(1, parseInt(e.target.value) || 1))
            setResult(null)
          }}
          className="w-full rounded border border-green-500/20 bg-transparent px-3 py-1.5 font-mono text-foreground outline-none focus:border-green-400/40"
        />
      </div>

      {symbol && (
        <div className="mb-3 flex justify-between text-muted-foreground">
          <span>ESTIMATED COST</span>
          <span>
            {quantity} x ${selectedStock?.price.toFixed(2)} = $
            {estimatedCost.toFixed(2)}
          </span>
        </div>
      )}

      <button
        onClick={handleSubmit}
        disabled={
          !selectedStock ||
          quantity < 1 ||
          executeMutation.isPending
        }
        className={`w-full cursor-pointer rounded border px-3 py-1.5 text-xs transition-colors ${
          action === "BUY"
            ? "border-green-400/30 bg-green-500/15 text-green-400 hover:bg-green-500/25"
            : "border-red-400/30 bg-red-500/15 text-red-400 hover:bg-red-500/25"
        } disabled:cursor-not-allowed disabled:opacity-40`}
      >
        {executeMutation.isPending
          ? "EXECUTING..."
          : `${action} ${symbol ? symbol : "???"}`}
      </button>

      {result && (
        <div
          className={`mt-3 rounded border p-2 ${
            result.status === "ACCEPTED"
              ? "border-green-400/30 bg-green-500/10"
              : "border-red-400/30 bg-red-500/10"
          }`}
        >
          <div
            className={
              result.status === "ACCEPTED"
                ? "text-green-400"
                : "text-red-400"
            }
          >
            {result.status}
            {result.errorCode ? ` [${result.errorCode}]` : ""}
          </div>
          {result.message && (
            <div className="mt-0.5 text-foreground">{result.message}</div>
          )}
          {result.status === "ACCEPTED" && (
            <div className="mt-1 space-y-0.5 text-muted-foreground">
              <div className="flex justify-between">
                <span>COST</span>
                <span>${result.totalCost.toFixed(2)}</span>
              </div>
              <div className="flex justify-between">
                <span>NEW CASH</span>
                <span>${result.newCashBalance.toFixed(2)}</span>
              </div>
              <div className="flex justify-between">
                <span>NEW POSITION</span>
                <span>{result.newQuantity} shares</span>
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  )
}
