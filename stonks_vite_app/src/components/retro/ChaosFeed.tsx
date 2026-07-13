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
      <div className="group/header relative mb-3 border-b border-green-500/10 pb-1 text-muted-foreground cursor-help">
        CHAOS FEED
        <span className="pointer-events-none absolute bottom-full left-1/2 z-50 mb-1 hidden w-56 -translate-x-1/2 rounded border border-green-500/20 bg-background px-2 py-1.5 text-xs text-muted-foreground group-hover/header:block">
          AI-generated chaotic events based on real-world news. Events cause
          price swings in affected stocks, from market dumps to moon shots.
        </span>
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
              {e.sourceUrl && (
                <div className="mt-0.5 text-[10px]">
                  <a
                    href={e.sourceUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="text-green-400/70 hover:text-green-400 hover:underline transition-colors"
                  >
                    [SOURCE]
                  </a>
                  {e.sourceHeadline && (
                    <span className="ml-1 text-muted-foreground/50">
                      {e.sourceHeadline}
                    </span>
                  )}
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
