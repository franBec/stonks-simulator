import { useGetStocks } from "@/api/hooks"
import { unwrap } from "@/api/hooks"
import type { StockPrice } from "@/api/hooks"

function PriceCell({ price, change }: { price: number; change: number }) {
  const up = change >= 0
  return (
    <span className={up ? "text-green-400" : "text-red-400"}>
      ${price.toFixed(2)}
    </span>
  )
}

function ChangeCell({ change, changePercent }: { change: number; changePercent: number }) {
  const up = change >= 0
  const arrow = up ? "▲" : "▼"
  return (
    <span className={up ? "text-green-400" : "text-red-400"}>
      {arrow} {change >= 0 ? "+" : ""}
      {change.toFixed(2)} ({change >= 0 ? "+" : ""}
      {changePercent.toFixed(2)}%)
    </span>
  )
}

export function StockTicker() {
  const { data: stocks, isLoading } = useGetStocks<StockPrice[]>({
    query: { select: unwrap<StockPrice[]> },
  })

  if (isLoading) {
    return (
      <div className="terminal-border animate-fade-in flex items-center justify-center p-8">
        <span className="cursor-blink text-muted-foreground">
          LOADING MARKET DATA...
        </span>
      </div>
    )
  }

  if (!stocks?.length) {
    return (
      <div className="terminal-border p-8 text-center text-muted-foreground">
        NO DATA - MARKET CLOSED?
      </div>
    )
  }

  return (
    <div className="terminal-border animate-fade-in overflow-x-auto">
      <div className="border-b border-green-500/10 px-4 py-2 text-xs text-muted-foreground">
        LIVE MARKET DATA — UPDATING EVERY 5s
      </div>
      <table className="w-full font-mono text-xs sm:text-sm">
        <thead>
          <tr className="border-b border-green-500/10 text-left text-muted-foreground">
            <th className="px-4 py-2 font-normal">SYMBOL</th>
            <th className="px-4 py-2 font-normal">NAME</th>
            <th className="px-4 py-2 text-right font-normal">PRICE</th>
            <th className="px-4 py-2 text-right font-normal">CHANGE</th>
          </tr>
        </thead>
        <tbody>
          {stocks.map((s) => (
            <tr
              key={s.symbol}
              className="border-b border-green-500/5 transition-colors hover:bg-green-500/5"
            >
              <td className="px-4 py-2 font-bold text-foreground">{s.symbol}</td>
              <td className="px-4 py-2 text-muted-foreground">{s.name}</td>
              <td className="px-4 py-2 text-right">
                <PriceCell price={s.price} change={s.change} />
              </td>
              <td className="px-4 py-2 text-right font-mono">
                <ChangeCell change={s.change} changePercent={s.changePercent} />
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
