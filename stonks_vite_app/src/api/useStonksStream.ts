import { useEffect, useRef, useState } from "react"
import { useQueryClient } from "@tanstack/react-query"
import { getGetStocksQueryKey } from "@/__generated__/api/stocks/stocks"
import { getGetPortfolioQueryKey } from "@/__generated__/api/portfolio/portfolio"
import { getGetChaoticEventsQueryKey } from "@/__generated__/api/chaotic-events/chaotic-events"

export interface PricePoint {
  timestamp: number
  price: number
}

export type PriceHistory = Record<string, PricePoint[]>

const MAX_POINTS = 240
const RECENT_WINDOW = 60

function decimateHistory(points: PricePoint[]): PricePoint[] {
  if (points.length <= MAX_POINTS) return points
  const recent = points.slice(-RECENT_WINDOW)
  const older = points.slice(0, points.length - RECENT_WINDOW)
  const budget = MAX_POINTS - RECENT_WINDOW
  const step = Math.ceil(older.length / budget)
  return [...older.filter((_, i) => i % step === 0), ...recent]
}

const API_BASE = import.meta.env.VITE_BACKEND_URL ?? "http://localhost:8080"
const STREAM_URL = `${API_BASE}/api/stream`

export function useStonksStream() {
  const queryClient = useQueryClient()
  const esRef = useRef<EventSource | null>(null)
  const historyRef = useRef<Map<string, PricePoint[]>>(new Map())
  const [priceHistory, setPriceHistory] = useState<PriceHistory>({})
  const connectRef = useRef<() => void>(() => {})

  useEffect(() => {
    function connect(): EventSource {
      const es = new EventSource(STREAM_URL)

      es.addEventListener("PRICE_TICK", (event: MessageEvent) => {
        queryClient.invalidateQueries({
          queryKey: getGetStocksQueryKey(),
        })

        try {
          const data = JSON.parse(event.data)
          const { symbol, price, timestamp } = data
          const pts = historyRef.current.get(symbol) ?? []
          pts.push({
            timestamp: new Date(timestamp).getTime(),
            price: Number(price),
          })
          const decimated = decimateHistory(pts)
          historyRef.current.set(symbol, decimated)
          setPriceHistory(Object.fromEntries(historyRef.current))
        } catch {
          // ignore malformed event data
        }
      })

      es.addEventListener("TRADE_EXECUTED", () => {
        queryClient.invalidateQueries({
          queryKey: getGetPortfolioQueryKey(),
        })
        queryClient.invalidateQueries({
          queryKey: ["/trades"],
        })
      })

      es.addEventListener("CHAOS_EVENT", () => {
        queryClient.invalidateQueries({
          queryKey: getGetChaoticEventsQueryKey(),
        })
      })

      es.onerror = () => {
        es.close()
        esRef.current = null
        setTimeout(() => {
          esRef.current = connect()
        }, 3000)
      }

      return es
    }

    connectRef.current = () => {
      esRef.current?.close()
      esRef.current = connect()
    }

    esRef.current = connect()

    return () => {
      esRef.current?.close()
    }
  }, [queryClient])

  function reconnect() {
    connectRef.current()
  }

  return { esRef, priceHistory, reconnect }
}
