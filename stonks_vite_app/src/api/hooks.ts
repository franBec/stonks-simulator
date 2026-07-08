export { useGetStocks } from "@/__generated__/api/stocks/stocks"
export { useExecuteTrade, useGetTradeHistory } from "@/__generated__/api/trades/trades"
export { useGetPortfolio } from "@/__generated__/api/portfolio/portfolio"
export {
  useGetChaoticEvents,
  useTriggerChaoticEvent,
} from "@/__generated__/api/chaotic-events/chaotic-events"
export {
  useGetIntensityLevel,
  useSetIntensityLevel,
} from "@/__generated__/api/intensity-level/intensity-level"

export type {
  StockPrice,
  Portfolio,
  ChaoticEvent,
  IntensityLevel,
  TradeExecutionResult,
  TradeHistoryItem,
  TradeHistoryResponseData,
  GetTradeHistoryParams,
} from "@/__generated__/api/types"

export type { TradeExecutionRequest } from "@/__generated__/api/types"
export type { ChaoticEventTriggerRequest } from "@/__generated__/api/types"
export type { IntensityLevelSetRequest } from "@/__generated__/api/types"

/** Unwrap Orval+API double-wrapped response: response.data.data → T */
export function unwrap<T>(response: unknown): T {
  return (response as { data: { data: T } }).data.data
}
