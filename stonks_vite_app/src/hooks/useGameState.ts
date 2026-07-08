import { useState, useMemo } from "react"
import { useQueryClient } from "@tanstack/react-query"
import { useGetPortfolio, useGetTradeHistory } from "@/api/hooks"
import { unwrap } from "@/api/hooks"
import type { Portfolio, TradeHistoryResponseData } from "@/api/hooks"
import { getGetPortfolioQueryKey } from "@/__generated__/api/portfolio/portfolio"
import { getGetTradeHistoryQueryKey } from "@/__generated__/api/trades/trades"

const WIN_THRESHOLD = 100000
const LOSE_THRESHOLD = 10
const API_BASE = import.meta.env.VITE_BACKEND_URL ?? "http://localhost:8080"

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

  const { data: portfolio } = useGetPortfolio<Portfolio>({
    query: { select: unwrap<Portfolio>, refetchInterval: 5000 },
  })

  const { data: tradeHistory } = useGetTradeHistory<TradeHistoryResponseData>(
    { page: 0, size: 1 },
    { query: { select: unwrap<TradeHistoryResponseData> } },
  )

  const [dismissed, setDismissed] = useState(false)
  const [isResetting, setIsResetting] = useState(false)

  const totalValue = useMemo(
    () =>
      portfolio
        ? portfolio.cashBalance +
          portfolio.positions.reduce((sum, p) => sum + p.marketValue, 0)
        : 0,
    [portfolio],
  )

  const gameOver = useMemo(() => {
    if (dismissed) return false
    if (totalValue >= WIN_THRESHOLD) return true
    if (totalValue > 0 && totalValue <= LOSE_THRESHOLD) return true
    return false
  }, [totalValue, dismissed])

  const won = useMemo(() => totalValue >= WIN_THRESHOLD, [totalValue])

  const reset = () => {
    setIsResetting(true)
    fetch(`${API_BASE}/api/portfolio/reset`, { method: "POST" })
      .then(() => {
        setDismissed(true)
        queryClient.invalidateQueries({ queryKey: getGetPortfolioQueryKey() })
        queryClient.invalidateQueries({
          queryKey: getGetTradeHistoryQueryKey(),
        })
        setTimeout(() => setDismissed(false), 500)
      })
      .finally(() => setIsResetting(false))
  }

  return {
    gameOver,
    won,
    totalValue,
    unrealizedPnl: portfolio?.unrealizedPnl ?? 0,
    tradeCount: tradeHistory?.totalElements ?? 0,
    reset,
    isResetting,
  }
}
