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

const API_BASE = import.meta.env.VITE_BACKEND_URL ?? "http://localhost:8080"
const STREAM_URL = `${API_BASE}/api/stream`

export function useStonksStream() {
  const queryClient = useQueryClient()
  const esRef = useRef<EventSource | null>(null)
  const historyRef = useRef<Map<string, PricePoint[]>>(new Map())
  const [priceHistory, setPriceHistory] = useState<PriceHistory>({})

  useEffect(() => {
    let es = new EventSource(STREAM_URL)

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
        if (pts.length > MAX_POINTS) {
          pts.splice(0, pts.length - MAX_POINTS)
        }
        historyRef.current.set(symbol, pts)
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
      setTimeout(() => {
        es = new EventSource(STREAM_URL)
        esRef.current = es
      }, 3000)
    }

    esRef.current = es

    return () => {
      es.close()
    }
  }, [queryClient])

  return { esRef, priceHistory }
}
