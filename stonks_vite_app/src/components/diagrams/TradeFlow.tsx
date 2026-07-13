import { type Node, type Edge, MarkerType } from "@xyflow/react"
import { DiagramWrapper } from "./DiagramWrapper"
import {
  ServiceNode,
  CobolNode,
  SseEdgeNode,
  ConnectorNode,
} from "./CustomNodes"
import { AnimatedEdge } from "./AnimatedEdge"

const nodeTypes = {
  service: ServiceNode,
  cobol: CobolNode,
  sseEdge: SseEdgeNode,
  connector: ConnectorNode,
}

const edgeTypes = {
  animated: AnimatedEdge,
}

const nodes: Node[] = [
  {
    id: "trader",
    type: "service",
    position: { x: 40, y: 240 },
    data: {
      label: "Trader",
      typeTag: "UI",
      description: "Trade form: BUY/SELL with symbol + quantity",
      detail: "TradePage → POST /api/trades",
      width: 200,
    },
  },
  {
    id: "trade-service",
    type: "service",
    position: { x: 340, y: 60 },
    data: {
      label: "TradeService",
      typeTag: "Service",
      description: "Validates input, fetches current price, delegates to COBOL",
      detail: "Updates portfolio state on success",
      width: 220,
    },
  },
  {
    id: "cobol-portfolio",
    type: "cobol",
    position: { x: 660, y: 60 },
    data: {
      label: "COBOL portfolio-mgr",
      typeTag: "Process",
      description: "Validates trade: funds, shares, symbol, action",
      detail: "303 lines — 9 validation rules + fee calc",
      width: 220,
    },
  },
  {
    id: "db-persist",
    type: "service",
    position: { x: 980, y: 60 },
    data: {
      label: "DB Persistence",
      typeTag: "Storage",
      description: "Saves trade history + updates portfolio state",
      detail: "trade_history + portfolio_position tables",
      width: 220,
    },
  },
  {
    id: "broadcast",
    type: "sseEdge",
    position: { x: 340, y: 400 },
    data: {
      label: "BroadcastSseService",
      typeTag: "Streaming",
      description: "SSE TRADE_EXECUTED with trade result",
      detail: "TradeExecutedEvent → all SSE clients",
      width: 220,
    },
  },
  {
    id: "portfolio-ui",
    type: "service",
    position: { x: 660, y: 400 },
    data: {
      label: "React Frontend",
      typeTag: "UI Update",
      description: "Updates portfolio sidebar + trade history log",
      detail: "Invalidates /portfolio + /trades caches",
      width: 220,
    },
  },
  {
    id: "game-check",
    type: "connector",
    position: { x: 980, y: 400 },
    data: {
      label: "Game State Check",
      typeTag: "Guard",
      description: "Checks win/lose thresholds after trade",
      detail: "Portfolio value vs win/lose thresholds",
      width: 200,
    },
  },
]

const edges: Edge[] = [
  {
    id: "trader-submit",
    source: "trader",
    target: "trade-service",
    sourceHandle: "right-source",
    targetHandle: "left",
    type: "animated",
    animated: true,
    markerEnd: {
      type: MarkerType.ArrowClosed,
      color: "#00ff41",
      width: 12,
      height: 12,
    },
    data: { label: "POST /api/trades" },
    style: {
      stroke: "#00ff41",
      strokeWidth: 2,
      strokeOpacity: 0.7,
      filter: "drop-shadow(0 0 6px rgba(0,255,65,0.4))",
    },
  },
  {
    id: "delegate-cobol",
    source: "trade-service",
    target: "cobol-portfolio",
    sourceHandle: "right-source",
    targetHandle: "left",
    type: "animated",
    animated: true,
    markerEnd: {
      type: MarkerType.ArrowClosed,
      color: "#00ff41",
      width: 12,
      height: 12,
    },
    data: { label: "execute('portfolio-mgr')" },
    style: { stroke: "#00ff41", strokeWidth: 2, strokeOpacity: 0.7 },
  },
  {
    id: "cobol-response",
    source: "cobol-portfolio",
    target: "trade-service",
    sourceHandle: "bottom-source",
    targetHandle: "bottom",
    type: "animated",
    animated: true,
    markerEnd: {
      type: MarkerType.ArrowClosed,
      color: "#00ff41",
      width: 12,
      height: 12,
    },
    data: { label: "TradeResult", arcHeight: 100 },
    style: {
      stroke: "#00ff41",
      strokeWidth: 1.5,
      strokeOpacity: 0.5,
      strokeDasharray: "4 4",
    },
  },
  {
    id: "persist-trade",
    source: "trade-service",
    target: "db-persist",
    sourceHandle: "right-source",
    targetHandle: "left",
    type: "animated",
    animated: true,
    markerEnd: {
      type: MarkerType.ArrowClosed,
      color: "#00ff41",
      width: 12,
      height: 12,
    },
    data: { label: "save trade + portfolio" },
    style: {
      stroke: "#00ff41",
      strokeWidth: 1,
      strokeOpacity: 0.3,
      strokeDasharray: "8 4",
    },
  },
  {
    id: "broadcast-trade",
    source: "trade-service",
    target: "broadcast",
    sourceHandle: "bottom-source",
    targetHandle: "top",
    type: "animated",
    animated: true,
    markerEnd: {
      type: MarkerType.ArrowClosed,
      color: "#00ff41",
      width: 12,
      height: 12,
    },
    data: { label: "TradeExecutedEvent" },
    style: { stroke: "#00ff41", strokeWidth: 2, strokeOpacity: 0.6 },
  },
  {
    id: "sse-client",
    source: "broadcast",
    target: "portfolio-ui",
    sourceHandle: "right-source",
    targetHandle: "left",
    type: "animated",
    animated: true,
    markerEnd: {
      type: MarkerType.ArrowClosed,
      color: "#00ff41",
      width: 12,
      height: 12,
    },
    data: { label: "SSE: TRADE_EXECUTED" },
    style: {
      stroke: "#00ff41",
      strokeWidth: 2.5,
      strokeOpacity: 0.8,
      filter: "drop-shadow(0 0 8px rgba(0,255,65,0.5))",
    },
  },
  {
    id: "check-game",
    source: "trade-service",
    target: "game-check",
    sourceHandle: "right-source",
    targetHandle: "left",
    type: "animated",
    animated: true,
    markerEnd: {
      type: MarkerType.ArrowClosed,
      color: "#ff9933",
      width: 12,
      height: 12,
    },
    data: { label: "evaluate win/loss" },
    style: {
      stroke: "#ff9933",
      strokeWidth: 1.5,
      strokeOpacity: 0.5,
      strokeDasharray: "4 4",
    },
  },
]

export function TradeFlow() {
  return (
    <DiagramWrapper
      nodes={nodes}
      edges={edges}
      nodeTypes={nodeTypes}
      edgeTypes={edgeTypes}
      height={620}
    />
  )
}
