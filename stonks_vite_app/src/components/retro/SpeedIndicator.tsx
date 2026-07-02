import type { SpeedConfig } from "@/api/useStonksStream"

function formatIntervalMs(ms: number): string {
  if (ms >= 60_000) return `${ms / 60_000}min`
  return `${ms / 1_000}s`
}

const LEVEL_LABELS: Record<string, string> = {
  PAPER_HANDS: "PAPER HANDS",
  MODERATE: "MODERATE",
  HIGH_VOLATILITY: "HIGH VOLATILITY",
  EXTREME: "EXTREME",
  MAXIMUM_OVERDRIVE: "MAXIMUM OVERDRIVE",
}

interface SpeedIndicatorProps {
  config: SpeedConfig | null
}

function TooltipRow({
  label,
  value,
  colorClass,
  tooltip,
}: {
  label: string
  value: string
  colorClass: string
  tooltip: string
}) {
  return (
    <div className="group/row relative cursor-help">
      <span className="text-muted-foreground/60">{label}</span>
      <span className={colorClass}>{value}</span>
      <span className="pointer-events-none absolute bottom-full left-1/2 z-50 mb-1 hidden w-56 -translate-x-1/2 rounded border border-green-500/20 bg-background px-2 py-1.5 text-xs text-muted-foreground group-hover/row:block">
        {tooltip}
      </span>
    </div>
  )
}

export function SpeedIndicator({ config }: SpeedIndicatorProps) {
  if (!config) {
    return (
      <div className="terminal-border p-4 font-mono text-xs">
        <div className="cursor-blink text-muted-foreground">
          AWAITING STREAM...
        </div>
      </div>
    )
  }

  const label = LEVEL_LABELS[config.intensityLevel] ?? config.intensityLevel

  return (
    <div className="terminal-border animate-fade-in p-4 font-mono text-xs">
      <div className="group/header relative mb-2 border-b border-green-500/10 pb-1 text-muted-foreground cursor-help">
        SIMULATION SPEED - {label}
        <span className="pointer-events-none absolute bottom-full left-1/2 z-50 mb-1 hidden w-56 -translate-x-1/2 rounded border border-green-500/20 bg-background px-2 py-1.5 text-xs text-muted-foreground group-hover/header:block">
          Current market intensity level. Higher levels mean faster AI events and more volatility.
        </span>
      </div>
      <div className="space-y-1.5">
        <TooltipRow
          label="MULTIPLIER: "
          value={`${config.volatilityMultiplier}x`}
          colorClass="text-yellow-400"
          tooltip="Price volatility multiplier applied to every tick. Higher = wilder price swings."
        />
        <TooltipRow
          label="TICK: "
          value={formatIntervalMs(config.tickIntervalMs)}
          colorClass="text-green-400"
          tooltip="Interval at which stock prices are recalculated by the COBOL price engine."
        />
        <TooltipRow
          label="AI EVENTS: "
          value={`every ${formatIntervalMs(config.aiEventIntervalMs)}`}
          colorClass="text-red-400/80"
          tooltip="Minimum interval between AI-generated chaos events at the current intensity level."
        />
      </div>
    </div>
  )
}
