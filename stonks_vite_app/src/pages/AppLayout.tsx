import { Outlet } from "react-router-dom"
import { useGameState } from "@/hooks/useGameState"
import { GameOverlay } from "@/components/retro/GameOverlay"

export function AppLayout() {
  const { gameOver, won, totalValue, unrealizedPnl, tradeCount, reset, isResetting } =
    useGameState()

  return (
    <>
      {gameOver && (
        <GameOverlay
          won={won}
          totalValue={totalValue}
          unrealizedPnl={unrealizedPnl}
          tradeCount={tradeCount}
          onReset={reset}
          isResetting={isResetting}
        />
      )}
      <Outlet />
      <footer className="py-2 text-center font-mono text-xs text-muted-foreground/40">
        made with React + Vite + Java Spring Boot + COBOL by{" "}
        <a
          href="https://pollito.dev/"
          target="_blank"
          rel="noopener noreferrer"
          className="text-muted-foreground/60 hover:text-foreground transition-colors"
        >
          Pollito &lt;🐤/&gt;
        </a>
      </footer>
    </>
  )
}
