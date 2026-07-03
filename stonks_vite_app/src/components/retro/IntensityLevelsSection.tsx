import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Cell,
} from "recharts"
import { Zap, Gauge, Clock } from "lucide-react"

interface IntensityLevel {
  name: string
  class: string
  color: string
  volatilityMultiplier: number
  aiEventIntervalMs: number
  aiEventIntervalLabel: string
  tickIntervalMs: number
  description: string
}

const LEVELS: IntensityLevel[] = [
  {
    name: "Paper Hands",
    class: "PAPER_HANDS",
    color: "#00cc33",
    volatilityMultiplier: 1.0,
    aiEventIntervalMs: 900000,
    aiEventIntervalLabel: "15 min",
    tickIntervalMs: 5000,
    description: "Baseline simulation. Low volatility, rare AI events. Good for learning the market.",
  },
  {
    name: "Moderate",
    class: "MODERATE",
    color: "#00ff41",
    volatilityMultiplier: 2.0,
    aiEventIntervalMs: 300000,
    aiEventIntervalLabel: "5 min",
    tickIntervalMs: 5000,
    description: "Twice the volatility. AI events every 5 minutes. Things get interesting.",
  },
  {
    name: "High Volatility",
    class: "HIGH_VOLATILITY",
    color: "#ffcc00",
    volatilityMultiplier: 5.0,
    aiEventIntervalMs: 120000,
    aiEventIntervalLabel: "2 min",
    tickIntervalMs: 5000,
    description: "5x volatility multiplier. Chaos events every 2 minutes. Strap in.",
  },
  {
    name: "Extreme",
    class: "EXTREME",
    color: "#ff8800",
    volatilityMultiplier: 12.5,
    aiEventIntervalMs: 60000,
    aiEventIntervalLabel: "1 min",
    tickIntervalMs: 5000,
    description: "12.5x volatility. AI mayhem every minute. Only diamond hands survive.",
  },
  {
    name: "Maximum Overdrive",
    class: "MAXIMUM_OVERDRIVE",
    color: "#ff3333",
    volatilityMultiplier: 25.0,
    aiEventIntervalMs: 30000,
    aiEventIntervalLabel: "30 sec",
    tickIntervalMs: 5000,
    description: "25x volatility multiplier. AI chaos every 30 seconds. Absolute carnage.",
  },
]

const barData = LEVELS.map((l) => ({
  name: l.name,
  "Volatility Multiplier": l.volatilityMultiplier,
  color: l.color,
}))

function CustomTooltip({
  active,
  payload,
}: {
  active?: boolean
  payload?: Array<{ value: number }>
}) {
  if (!active || !payload?.length) return null
  return (
    <div className="rounded border border-[#00ff41]/20 bg-[#0d100d] px-3 py-2 font-mono text-xs">
      <span className="text-[#00ff41]">{payload[0].value}x Volatility</span>
    </div>
  )
}

export function IntensityLevelsSection() {
  return (
    <div className="space-y-6">
      <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-5">
        {LEVELS.map((level) => (
          <div
            key={level.class}
            className="terminal-border rounded-lg bg-[#0d100d] p-4 font-mono transition-all hover:border-[#00ff41]/30"
            style={{
              borderLeft: `3px solid ${level.color}`,
            }}
          >
            <div className="flex items-center gap-2 mb-2">
              <div
                className="h-2 w-2 rounded-full"
                style={{
                  backgroundColor: level.color,
                  boxShadow: `0 0 6px ${level.color}`,
                }}
              />
              <span className="text-xs font-bold text-[#00ff41] text-glow-dim">
                {level.name}
              </span>
            </div>

            <div className="space-y-1.5">
              <div className="flex items-center gap-1.5 text-[10px] text-[#008833]">
                <Gauge size={10} />
                <span>Volatility:</span>
                <span className="text-[#00ff41] font-bold">
                  {level.volatilityMultiplier}x
                </span>
              </div>
              <div className="flex items-center gap-1.5 text-[10px] text-[#008833]">
                <Clock size={10} />
                <span>AI Events:</span>
                <span className="text-[#00ff41] font-bold">
                  {level.aiEventIntervalLabel}
                </span>
              </div>
              <div className="flex items-center gap-1.5 text-[10px] text-[#008833]">
                <Zap size={10} />
                <span>Tick:</span>
                <span className="text-[#00ff41] font-bold">
                  {level.tickIntervalMs / 1000}s
                </span>
              </div>
            </div>

            <p className="mt-3 text-[10px] text-[#008833]/70 leading-relaxed">
              {level.description}
            </p>

            <div className="mt-2 text-[9px] text-[#00ff41]/30 font-mono">
              {level.class}
            </div>
          </div>
        ))}
      </div>

      <div className="terminal-border rounded-lg bg-[#0d100d] p-4">
        <h4 className="mb-3 font-mono text-xs text-[#00ff41]/70 uppercase tracking-wider">
          Volatility Multiplier Comparison
        </h4>
        <ResponsiveContainer width="100%" height={200}>
          <BarChart data={barData} margin={{ top: 5, right: 5, bottom: 5, left: 5 }}>
            <CartesianGrid
              strokeDasharray="3 3"
              stroke="rgba(0,255,65,0.06)"
              vertical={false}
            />
            <XAxis
              dataKey="name"
              tick={{ fontSize: 9, fill: "#008833", fontFamily: "JetBrains Mono" }}
              axisLine={{ stroke: "rgba(0,255,65,0.1)" }}
              tickLine={false}
              interval={0}
              angle={-20}
              textAnchor="end"
              height={50}
            />
            <YAxis
              tick={{ fontSize: 9, fill: "#008833", fontFamily: "JetBrains Mono" }}
              axisLine={false}
              tickLine={false}
              tickFormatter={(v: number) => `${v}x`}
            />
            <Tooltip content={<CustomTooltip />} cursor={{ fill: "rgba(0,255,65,0.03)" }} />
            <Bar dataKey="Volatility Multiplier" radius={[2, 2, 0, 0]} maxBarSize={40}>
              {barData.map((entry, i) => (
                <Cell
                  key={i}
                  fill={entry.color}
                  style={{
                    filter: `drop-shadow(0 0 6px ${entry.color}66)`,
                  }}
                />
              ))}
            </Bar>
          </BarChart>
        </ResponsiveContainer>
      </div>
    </div>
  )
}
