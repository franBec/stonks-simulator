/* eslint-disable react-refresh/only-export-components */
import { createContext, useContext, useState, type ReactNode } from "react"

export interface GameConfig {
  winThreshold: number
  loseThreshold: number
  initialCash: number
}

interface GameConfigContextValue {
  gameConfig: GameConfig | null
  setGameConfig: (config: GameConfig) => void
}

const GameConfigContext = createContext<GameConfigContextValue | null>(null)

export function GameConfigProvider({ children }: { children: ReactNode }) {
  const [gameConfig, setGameConfig] = useState<GameConfig | null>(null)

  return (
    <GameConfigContext.Provider value={{ gameConfig, setGameConfig }}>
      {children}
    </GameConfigContext.Provider>
  )
}

export function useGameConfig() {
  const ctx = useContext(GameConfigContext)
  if (!ctx) throw new Error("useGameConfig must be used within GameConfigProvider")
  return ctx
}
