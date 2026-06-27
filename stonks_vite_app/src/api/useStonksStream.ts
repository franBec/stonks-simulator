import { useEffect, useRef } from "react"
import { useQueryClient } from "@tanstack/react-query"
import { getGetStocksQueryKey } from "@/__generated__/api/stocks/stocks"
import { getGetPortfolioQueryKey } from "@/__generated__/api/portfolio/portfolio"
import { getGetChaoticEventsQueryKey } from "@/__generated__/api/chaotic-events/chaotic-events"

const API_BASE = import.meta.env.VITE_BACKEND_URL ?? "http://localhost:8080"
const STREAM_URL = `${API_BASE}/api/stream`

export function useStonksStream() {
  const queryClient = useQueryClient()
  const esRef = useRef<EventSource | null>(null)

  useEffect(() => {
    let es = new EventSource(STREAM_URL)

    es.addEventListener("PRICE_TICK", () => {
      queryClient.invalidateQueries({
        queryKey: getGetStocksQueryKey(),
      })
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

  return esRef
}
