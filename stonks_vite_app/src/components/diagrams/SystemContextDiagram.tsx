import { type Node, type Edge, MarkerType } from "@xyflow/react"
import { DiagramWrapper } from "./DiagramWrapper"
import {
  PersonNode,
  ExternalNode,
  SystemBoundaryNode,
} from "./CustomNodes"
import { AnimatedEdge } from "./AnimatedEdge"

const nodeTypes = {
  person: PersonNode,
  external: ExternalNode,
  systemBoundary: SystemBoundaryNode,
}

const edgeTypes = {
  animated: AnimatedEdge,
}

const nodes: Node[] = [
  {
    id: "trader",
    type: "person",
    position: { x: 50, y: 180 },
    data: {
      label: "Trader",
      typeTag: "Person",
      description: "Single user with $10,000 play money",
    },
  },
  {
    id: "system-boundary",
    type: "systemBoundary",
    position: { x: 300, y: 40 },
    data: {
      label: "STONKS Simulator [Software System]",
      width: 320,
      height: 400,
    },
    selectable: false,
    draggable: false,
  },
  {
    id: "stonks-system",
    type: "external",
    position: { x: 350, y: 200 },
    data: {
      label: "STONKS Simulator",
      typeTag: "Software System",
      description:
        "COBOL-powered meme stock trading platform. Live price simulation, AI chaos events, portfolio tracking.",
      detail: "React + Spring Boot + COBOL + PostgreSQL",
    },
    parentId: "system-boundary",
    extent: "parent" as const,
  },
  {
    id: "openrouter",
    type: "external",
    position: { x: 750, y: 150 },
    data: {
      label: "OpenRouter API",
      typeTag: "External System",
      description: "Llama 3.1 8B — AI memeification of real-world news into chaotic market events",
    },
  },
  {
    id: "rss-feeds",
    type: "external",
    position: { x: 750, y: 320 },
    data: {
      label: "RSS News Feeds",
      typeTag: "External System",
      description: "BBC Tech, TechCrunch, The Guardian Business — real headlines for AI context",
    },
  },
]

const edges: Edge[] = [
  {
    id: "trader-sse",
    source: "stonks-system",
    target: "trader",
    type: "animated",
    animated: true,
    markerEnd: { type: MarkerType.ArrowClosed, color: "#00ff41", width: 12, height: 12 },
    data: { label: "SSE Stream" },
    style: { stroke: "#00ff41", strokeWidth: 2, strokeOpacity: 0.6 },
  },
  {
    id: "trader-rest",
    source: "trader",
    target: "stonks-system",
    type: "animated",
    animated: true,
    markerEnd: { type: MarkerType.ArrowClosed, color: "#00ff41", width: 12, height: 12 },
    data: { label: "REST API" },
    style: { stroke: "#00ff41", strokeWidth: 1.5, strokeOpacity: 0.4 },
  },
  {
    id: "stonks-ai",
    source: "stonks-system",
    target: "openrouter",
    type: "animated",
    animated: true,
    markerEnd: { type: MarkerType.ArrowClosed, color: "#cc33ff", width: 12, height: 12 },
    data: { label: "AI Prompt" },
    style: { stroke: "#cc33ff", strokeWidth: 1.5, strokeOpacity: 0.5, strokeDasharray: "4 4" },
  },
  {
    id: "stonks-rss",
    source: "stonks-system",
    target: "rss-feeds",
    type: "animated",
    animated: true,
    markerEnd: { type: MarkerType.ArrowClosed, color: "#ff9933", width: 12, height: 12 },
    data: { label: "HTTP GET" },
    style: { stroke: "#ff9933", strokeWidth: 1.5, strokeOpacity: 0.5, strokeDasharray: "4 4" },
  },
]

export function SystemContextDiagram() {
  return (
    <DiagramWrapper
      nodes={nodes}
      edges={edges}
      nodeTypes={nodeTypes}
      edgeTypes={edgeTypes}
      defaultViewport={{ x: 0, y: 0, zoom: 0.85 }}
    />
  )
}
