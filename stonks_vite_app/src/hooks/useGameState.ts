import { useState, useEffect, startTransition } from "react"
import { useQueryClient } from "@tanstack/react-query"
import { useGetPortfolio, useGetTradeHistory, useResetGame } from "@/api/hooks"
import { unwrap } from "@/api/hooks"
import type { Portfolio, TradeHistoryResponseData } from "@/api/hooks"
import { getGetPortfolioQueryKey } from "@/__generated__/api/portfolio/portfolio"
import { getGetTradeHistoryQueryKey } from "@/__generated__/api/trades/trades"
import { useGameConfig } from "@/hooks/useGameConfig"

const DEFAULT_WIN_THRESHOLD = 100000
const DEFAULT_LOSE_THRESHOLD = 1000

export interface GameState {
  gameOver: boolean
  won: boolean
  totalValue: number
  unrealizedPnl: number
  tradeCount: number
  reset: () => void
  isResetting: boolean
}

export function useGameState(): GameState {
  const queryClient = useQueryClient()
  const { gameConfig } = useGameConfig()

  const winThreshold = gameConfig?.winThreshold ?? DEFAULT_WIN_THRESHOLD
  const loseThreshold = gameConfig?.loseThreshold ?? DEFAULT_LOSE_THRESHOLD

  const { data: portfolio } = useGetPortfolio<Portfolio>({
    query: { select: unwrap<Portfolio>, refetchInterval: 5000 },
  })

  const { data: tradeHistory } = useGetTradeHistory<TradeHistoryResponseData>(
    { page: 0, size: 1 },
    { query: { select: unwrap<TradeHistoryResponseData> } },
  )

  const [gameEnded, setGameEnded] = useState(false)
  const [won, setWon] = useState(false)
  const [isResetting, setIsResetting] = useState(false)
  const { mutateAsync: resetGameMutation } = useResetGame()
  const [snapshot, setSnapshot] = useState<{
    totalValue: number
    unrealizedPnl: number
    tradeCount: number
  } | null>(null)

  const totalValue =
    portfolio
      ? portfolio.cashBalance +
        portfolio.positions.reduce((sum, p) => sum + p.marketValue, 0)
      : 0

  useEffect(() => {
    startTransition(() => {
      if (gameEnded) return
      if (totalValue >= winThreshold) {
        setGameEnded(true)
        setWon(true)
        setSnapshot({
          totalValue,
          unrealizedPnl: portfolio?.unrealizedPnl ?? 0,
          tradeCount: tradeHistory?.totalElements ?? 0,
        })
      } else if (totalValue > 0 && totalValue <= loseThreshold) {
        setGameEnded(true)
        setWon(false)
        setSnapshot({
          totalValue,
          unrealizedPnl: portfolio?.unrealizedPnl ?? 0,
          tradeCount: tradeHistory?.totalElements ?? 0,
        })
      }
    })
  }, [totalValue, gameEnded, winThreshold, loseThreshold])

  const reset = () => {
    setIsResetting(true)
    resetGameMutation()
      .then(() => queryClient.invalidateQueries({ queryKey: getGetPortfolioQueryKey() }))
      .then(() => queryClient.invalidateQueries({ queryKey: getGetTradeHistoryQueryKey() }))
      .then(() => {
        startTransition(() => {
          setGameEnded(false)
          setWon(false)
        })
      })
      .finally(() => setIsResetting(false))
  }

  return {
    gameOver: gameEnded,
    won,
    totalValue: snapshot?.totalValue ?? totalValue,
    unrealizedPnl: snapshot?.unrealizedPnl ?? portfolio?.unrealizedPnl ?? 0,
    tradeCount: snapshot?.tradeCount ?? tradeHistory?.totalElements ?? 0,
    reset,
    isResetting,
  }
}
