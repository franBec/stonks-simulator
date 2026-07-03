import { type Node, type Edge, MarkerType } from "@xyflow/react"
import { DiagramWrapper } from "./DiagramWrapper"
import { SchedulerNode, ServiceNode, CobolNode, SseEdgeNode } from "./CustomNodes"
import { AnimatedEdge } from "./AnimatedEdge"

const nodeTypes = {
  scheduler: SchedulerNode,
  service: ServiceNode,
  cobol: CobolNode,
  sseEdge: SseEdgeNode,
}

const edgeTypes = {
  animated: AnimatedEdge,
}

const nodes: Node[] = [
  {
    id: "scheduler",
    type: "scheduler",
    position: { x: 20, y: 180 },
    data: {
      label: "Scheduler",
      typeTag: "Trigger",
      description: "Every 5 seconds",
      detail: "StockPriceTickScheduler",
    },
  },
  {
    id: "stock-service",
    type: "service",
    position: { x: 230, y: 60 },
    data: {
      label: "StockService",
      typeTag: "Service",
      description: "Iterates 10 stocks, applies volatility multiplier, delegates to COBOL",
      detail: "ConcurrentHashMap<String, StockPrice>",
    },
  },
  {
    id: "cobol-price",
    type: "cobol",
    position: { x: 480, y: 60 },
    data: {
      label: "COBOL price-engine",
      typeTag: "Process",
      description: "Random walk with trend bias + circuit breaker (±15%)",
      detail: "Price floor $0.10 / ceiling $500.00",
    },
  },
  {
    id: "event",
    type: "service",
    position: { x: 720, y: 60 },
    data: {
      label: "Domain Events",
      typeTag: "Event Bus",
      description: "StockPriceUpdatedEvent published to all listeners",
      detail: "Spring ApplicationEventPublisher",
    },
  },
  {
    id: "broadcast",
    type: "sseEdge",
    position: { x: 480, y: 320 },
    data: {
      label: "BroadcastSseService",
      typeTag: "Streaming",
      description: "SSE broadcast to all connected clients",
      detail: "CopyOnWriteArrayList<SseEmitter> — 15s heartbeat",
    },
  },
  {
    id: "frontend",
    type: "service",
    position: { x: 750, y: 320 },
    data: {
      label: "React Frontend",
      typeTag: "UI",
      description: "Price table + chart update via SSE PRICE_TICK event",
      detail: "EventSource → useStonksStream() hook",
    },
  },
  {
    id: "persist",
    type: "service",
    position: { x: 230, y: 320 },
    data: {
      label: "DB Persistence",
      typeTag: "Storage",
      description: "Snapshots saved every 60s to stock_price table",
      detail: "Also persists on shutdown via @PreDestroy",
    },
  },
]

const edges: Edge[] = [
  {
    id: "tick",
    source: "scheduler",
    target: "stock-service",
    type: "animated",
    animated: true,
    markerEnd: {
      type: MarkerType.ArrowClosed,
      color: "#00ff41",
      width: 12,
      height: 12,
    },
    data: { label: "simulate()" },
    style: {
      stroke: "#00ff41",
      strokeWidth: 2,
      strokeOpacity: 0.7,
      filter: "drop-shadow(0 0 6px rgba(0,255,65,0.4))",
    },
  },
  {
    id: "delegate",
    source: "stock-service",
    target: "cobol-price",
    type: "animated",
    animated: true,
    markerEnd: {
      type: MarkerType.ArrowClosed,
      color: "#00ff41",
      width: 12,
      height: 12,
    },
    data: { label: "calculate()" },
    style: { stroke: "#00ff41", strokeWidth: 2, strokeOpacity: 0.7 },
  },
  {
    id: "result",
    source: "cobol-price",
    target: "stock-service",
    type: "animated",
    animated: true,
    markerEnd: {
      type: MarkerType.ArrowClosed,
      color: "#00ff41",
      width: 12,
      height: 12,
    },
    data: { label: "newPrice" },
    style: {
      stroke: "#00ff41",
      strokeWidth: 1.5,
      strokeOpacity: 0.5,
      strokeDasharray: "4 4",
    },
  },
  {
    id: "publish",
    source: "stock-service",
    target: "event",
    type: "animated",
    animated: true,
    markerEnd: {
      type: MarkerType.ArrowClosed,
      color: "#00ff41",
      width: 12,
      height: 12,
    },
    data: { label: "publish event" },
    style: { stroke: "#00ff41", strokeWidth: 2, strokeOpacity: 0.7 },
  },
  {
    id: "sse-broadcast",
    source: "event",
    target: "broadcast",
    type: "animated",
    animated: true,
    style: { stroke: "#00ff41", strokeWidth: 1.5, strokeOpacity: 0.4 },
  },
  {
    id: "sse-client",
    source: "broadcast",
    target: "frontend",
    type: "animated",
    animated: true,
    markerEnd: {
      type: MarkerType.ArrowClosed,
      color: "#00ff41",
      width: 12,
      height: 12,
    },
    data: { label: "SSE: PRICE_TICK" },
    style: {
      stroke: "#00ff41",
      strokeWidth: 2.5,
      strokeOpacity: 0.8,
      filter: "drop-shadow(0 0 8px rgba(0,255,65,0.5))",
    },
  },
  {
    id: "persist-edge",
    source: "stock-service",
    target: "persist",
    type: "animated",
    animated: true,
    markerEnd: {
      type: MarkerType.ArrowClosed,
      color: "#00ff41",
      width: 12,
      height: 12,
    },
    data: { label: "persist every 60s" },
    style: {
      stroke: "#00ff41",
      strokeWidth: 1,
      strokeOpacity: 0.3,
      strokeDasharray: "8 4",
    },
  },
]

export function PriceTickFlow() {
  return (
    <DiagramWrapper
      nodes={nodes}
      edges={edges}
      nodeTypes={nodeTypes}
      edgeTypes={edgeTypes}
      defaultViewport={{ x: 0, y: 0, zoom: 0.8 }}
    />
  )
}
