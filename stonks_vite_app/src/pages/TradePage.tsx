import { Link } from "react-router-dom"
import { useStonksStream } from "@/api/useStonksStream"
import { Scanlines } from "@/components/retro/Scanlines"
import { PortfolioSidebar } from "@/components/retro/PortfolioSidebar"
import { TradeForm } from "@/components/retro/TradeForm"
import { TradeHistory } from "@/components/retro/TradeHistory"

export function TradePage() {
  useStonksStream()

  return (
    <div className="min-h-svh p-4 font-mono">
      <Scanlines />

      <header className="mb-4 flex items-center justify-between border-b border-green-500/10 pb-3">
        <div>
          <Link to="/" className="transition-opacity hover:opacity-80">
            <h1 className="text-glow text-lg font-bold tracking-wider">
              STONKS SIMULATOR
            </h1>
          </Link>
          <p className="text-xs text-muted-foreground">
            COBOL-powered meme stock trading // v1.0.0
          </p>
        </div>
        <div className="flex items-center gap-3">
          <Link
            to="/app"
            className="rounded border border-green-500/15 px-3 py-1.5 font-mono text-xs text-muted-foreground transition-colors hover:border-green-400/40 hover:text-green-400"
          >
            DASHBOARD
          </Link>
          <Link
            to="/how-it-works"
            className="rounded border border-green-500/15 px-3 py-1.5 font-mono text-xs text-muted-foreground transition-colors hover:border-green-400/40 hover:text-green-400"
          >
            HOW IT WORKS
          </Link>
        </div>
      </header>

      <div className="space-y-4">
        <div className="grid gap-4 lg:grid-cols-2">
          <PortfolioSidebar />
          <TradeForm />
        </div>

        <TradeHistory />
      </div>
    </div>
  )
}
