import { useGetChaoticEvents } from "@/api/hooks"
import { unwrap } from "@/api/hooks"
import type { ChaoticEvent } from "@/api/hooks"

const SEVERITY_COLORS: Record<string, string> = {
  LOW: "text-yellow-400",
  MEDIUM: "text-orange-400",
  HIGH: "text-red-400",
  CRITICAL: "text-red-500",
}

function formatEventTime(iso: string): string {
  return new Date(iso).toLocaleTimeString([], {
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
  })
}

interface ChaosFeedProps {
  sidebar?: boolean
}

export function ChaosFeed({ sidebar = false }: ChaosFeedProps) {
  const { data: events, isLoading } = useGetChaoticEvents<ChaoticEvent[]>({
    query: { select: unwrap<ChaoticEvent[]> },
  })

  if (isLoading) {
    return (
      <div className="terminal-border p-4 font-mono text-xs">
        <div className="cursor-blink text-muted-foreground">
          MONITORING CHAOS ENGINE...
        </div>
      </div>
    )
  }

  return (
    <div className="terminal-border animate-fade-in p-4 font-mono text-xs">
      <div className="mb-3 border-b border-green-500/10 pb-1 text-muted-foreground">
        CHAOS FEED
      </div>

      {!events?.length ? (
        <div className="text-muted-foreground">
          NO ACTIVE CHAOS EVENTS — MARKET CALM
        </div>
      ) : (
        <div
          className={
            sidebar
              ? "max-h-[calc(100svh-13rem)] space-y-3 overflow-y-auto"
              : "max-h-64 space-y-3 overflow-y-auto"
          }
        >
          {events.map((e) => (
            <div
              key={e.eventId}
              className="animate-fade-in border-b border-green-500/5 pb-2 last:border-0"
            >
              <div className="flex items-center gap-2">
                <span className={SEVERITY_COLORS[e.severity] ?? ""}>
                  [{e.severity}]
                </span>
                <span className="font-bold text-foreground">{e.title}</span>
              </div>
              <div className="mt-0.5 text-muted-foreground/60 text-[10px]">
                {formatEventTime(e.startedAt)}
              </div>
              {e.targetSymbol && (
                <div className="mt-0.5 text-muted-foreground">
                  TARGET: {e.targetSymbol}
                  {e.priceEffect != null && (
                    <span
                      className={
                        e.priceEffect >= 1
                          ? "ml-1 text-green-400"
                          : "ml-1 text-red-400"
                      }
                    >
                      ({((e.priceEffect - 1) * 100).toFixed(0)}%)
                    </span>
                  )}
                </div>
              )}
              <div className="mt-0.5 text-muted-foreground">
                {e.description}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
