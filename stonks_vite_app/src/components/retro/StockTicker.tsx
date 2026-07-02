import { useState, useMemo } from "react"
import { useGetStocks } from "@/api/hooks"
import { unwrap } from "@/api/hooks"
import type { StockPrice } from "@/api/hooks"

type SortColumn = "symbol" | "name" | "price" | "change" | "trend" | "trendBias"
type SortDir = "asc" | "desc"

const COLUMNS: { key: SortColumn; label: string; className?: string }[] = [
  { key: "symbol", label: "SYMBOL" },
  { key: "name", label: "NAME" },
  { key: "trend", label: "TREND" },
  { key: "trendBias", label: "BIAS/TICK", className: "text-right" },
  { key: "price", label: "PRICE", className: "text-right" },
  { key: "change", label: "CHANGE", className: "text-right" },
]

const TREND_BIAS: Record<string, string> = {
  BULL: "+0.3%",
  BEAR: "-0.3%",
  MOON: "+1.0%",
  CHAOS: "±2.0%",
  CRASH: "-5.0%",
}

const TREND_DESC: Record<string, string> = {
  BULL: "Steady upward drift — price gains ~0.3% per tick",
  BEAR: "Steady downward drift — price loses ~0.3% per tick",
  MOON: "Strong upward momentum — price surges ~1.0% per tick",
  CHAOS: "Unpredictable swings — random bias between -2% and +2% per tick",
  CRASH: "Heavy downward momentum — price drops ~5.0% per tick",
}

function SortArrow({ column, sort, dir }: { column: SortColumn; sort: SortColumn; dir: SortDir }) {
  if (column !== sort) return <span className="ml-1 text-green-500/20">↕</span>
  return <span className="ml-1">{dir === "asc" ? "▲" : "▼"}</span>
}

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

function TrendCell({ trend }: { trend: string }) {
  const desc = TREND_DESC[trend] ?? "Unknown trend type"
  return (
    <span className="group relative cursor-help text-muted-foreground">
      {trend}
      <span className="pointer-events-none absolute bottom-full left-1/2 z-50 mb-1 hidden w-56 -translate-x-1/2 rounded border border-green-500/20 bg-background px-2 py-1.5 text-xs text-muted-foreground group-hover:block">
        {desc}
      </span>
    </span>
  )
}

function BiasCell({ bias }: { bias: string }) {
  const up = bias.startsWith("+")
  const cls = up ? "text-green-400" : bias.startsWith("-") ? "text-red-400" : "text-yellow-400"
  return <span className={cls}>{bias}</span>
}

export function StockTicker() {
  const { data: stocks, isLoading } = useGetStocks<StockPrice[]>({
    query: { select: unwrap<StockPrice[]> },
  })

  const [sort, setSort] = useState<SortColumn>("symbol")
  const [dir, setDir] = useState<SortDir>("asc")

  const handleSort = (column: SortColumn) => {
    if (column === sort) {
      setDir((prev) => (prev === "asc" ? "desc" : "asc"))
    } else {
      setDir("asc")
      setSort(column)
    }
  }

  const sorted = useMemo(() => {
    if (!stocks) return []
    const s = [...stocks].sort((a, b) => {
      let cmp = 0
      switch (sort) {
        case "symbol":
          cmp = a.symbol.localeCompare(b.symbol)
          break
        case "name":
          cmp = a.name.localeCompare(b.name)
          break
        case "trend":
          cmp = a.trend.localeCompare(b.trend)
          break
        case "trendBias":
          cmp = (TREND_BIAS[a.trend] ?? "").localeCompare(TREND_BIAS[b.trend] ?? "")
          break
        case "price":
          cmp = a.price - b.price
          break
        case "change":
          cmp = a.change - b.change
          break
      }
      return dir === "asc" ? cmp : -cmp
    })
    return s
  }, [stocks, sort, dir])

  if (isLoading) {
    return (
      <div className="terminal-border animate-fade-in flex items-center justify-center p-8">
        <span className="cursor-blink text-muted-foreground">
          LOADING MARKET DATA...
        </span>
      </div>
    )
  }

  if (!sorted.length) {
    return (
      <div className="terminal-border p-8 text-center text-muted-foreground">
        NO DATA - MARKET CLOSED?
      </div>
    )
  }

  return (
    <div className="terminal-border animate-fade-in overflow-x-auto">
      <div className="border-b border-green-500/10 px-4 py-2 text-xs text-muted-foreground">
        LIVE MARKET DATA
      </div>
      <table className="w-full font-mono text-xs sm:text-sm">
        <thead>
          <tr className="border-b border-green-500/10 text-left text-muted-foreground">
            {COLUMNS.map((col) => (
              <th
                key={col.key}
                className={`cursor-pointer select-none px-4 py-2 font-normal transition-colors hover:text-foreground ${col.className ?? ""}`}
                onClick={() => handleSort(col.key)}
              >
                {col.label}
                <SortArrow column={col.key} sort={sort} dir={dir} />
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {sorted.map((s) => (
            <tr
              key={s.symbol}
              className="border-b border-green-500/5 transition-colors hover:bg-green-500/5"
            >
              <td className="px-4 py-2 font-bold text-foreground">{s.symbol}</td>
              <td className="px-4 py-2 text-muted-foreground">{s.name}</td>
              <td className="px-4 py-2">
                <TrendCell trend={s.trend} />
              </td>
              <td className="px-4 py-2 text-right font-mono">
                <BiasCell bias={TREND_BIAS[s.trend] ?? "?"} />
              </td>
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
