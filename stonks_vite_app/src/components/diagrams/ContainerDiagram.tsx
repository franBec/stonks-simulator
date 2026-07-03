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
    position: { x: 50, y: 50 },
    data: {
      label: "React SPA",
      typeTag: "Container",
      description: "Single-page app with 3270 terminal aesthetic",
      detail: "React 19 + Vite + Tailwind CSS v4",
    },
  },
  {
    id: "spring-boot",
    type: "container",
    position: { x: 330, y: 50 },
    data: {
      label: "Spring Boot Backend",
      typeTag: "Container",
      description: "REST API + SSE streaming + AI orchestration",
      detail: "Spring Boot 4.0.6 (Java 25) — Spring Modulith",
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
    id: "cobol-engine",
    type: "cobol",
    position: { x: 330, y: 310 },
    data: {
      label: "COBOL Trading Engine",
      typeTag: "Container",
      description: "Legacy trading logic: price simulation, stock catalog, trade validation",
      detail: "GnuCOBOL 3.2 — stdin/stdout JSON serialization",
    },
  },
  {
    id: "database",
    type: "database",
    position: { x: 620, y: 310 },
    data: {
      label: "PostgreSQL / H2",
      typeTag: "Container",
      description: "Portfolio, positions, prices, trade history, chaos event log",
      detail: "6 tables — PostgreSQL (prod) / H2 (dev)",
    },
  },
  {
    id: "scheduler",
    type: "scheduler",
    position: { x: 50, y: 310 },
    data: {
      label: "Scheduler",
      typeTag: "Component",
      description: "Fixed-rate price ticks every 5s + dynamic chaos event scheduling",
      detail: "StockPriceTickScheduler + ChaoseventScheduler",
    },
  },
]

const edges: Edge[] = [
  {
    id: "react-sse",
    source: "spring-boot",
    target: "react-spa",
    type: "animated",
    animated: true,
    markerEnd: {
      type: MarkerType.ArrowClosed,
      color: "#00ff41",
      width: 12,
      height: 12,
    },
    data: { label: "SSE Stream" },
    style: { stroke: "#00ff41", strokeWidth: 2, strokeOpacity: 0.6 },
  },
  {
    id: "react-rest",
    source: "react-spa",
    target: "spring-boot",
    type: "animated",
    animated: true,
    markerEnd: {
      type: MarkerType.ArrowClosed,
      color: "#00ff41",
      width: 12,
      height: 12,
    },
    data: { label: "REST API (JSON)" },
    style: { stroke: "#00ff41", strokeWidth: 1.5, strokeOpacity: 0.4 },
  },
  {
    id: "scheduler-tick",
    source: "scheduler",
    target: "spring-boot",
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
      strokeWidth: 2,
      strokeOpacity: 0.6,
      strokeDasharray: "3 6",
    },
  },
  {
    id: "boot-cobol",
    source: "spring-boot",
    target: "cobol-engine",
    type: "animated",
    animated: true,
    markerEnd: {
      type: MarkerType.ArrowClosed,
      color: "#00ff41",
      width: 12,
      height: 12,
    },
    data: { label: "stdin/stdout JSON" },
    style: { stroke: "#00ff41", strokeWidth: 1.5, strokeOpacity: 0.5 },
  },
  {
    id: "boot-db",
    source: "spring-boot",
    target: "database",
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
      strokeWidth: 1.5,
      strokeOpacity: 0.4,
      strokeDasharray: "5 5",
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
    />
  )
}
