import { useState, useMemo } from "react"
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from "recharts"
import { HelpCircle } from "lucide-react"
import type { PriceHistory } from "@/api/useStonksStream"

const ALL_SYMBOLS = [
  "COBL",
  "GMEE",
  "DOGE",
  "TEND",
  "FOMO",
  "PAPR",
  "YOLO",
  "MEME",
  "BUGS",
  "JAVA",
] as const

const SYMBOL_COLORS: Record<string, string> = {
  COBL: "#00ff41",
  GMEE: "#ff3333",
  DOGE: "#ff9900",
  TEND: "#3399ff",
  FOMO: "#cc33ff",
  PAPR: "#00cc33",
  YOLO: "#ff66aa",
  MEME: "#00ddff",
  BUGS: "#ffff33",
  JAVA: "#ff6633",
}

function formatTime(timestamp: number): string {
  return new Date(timestamp).toLocaleTimeString([], {
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
  })
}

function formatPrice(value: number): string {
  return `$${value.toFixed(2)}`
}

function mergeHistory(
  history: PriceHistory,
  selected: Set<string>,
): Array<Record<string, number | string | null>> {
  const timeSet = new Set<number>()
  const selectedSymbols = ALL_SYMBOLS.filter((s) => selected.has(s))
  for (const s of selectedSymbols) {
    for (const pt of history[s] ?? []) {
      timeSet.add(pt.timestamp)
    }
  }
  const times = Array.from(timeSet).sort((a, b) => a - b)

  const lookup: Record<string, Record<number, number>> = {}
  for (const s of selectedSymbols) {
    lookup[s] = {}
    for (const pt of history[s] ?? []) {
      lookup[s][pt.timestamp] = pt.price
    }
  }

  return times.map((t) => {
    const entry: Record<string, number | string | null> = { timestamp: t }
    for (const s of selectedSymbols) {
      entry[s] = lookup[s]?.[t] ?? null
    }
    return entry
  })
}

interface StockChartProps {
  priceHistory: PriceHistory
}

export function StockChart({ priceHistory }: StockChartProps) {
  const [selected, setSelected] = useState<Set<string>>(
    new Set(ALL_SYMBOLS),
  )

  const toggleSymbol = (symbol: string) => {
    setSelected((prev) => {
      const next = new Set(prev)
      if (next.has(symbol)) {
        next.delete(symbol)
      } else {
        next.add(symbol)
      }
      return next
    })
  }

  const allSelected = selected.size === ALL_SYMBOLS.length

  const toggleAll = () => {
    if (allSelected) {
      setSelected(new Set())
    } else {
      setSelected(new Set(ALL_SYMBOLS))
    }
  }

  const mergedData = useMemo(
    () => mergeHistory(priceHistory, selected),
    [priceHistory, selected],
  )

  const hasData = Object.values(priceHistory).some(
    (pts) => pts.length > 0,
  )

  return (
    <div className="terminal-border animate-fade-in">
      <div className="flex items-center justify-between border-b border-green-500/10 px-4 py-2">
        <span className="text-xs text-muted-foreground">
          LIVE PRICE CHART
        </span>
        <span className="group relative cursor-help">
          <HelpCircle className="h-3.5 w-3.5 text-muted-foreground" />
          <span className="pointer-events-none absolute bottom-full right-0 z-50 mb-1 hidden w-56 rounded border border-green-500/20 bg-background px-2 py-1.5 text-xs text-muted-foreground group-hover:block">
            Historical data is only saved in the web app. Backend
            time-series storage is out of scope for now.
          </span>
        </span>
      </div>

      <div className="flex flex-wrap gap-1.5 px-4 py-2 border-b border-green-500/5">
        <button
          onClick={toggleAll}
          className={`px-2 py-0.5 text-xs border font-mono transition-colors cursor-pointer ${
            allSelected
              ? "border-green-500/40 bg-green-500/10 text-foreground"
              : "border-green-500/10 bg-transparent text-muted-foreground hover:border-green-500/25"
          }`}
        >
          ALL
        </button>
        <span className="text-muted-foreground/30 text-xs self-center">|</span>
        {ALL_SYMBOLS.map((symbol) => {
          const isSelected = selected.has(symbol)
          return (
            <button
              key={symbol}
              onClick={() => toggleSymbol(symbol)}
              className={`px-2 py-0.5 text-xs border font-mono transition-colors cursor-pointer ${
                isSelected
                  ? "border-green-500/40 bg-green-500/10 text-foreground"
                  : "border-green-500/10 bg-transparent text-muted-foreground hover:border-green-500/25"
              }`}
              style={
                isSelected
                  ? { color: SYMBOL_COLORS[symbol] }
                  : undefined
              }
            >
              {symbol}
            </button>
          )
        })}
      </div>

      <div className="px-4 py-3">
        {!hasData ? (
          <div className="flex h-64 items-center justify-center">
            <span className="cursor-blink text-sm text-muted-foreground">
              ACCUMULATING MARKET DATA...
            </span>
          </div>
        ) : (
          <ResponsiveContainer width="100%" height={320}>
            <LineChart
              data={mergedData}
              margin={{ top: 8, right: 8, left: 8, bottom: 0 }}
            >
              <CartesianGrid
                strokeDasharray="3 3"
                stroke="#00ff4115"
              />
              <XAxis
                dataKey="timestamp"
                tickFormatter={formatTime}
                stroke="#008833"
                tick={{ fill: "#008833", fontSize: 11 }}
                tickLine={{ stroke: "#00ff4122" }}
                minTickGap={40}
              />
              <YAxis
                tickFormatter={formatPrice}
                stroke="#008833"
                tick={{ fill: "#008833", fontSize: 11 }}
                tickLine={{ stroke: "#00ff4122" }}
                width={70}
              />
              <Tooltip
                contentStyle={{
                  backgroundColor: "#0d100d",
                  border: "1px solid rgba(0,255,65,0.2)",
                  borderRadius: "0.125rem",
                  fontSize: "12px",
                  fontFamily: "JetBrains Mono, monospace",
                  color: "#00ff41",
                }}
                labelFormatter={formatTime}
                formatter={(value: number) => [
                  `$${value.toFixed(2)}`,
                ]}
              />
              <Legend
                wrapperStyle={{
                  fontSize: "11px",
                  fontFamily: "JetBrains Mono, monospace",
                  color: "#008833",
                }}
              />
              {ALL_SYMBOLS.filter((s) => selected.has(s)).map(
                (symbol) => (
                  <Line
                    key={symbol}
                    type="monotone"
                    dataKey={symbol}
                    stroke={SYMBOL_COLORS[symbol]}
                    strokeWidth={1.5}
                    dot={false}
                    connectNulls
                    isAnimationActive={false}
                  />
                ),
              )}
            </LineChart>
          </ResponsiveContainer>
        )}
      </div>
    </div>
  )
}
