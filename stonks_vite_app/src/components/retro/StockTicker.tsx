import { useState, useMemo } from "react"
import { useGetStocks } from "@/api/hooks"
import { unwrap } from "@/api/hooks"
import type { StockPrice } from "@/api/hooks"

type SortColumn = "symbol" | "name" | "price" | "change"
type SortDir = "asc" | "desc"

const COLUMNS: { key: SortColumn; label: string; className?: string }[] = [
  { key: "symbol", label: "SYMBOL" },
  { key: "name", label: "NAME" },
  { key: "price", label: "PRICE", className: "text-right" },
  { key: "change", label: "CHANGE", className: "text-right" },
]

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

export function StockTicker() {
  const { data: stocks, isLoading } = useGetStocks<StockPrice[]>({
    query: { select: unwrap<StockPrice[]> },
  })

  const [sort, setSort] = useState<SortColumn>("symbol")
  const [dir, setDir] = useState<SortDir>("asc")

  const handleSort = (column: SortColumn) => {
    setSort((prev) => {
      if (prev === column) {
        setDir((d) => (d === "asc" ? "desc" : "asc"))
        return column
      }
      setDir("asc")
      return column
    })
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
        LIVE MARKET DATA — UPDATING EVERY 5s
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
