import { Link } from "react-router-dom"
import { useStonksStream } from "@/api/useStonksStream"
import { Scanlines } from "@/components/retro/Scanlines"
import { StockTicker } from "@/components/retro/StockTicker"
import { StockChart } from "@/components/retro/StockChart"
import { PortfolioSidebar } from "@/components/retro/PortfolioSidebar"
import { IntensityIndicator } from "@/components/retro/IntensityIndicator"
import { ChaosFeed } from "@/components/retro/ChaosFeed"

export function DashboardPage() {
  const { priceHistory } = useStonksStream()

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
        <IntensityIndicator />
      </header>

      <div className="grid gap-4 lg:grid-cols-[240px_1fr]">
        <aside className="space-y-4">
          <PortfolioSidebar />
        </aside>

        <main className="min-w-0 space-y-4">
          <StockTicker />
          <StockChart priceHistory={priceHistory} />
          <ChaosFeed />
        </main>
      </div>
    </div>
  )
}
