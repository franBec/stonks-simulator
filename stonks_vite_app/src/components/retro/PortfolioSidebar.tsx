import { useGetPortfolio } from "@/api/hooks"
import { unwrap } from "@/api/hooks"
import type { Portfolio } from "@/api/hooks"

export function PortfolioSidebar() {
  const { data: portfolio, isLoading } = useGetPortfolio<Portfolio>({
    query: { select: unwrap<Portfolio>, refetchInterval: 5000 },
  })

  if (isLoading) {
    return (
      <div className="terminal-border p-4">
        <div className="cursor-blink text-xs text-muted-foreground">
          LOADING PORTFOLIO...
        </div>
      </div>
    )
  }

  if (!portfolio) {
    return (
      <div className="terminal-border p-4 text-xs text-muted-foreground">
        NO PORTFOLIO DATA
      </div>
    )
  }

  const activePositions = portfolio.positions.filter((p) => p.quantity > 0)
  const totalValue =
    portfolio.cashBalance +
    activePositions.reduce((sum, p) => sum + p.marketValue, 0)
  const pnlColor =
    portfolio.unrealizedPnl >= 0 ? "text-green-400" : "text-red-400"

  return (
    <div className="terminal-border animate-fade-in p-4 font-mono text-xs">
      <div className="mb-3 border-b border-green-500/10 pb-2 text-muted-foreground">
        PORTFOLIO
      </div>

      <div className="space-y-1">
        <div className="flex justify-between">
          <span className="text-muted-foreground">CASH</span>
          <span>${portfolio.cashBalance.toFixed(2)}</span>
        </div>
        <div className="flex justify-between">
          <span className="text-muted-foreground">TOTAL VALUE</span>
          <span>${totalValue.toFixed(2)}</span>
        </div>
        <div className="flex justify-between">
          <span className="text-muted-foreground">UNREALIZED P&amp;L</span>
          <span className={pnlColor}>
            {portfolio.unrealizedPnl >= 0 ? "+" : ""}$
            {portfolio.unrealizedPnl.toFixed(2)}
          </span>
        </div>
      </div>

      {activePositions.length > 0 && (
        <>
          <div className="mb-2 mt-4 border-b border-green-500/10 pb-1 text-muted-foreground">
            POSITIONS
          </div>
          <div className="space-y-1">
            {activePositions.map((p) => {
              const pnlColor =
                p.unrealizedPnl >= 0 ? "text-green-400" : "text-red-400"
              return (
                <div key={p.symbol} className="flex justify-between">
                  <span>
                    <span className="text-foreground">{p.symbol}</span>
                    <span className="text-muted-foreground">
                      {" "}
                      x{p.quantity}
                    </span>
                  </span>
                  <span className={pnlColor}>
                    {p.unrealizedPnl >= 0 ? "+" : ""}$
                    {p.unrealizedPnl.toFixed(2)}
                  </span>
                </div>
              )
            })}
          </div>
        </>
      )}
    </div>
  )
}
