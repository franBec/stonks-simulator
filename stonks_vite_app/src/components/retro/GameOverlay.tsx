import { Scanlines } from "@/components/retro/Scanlines"

interface GameOverlayProps {
  won: boolean
  totalValue: number
  unrealizedPnl: number
  tradeCount: number
  onReset: () => void
  isResetting: boolean
}

export function GameOverlay({
  won,
  totalValue,
  unrealizedPnl,
  tradeCount,
  onReset,
  isResetting,
}: GameOverlayProps) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-background/95 backdrop-blur-sm font-mono">
      <Scanlines />
      <div className="terminal-border relative z-10 w-full max-w-md animate-fade-in p-8 text-center">
        <div
          className={`mb-4 text-2xl font-bold tracking-wider ${
            won ? "text-glow" : "text-red-400"
          }`}
        >
          {won ? "MOON ACHIEVED!" : "BANKRUPT!"}
        </div>

        <div
          className={`mb-6 text-sm ${
            won ? "text-green-400" : "text-red-400"
          }`}
        >
          {won
            ? "YOU RETIRED EARLY ON A PRIVATE ISLAND"
            : "THE STONKS GODS ARE DISPLEASED"}
        </div>

        <div className="mb-6 space-y-2 border-y border-green-500/10 py-4 text-xs text-muted-foreground">
          <div className="flex justify-between">
            <span>FINAL VALUE</span>
            <span className="text-foreground">${totalValue.toFixed(2)}</span>
          </div>
          <div className="flex justify-between">
            <span>UNREALIZED P&amp;L</span>
            <span className={unrealizedPnl >= 0 ? "text-green-400" : "text-red-400"}>
              {unrealizedPnl >= 0 ? "+" : ""}$
              {unrealizedPnl.toFixed(2)}
            </span>
          </div>
          <div className="flex justify-between">
            <span>TRADES EXECUTED</span>
            <span className="text-foreground">{tradeCount}</span>
          </div>
        </div>

        <button
          onClick={onReset}
          disabled={isResetting}
          className="w-full cursor-pointer rounded border border-green-400/30 bg-green-500/15 px-4 py-2 text-sm text-green-400 transition-colors hover:bg-green-500/25 disabled:cursor-not-allowed disabled:opacity-40"
        >
          {isResetting ? "RESETTING..." : "PLAY AGAIN"}
        </button>
      </div>
    </div>
  )
}
