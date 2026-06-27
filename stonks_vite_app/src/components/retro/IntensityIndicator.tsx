import { useGetIntensityLevel } from "@/api/hooks"
import { unwrap } from "@/api/hooks"
import type { IntensityLevel } from "@/api/hooks"

const LEVEL_COLORS: Record<string, string> = {
  PAPER_HANDS: "text-gray-400",
  MODERATE: "text-yellow-400",
  HIGH_VOLATILITY: "text-orange-400",
  EXTREME: "text-red-400",
  MAXIMUM_OVERDRIVE: "text-red-500 animate-pulse",
}

const LEVEL_LABELS: Record<string, string> = {
  PAPER_HANDS: "PAPER HANDS",
  MODERATE: "MODERATE",
  HIGH_VOLATILITY: "HIGH VOLATILITY",
  EXTREME: "EXTREME",
  MAXIMUM_OVERDRIVE: "MAXIMUM OVERDRIVE",
}

export function IntensityIndicator() {
  const { data: level, isLoading } = useGetIntensityLevel<IntensityLevel>({
    query: { select: unwrap<IntensityLevel> },
  })

  if (isLoading || !level) {
    return (
      <div className="terminal-border p-4 font-mono text-xs">
        <div className="cursor-blink text-muted-foreground">
          READING INTENSITY...
        </div>
      </div>
    )
  }

  const color = LEVEL_COLORS[level] ?? "text-green-400"
  const label = LEVEL_LABELS[level] ?? level

  return (
    <div className="terminal-border animate-fade-in p-4 font-mono text-xs">
      <div className="mb-2 border-b border-green-500/10 pb-1 text-muted-foreground">
        INTENSITY LEVEL
      </div>
      <div className={`text-glow-dim text-sm font-bold ${color}`}>{label}</div>
    </div>
  )
}
