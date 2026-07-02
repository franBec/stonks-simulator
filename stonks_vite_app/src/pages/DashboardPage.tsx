import { Link } from "react-router-dom"
import { useStonksStream } from "@/api/useStonksStream"
import { Scanlines } from "@/components/retro/Scanlines"
import { StockTicker } from "@/components/retro/StockTicker"
import { StockChart } from "@/components/retro/StockChart"
import { ChaosFeed } from "@/components/retro/ChaosFeed"
import { SpeedIndicator } from "@/components/retro/SpeedIndicator"

export function DashboardPage() {
  const { priceHistory, speedConfig, reconnect } = useStonksStream()

  return (
    <div className="min-h-svh p-4 font-mono">
      <Scanlines />

      <header className="mb-4 flex items-center justify-between border-b border-green-500/10 pb-3">
        <div>
          <Link to="/" className="hover:opacity-80 transition-opacity">
            <h1 className="text-glow text-lg font-bold tracking-wider">
              STONKS SIMULATOR
            </h1>
          </Link>
          <p className="text-xs text-muted-foreground">
            COBOL-powered meme stock trading // v1.0.0
          </p>
        </div>
        <div className="flex items-center gap-3">
          <span className="group relative inline-block">
            <button
              onClick={reconnect}
              className="cursor-pointer select-none rounded border border-green-500/30 px-3 py-1.5 font-mono text-xs text-green-400 transition-colors hover:border-green-400/60 hover:bg-green-500/10 hover:text-green-300 active:translate-y-px"
            >
              RECONNECT
            </button>
            <span className="pointer-events-none absolute top-full right-0 z-50 mt-1 hidden w-56 rounded border border-green-500/20 bg-background px-2 py-1.5 text-xs text-muted-foreground group-hover:block">
              If prices stop updating, click RECONNECT to close the
              current SSE connection and open a fresh stream.
            </span>
          </span>
        </div>
      </header>

      <div className="grid gap-4 lg:grid-cols-[280px_1fr]">
        <aside className="space-y-4">
          <SpeedIndicator config={speedConfig} />
          <ChaosFeed sidebar />
        </aside>

        <main className="min-w-0 space-y-4">
          <StockTicker />
          <StockChart priceHistory={priceHistory} />
        </main>
      </div>
    </div>
  )
}
