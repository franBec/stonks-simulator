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
    position: { x: 40, y: 240 },
    data: {
      label: "Scheduler",
      typeTag: "Trigger",
      description: "Every 5 seconds",
      detail: "StockPriceTickScheduler",
      width: 180,
    },
  },
  {
    id: "stock-service",
    type: "service",
    position: { x: 300, y: 60 },
    data: {
      label: "StockService",
      typeTag: "Service",
      description: "Iterates 10 stocks, applies volatility, delegates to COBOL",
      detail: "ConcurrentHashMap<String, StockPrice>",
      width: 220,
    },
  },
  {
    id: "cobol-price",
    type: "cobol",
    position: { x: 640, y: 60 },
    data: {
      label: "COBOL price-engine",
      typeTag: "Process",
      description: "Random walk with trend bias + circuit breaker (±15%)",
      detail: "Price floor $0.10 / ceiling $500.00",
      width: 220,
    },
  },
  {
    id: "event",
    type: "service",
    position: { x: 980, y: 60 },
    data: {
      label: "Domain Events",
      typeTag: "Event Bus",
      description: "StockPriceUpdatedEvent published to all listeners",
      detail: "Spring ApplicationEventPublisher",
      width: 220,
    },
  },
  {
    id: "persist",
    type: "service",
    position: { x: 300, y: 400 },
    data: {
      label: "DB Persistence",
      typeTag: "Storage",
      description: "Snapshots saved every 60s to stock_price table",
      detail: "Also persists on shutdown via @PreDestroy",
      width: 220,
    },
  },
  {
    id: "broadcast",
    type: "sseEdge",
    position: { x: 640, y: 400 },
    data: {
      label: "BroadcastSseService",
      typeTag: "Streaming",
      description: "SSE broadcast to all connected clients",
      detail: "CopyOnWriteArrayList<SseEmitter> — 15s heartbeat",
      width: 220,
    },
  },
  {
    id: "frontend",
    type: "service",
    position: { x: 980, y: 400 },
    data: {
      label: "React Frontend",
      typeTag: "UI",
      description: "Price table + chart update via SSE PRICE_TICK event",
      detail: "EventSource → useStonksStream() hook",
      width: 220,
    },
  },
]

const edges: Edge[] = [
  {
    id: "tick",
    source: "scheduler",
    target: "stock-service",
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
    data: { label: "calculate()" },
    style: { stroke: "#00ff41", strokeWidth: 2, strokeOpacity: 0.7 },
  },
  {
    id: "result",
    source: "cobol-price",
    target: "stock-service",
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
    data: { label: "newPrice", arcHeight: 100 },
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
    data: { label: "publish event" },
    style: { stroke: "#00ff41", strokeWidth: 2, strokeOpacity: 0.7 },
  },
  {
    id: "sse-broadcast",
    source: "event",
    target: "broadcast",
    sourceHandle: "bottom-source",
    targetHandle: "top",
    type: "animated",
    animated: true,
    style: { stroke: "#00ff41", strokeWidth: 1.5, strokeOpacity: 0.4 },
  },
  {
    id: "sse-client",
    source: "broadcast",
    target: "frontend",
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
      height={620}
    />
  )
}
