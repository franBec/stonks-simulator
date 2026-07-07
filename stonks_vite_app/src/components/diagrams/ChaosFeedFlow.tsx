import { type Node, type Edge, MarkerType } from "@xyflow/react"
import { DiagramWrapper } from "./DiagramWrapper"
import {
  SchedulerNode,
  ServiceNode,
  AiNode,
  RssNode,
  SseEdgeNode,
} from "./CustomNodes"
import { AnimatedEdge } from "./AnimatedEdge"

const nodeTypes = {
  scheduler: SchedulerNode,
  service: ServiceNode,
  ai: AiNode,
  rss: RssNode,
  sseEdge: SseEdgeNode,
}

const edgeTypes = {
  animated: AnimatedEdge,
}

const nodes: Node[] = [
  {
    id: "chaos-scheduler",
    type: "scheduler",
    position: { x: 40, y: 300 },
    data: {
      label: "Chaos Scheduler",
      typeTag: "Trigger",
      description: "Dynamic interval based on intensity level",
      detail: "30s (Max Overdrive) to 15min (Paper Hands)",
      width: 200,
    },
  },
  {
    id: "chaos-service",
    type: "service",
    position: { x: 300, y: 60 },
    data: {
      label: "ChaoseventService",
      typeTag: "Orchestrator",
      description: "Core chaos pipeline: fetch news → generate event → apply impact → broadcast",
      detail: "Composite pattern for AI/stub adapters",
      width: 220,
    },
  },
  {
    id: "rss-feeds",
    type: "rss",
    position: { x: 300, y: 360 },
    data: {
      label: "RSS News Feeds",
      typeTag: "External",
      description: "BBC Tech, TechCrunch, The Guardian Business",
      detail: "Cached 60s — Rome RSS parser",
      width: 220,
    },
  },
  {
    id: "ai-openrouter",
    type: "ai",
    position: { x: 640, y: 60 },
    data: {
      label: "OpenRouter AI",
      typeTag: "Primary Adapter",
      description: "Llama 3.1 8B — generates chaotic trading event from real headlines",
      detail: "Rate limited: 20/min, 50/day",
      width: 220,
    },
  },
  {
    id: "fallback",
    type: "service",
    position: { x: 640, y: 360 },
    data: {
      label: "Fallback Catalog",
      typeTag: "Fallback Adapter",
      description: "18 pre-baked meme events: 'COBOL Programmer Retired', 'Elon Tweets Doge', etc.",
      detail: "Used when stonks.adapters.ai=stub or AI rate limit hit",
      width: 220,
    },
  },
  {
    id: "stock-impact",
    type: "service",
    position: { x: 980, y: 60 },
    data: {
      label: "StockService",
      typeTag: "Impact Handler",
      description: "Applies chaotic impact to in-memory stock price",
      detail: "onApplyStockImpact() — capped at ±50%",
      width: 220,
    },
  },
  {
    id: "db-audit",
    type: "service",
    position: { x: 980, y: 360 },
    data: {
      label: "Incident Log",
      typeTag: "Persistence",
      description: "All events logged to chaosevent_incident_log table",
      detail: "Audit trail: headline, symbol, impact%, type, severity",
      width: 220,
    },
  },
  {
    id: "broadcast",
    type: "sseEdge",
    position: { x: 640, y: 640 },
    data: {
      label: "BroadcastSseService",
      typeTag: "Streaming",
      description: "SSE CHAOS_EVENT with headline, symbol, impact, explanation",
      detail: "Broadcast to all connected clients",
      width: 220,
    },
  },
  {
    id: "frontend",
    type: "service",
    position: { x: 980, y: 640 },
    data: {
      label: "React Frontend",
      typeTag: "UI",
      description: "ChaosFeed component renders chaotic events in sidebar",
      detail: "SSE CHAOS_EVENT → toast notification",
      width: 220,
    },
  },
]

const edges: Edge[] = [
  {
    id: "trigger",
    source: "chaos-scheduler",
    target: "chaos-service",
    sourceHandle: "right-source",
    targetHandle: "left",
    type: "animated",
    animated: true,
    markerEnd: {
      type: MarkerType.ArrowClosed,
      color: "#ff3333",
      width: 12,
      height: 12,
    },
    data: { label: "triggerEvent()" },
    style: {
      stroke: "#ff3333",
      strokeWidth: 2,
      strokeOpacity: 0.7,
      filter: "drop-shadow(0 0 6px rgba(255,51,51,0.4))",
    },
  },
  {
    id: "fetch-news",
    source: "chaos-service",
    target: "rss-feeds",
    sourceHandle: "bottom-source",
    targetHandle: "top",
    type: "animated",
    animated: true,
    markerEnd: {
      type: MarkerType.ArrowClosed,
      color: "#ff9933",
      width: 12,
      height: 12,
    },
    data: { label: "getHeadlines()" },
    style: {
      stroke: "#ff9933",
      strokeWidth: 1.5,
      strokeOpacity: 0.6,
      strokeDasharray: "4 4",
    },
  },
  {
    id: "return-headlines",
    source: "rss-feeds",
    target: "chaos-service",
    sourceHandle: "top-source",
    targetHandle: "bottom",
    type: "animated",
    animated: true,
    style: {
      stroke: "#ff9933",
      strokeWidth: 1,
      strokeOpacity: 0.3,
      strokeDasharray: "4 4",
    },
  },
  {
    id: "ai-generate",
    source: "chaos-service",
    target: "ai-openrouter",
    sourceHandle: "right-source",
    targetHandle: "left",
    type: "animated",
    animated: true,
    markerEnd: {
      type: MarkerType.ArrowClosed,
      color: "#cc33ff",
      width: 12,
      height: 12,
    },
    data: { label: "generate()" },
    style: {
      stroke: "#cc33ff",
      strokeWidth: 2,
      strokeOpacity: 0.7,
      filter: "drop-shadow(0 0 6px rgba(204,51,255,0.4))",
    },
  },
  {
    id: "ai-response",
    source: "ai-openrouter",
    target: "chaos-service",
    sourceHandle: "bottom-source",
    targetHandle: "bottom",
    type: "animated",
    animated: true,
    data: { label: "ChaoticEvent", arcHeight: 100 },
    style: {
      stroke: "#cc33ff",
      strokeWidth: 1.5,
      strokeOpacity: 0.5,
      strokeDasharray: "4 4",
    },
  },
  {
    id: "fallback-gen",
    source: "chaos-service",
    target: "fallback",
    sourceHandle: "bottom-source",
    targetHandle: "top",
    type: "animated",
    animated: true,
    markerEnd: {
      type: MarkerType.ArrowClosed,
      color: "#ff9933",
      width: 12,
      height: 12,
    },
    data: { label: "fallback()" },
    style: {
      stroke: "#ff9933",
      strokeWidth: 1.5,
      strokeOpacity: 0.5,
      strokeDasharray: "4 4",
    },
  },
  {
    id: "fallback-response",
    source: "fallback",
    target: "chaos-service",
    sourceHandle: "top-source",
    targetHandle: "bottom",
    type: "animated",
    animated: true,
    style: {
      stroke: "#ff9933",
      strokeWidth: 1,
      strokeOpacity: 0.3,
      strokeDasharray: "4 4",
    },
  },
  {
    id: "impact",
    source: "chaos-service",
    target: "stock-impact",
    sourceHandle: "right-source",
    targetHandle: "left",
    type: "animated",
    animated: true,
    markerEnd: {
      type: MarkerType.ArrowClosed,
      color: "#ff3333",
      width: 12,
      height: 12,
    },
    data: { label: "ApplyImpact" },
    style: {
      stroke: "#ff3333",
      strokeWidth: 2,
      strokeOpacity: 0.7,
      filter: "drop-shadow(0 0 6px rgba(255,51,51,0.4))",
    },
  },
  {
    id: "broadcast-event",
    source: "chaos-service",
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
    data: { label: "ChaoticEventTriggered" },
    style: { stroke: "#00ff41", strokeWidth: 2, strokeOpacity: 0.6 },
  },
  {
    id: "sse-to-client",
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
    data: { label: "SSE: CHAOS_EVENT" },
    style: {
      stroke: "#00ff41",
      strokeWidth: 2.5,
      strokeOpacity: 0.8,
      filter: "drop-shadow(0 0 8px rgba(0,255,65,0.5))",
    },
  },
  {
    id: "audit-log",
    source: "chaos-service",
    target: "db-audit",
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
    data: { label: "record()" },
    style: {
      stroke: "#00ff41",
      strokeWidth: 1,
      strokeOpacity: 0.3,
      strokeDasharray: "8 4",
    },
  },
]

export function ChaosFeedFlow() {
  return (
    <DiagramWrapper
      nodes={nodes}
      edges={edges}
      nodeTypes={nodeTypes}
      edgeTypes={edgeTypes}
      height={720}
    />
  )
}
