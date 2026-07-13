import { type Node, type Edge, MarkerType } from "@xyflow/react"
import { DiagramWrapper } from "./DiagramWrapper"
import {
  ContainerNode,
  DatabaseNode,
  CobolNode,
  SchedulerNode,
  SseEdgeNode,
} from "./CustomNodes"
import { AnimatedEdge } from "./AnimatedEdge"

const nodeTypes = {
  container: ContainerNode,
  database: DatabaseNode,
  cobol: CobolNode,
  scheduler: SchedulerNode,
  sseEdge: SseEdgeNode,
}

const edgeTypes = {
  animated: AnimatedEdge,
}

const nodes: Node[] = [
  {
    id: "react-spa",
    type: "container",
    position: { x: 60, y: 140 },
    data: {
      label: "React SPA",
      typeTag: "Container",
      description: "Single-page app with 3270 terminal aesthetic",
      detail: "React 19 + Vite + Tailwind CSS v4",
      width: 220,
    },
  },
  {
    id: "spring-boot",
    type: "container",
    position: { x: 380, y: 140 },
    data: {
      label: "Spring Boot Backend",
      typeTag: "Container",
      description: "REST API + SSE streaming + AI orchestration",
      detail: "Spring Boot 4.0.6 (Java 21) — Spring Modulith",
      width: 260,
      services: [
        "StockService",
        "TradeService",
        "BroadcastSseService",
        "ChaoseventService",
        "IntensityService",
        "NewsService",
      ],
    },
  },
  {
    id: "database",
    type: "database",
    position: { x: 740, y: 140 },
    data: {
      label: "PostgreSQL / H2",
      typeTag: "Container",
      description: "Portfolio, positions, prices, trade history, chaos event log",
      detail: "6 tables — PostgreSQL (prod) / H2 (dev)",
      width: 220,
    },
  },
  {
    id: "scheduler",
    type: "scheduler",
    position: { x: 60, y: 460 },
    data: {
      label: "Scheduler",
      typeTag: "Component",
      description: "Fixed-rate price ticks every 5s + dynamic chaos event scheduling",
      detail: "StockPriceTickScheduler + ChaoseventScheduler",
      width: 220,
    },
  },
  {
    id: "cobol-engine",
    type: "cobol",
    position: { x: 380, y: 460 },
    data: {
      label: "COBOL Trading Engine",
      typeTag: "Container",
      description: "Legacy trading logic: price simulation, stock catalog, trade validation",
      detail: "GnuCOBOL 3.2 — stdin/stdout JSON serialization",
      width: 260,
    },
  },
]

const edges: Edge[] = [
  {
    id: "react-sse",
    source: "spring-boot",
    target: "react-spa",
    sourceHandle: "top-source",
    targetHandle: "top",
    type: "animated",
    animated: true,
    markerEnd: {
      type: MarkerType.ArrowClosed,
      color: "#00ff41",
      width: 12,
      height: 12,
    },
    data: { label: "SSE Stream", arcHeight: 140 },
    style: { stroke: "#00ff41", strokeWidth: 2.5, strokeOpacity: 0.9, filter: "drop-shadow(0 0 8px rgba(0,255,65,0.55))" },
  },
  {
    id: "react-rest",
    source: "react-spa",
    target: "spring-boot",
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
    data: { label: "REST API (JSON)" },
    style: { stroke: "#00ff41", strokeWidth: 2.5, strokeOpacity: 0.85, filter: "drop-shadow(0 0 7px rgba(0,255,65,0.45))" },
  },
  {
    id: "scheduler-tick",
    source: "scheduler",
    target: "spring-boot",
    sourceHandle: "top-source",
    targetHandle: "bottom",
    type: "animated",
    animated: true,
    markerEnd: {
      type: MarkerType.ArrowClosed,
      color: "#00ff41",
      width: 12,
      height: 12,
    },
    data: { label: "tick() every 5s" },
    style: {
      stroke: "#00ff41",
      strokeWidth: 2.5,
      strokeOpacity: 0.9,
      strokeDasharray: "4 4",
      filter: "drop-shadow(0 0 8px rgba(0,255,65,0.55))",
    },
  },
  {
    id: "boot-cobol",
    source: "spring-boot",
    target: "cobol-engine",
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
    data: { label: "stdin/stdout JSON" },
    style: { stroke: "#00ff41", strokeWidth: 2.5, strokeOpacity: 0.85, filter: "drop-shadow(0 0 7px rgba(0,255,65,0.45))" },
  },
  {
    id: "boot-db",
    source: "spring-boot",
    target: "database",
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
    data: { label: "JDBC / JPA" },
    style: {
      stroke: "#00ff41",
      strokeWidth: 2.5,
      strokeOpacity: 0.85,
      strokeDasharray: "5 5",
      filter: "drop-shadow(0 0 7px rgba(0,255,65,0.45))",
    },
  },
]

export function ContainerDiagram() {
  return (
    <DiagramWrapper
      nodes={nodes}
      edges={edges}
      nodeTypes={nodeTypes}
      edgeTypes={edgeTypes}
      height={680}
    />
  )
}
