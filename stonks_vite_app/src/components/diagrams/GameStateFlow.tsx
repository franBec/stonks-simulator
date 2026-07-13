import { type Node, type Edge, MarkerType } from "@xyflow/react"
import { DiagramWrapper } from "./DiagramWrapper"
import { ServiceNode, SseEdgeNode, ConnectorNode } from "./CustomNodes"
import { AnimatedEdge } from "./AnimatedEdge"

const nodeTypes = {
  service: ServiceNode,
  sseEdge: SseEdgeNode,
  connector: ConnectorNode,
}

const edgeTypes = {
  animated: AnimatedEdge,
}

const nodes: Node[] = [
  {
    id: "portfolio-service",
    type: "service",
    position: { x: 40, y: 80 },
    data: {
      label: "PortfolioService",
      typeTag: "Service",
      description: "Evaluates portfolio value after every trade + price tick",
      detail: "Total value = cash + positions × current prices",
      width: 220,
    },
  },
  {
    id: "game-state",
    type: "connector",
    position: { x: 360, y: 80 },
    data: {
      label: "GameStateService",
      typeTag: "State Machine",
      description: "PLAYING → WON (≥$100k) or LOST (≤$1k)",
      detail: "AtomicReference<GameStatus> — thread-safe",
      width: 240,
    },
  },
  {
    id: "win-state",
    type: "service",
    position: { x: 360, y: 300 },
    data: {
      label: "Game Over: WON",
      typeTag: "Event",
      description: "Portfolio hit win threshold → simulation freezes",
      detail: "GameWonEvent → SSE GAME_WON broadcast",
      width: 240,
    },
  },
  {
    id: "lose-state",
    type: "service",
    position: { x: 360, y: 520 },
    data: {
      label: "Game Over: LOST",
      typeTag: "Event",
      description: "Portfolio hit lose threshold → simulation freezes",
      detail: "GameLostEvent → SSE GAME_LOST broadcast",
      width: 240,
    },
  },
  {
    id: "freeze",
    type: "connector",
    position: { x: 700, y: 80 },
    data: {
      label: "Simulation Freeze",
      typeTag: "Guard",
      description: "Schedulers check isPlaying() before each tick",
      detail: "Price ticks + AI events stop on game over",
      width: 220,
    },
  },
  {
    id: "broadcast",
    type: "sseEdge",
    position: { x: 700, y: 380 },
    data: {
      label: "BroadcastSseService",
      typeTag: "Streaming",
      description: "SSE: GAME_WON / GAME_LOST / GAME_RESET",
      detail: "GAME_CONFIG sent on connect with thresholds",
      width: 220,
    },
  },
  {
    id: "frontend",
    type: "service",
    position: { x: 1020, y: 380 },
    data: {
      label: "React GameOverlay",
      typeTag: "UI",
      description: "Full-screen overlay: MOON ACHIEVED or BANKRUPT",
      detail: "Shows final value, P&L, trade count + PLAY AGAIN",
      width: 220,
    },
  },
  {
    id: "reset",
    type: "service",
    position: { x: 40, y: 520 },
    data: {
      label: "PortfolioResetController",
      typeTag: "REST",
      description: "POST /api/game/reset — restores $10k cash",
      detail: "Clears positions, resets game, resumes simulation",
      width: 240,
    },
  },
]

const edges: Edge[] = [
  {
    id: "evaluate",
    source: "portfolio-service",
    target: "game-state",
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
    data: { label: "check thresholds" },
    style: { stroke: "#ff9933", strokeWidth: 2, strokeOpacity: 0.7 },
  },
  {
    id: "mark-won",
    source: "game-state",
    target: "win-state",
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
    data: { label: "≥ $100,000" },
    style: { stroke: "#00ff41", strokeWidth: 2, strokeOpacity: 0.7 },
  },
  {
    id: "mark-lost",
    source: "game-state",
    target: "lose-state",
    sourceHandle: "bottom-source",
    targetHandle: "top",
    type: "animated",
    animated: true,
    markerEnd: {
      type: MarkerType.ArrowClosed,
      color: "#ff3333",
      width: 12,
      height: 12,
    },
    data: { label: "≤ $1,000" },
    style: { stroke: "#ff3333", strokeWidth: 2, strokeOpacity: 0.7 },
  },
  {
    id: "freeze-tick",
    source: "game-state",
    target: "freeze",
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
    data: { label: "isPlaying() = false" },
    style: { stroke: "#ff9933", strokeWidth: 1.5, strokeOpacity: 0.6 },
  },
  {
    id: "broadcast-won",
    source: "win-state",
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
    data: { label: "GAME_WON" },
    style: { stroke: "#00ff41", strokeWidth: 2, strokeOpacity: 0.7 },
  },
  {
    id: "broadcast-lost",
    source: "lose-state",
    target: "broadcast",
    sourceHandle: "right-source",
    targetHandle: "bottom",
    type: "animated",
    animated: true,
    markerEnd: {
      type: MarkerType.ArrowClosed,
      color: "#ff3333",
      width: 12,
      height: 12,
    },
    data: { label: "GAME_LOST" },
    style: { stroke: "#ff3333", strokeWidth: 2, strokeOpacity: 0.7 },
  },
  {
    id: "sse-overlay",
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
    data: { label: "SSE: GAME_WON / GAME_LOST" },
    style: {
      stroke: "#00ff41",
      strokeWidth: 2.5,
      strokeOpacity: 0.8,
      filter: "drop-shadow(0 0 8px rgba(0,255,65,0.5))",
    },
  },
  {
    id: "play-again",
    source: "frontend",
    target: "reset",
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
    data: { label: "POST /api/game/reset" },
    style: {
      stroke: "#00ff41",
      strokeWidth: 2,
      strokeOpacity: 0.7,
      strokeDasharray: "8 4",
    },
  },
  {
    id: "reset-state",
    source: "reset",
    target: "game-state",
    sourceHandle: "top-source",
    targetHandle: "left",
    type: "animated",
    animated: true,
    markerEnd: {
      type: MarkerType.ArrowClosed,
      color: "#ff9933",
      width: 12,
      height: 12,
    },
    data: { label: "reset() → PLAYING" },
    style: {
      stroke: "#ff9933",
      strokeWidth: 1.5,
      strokeOpacity: 0.5,
      strokeDasharray: "6 4",
    },
  },
  {
    id: "broadcast-reset",
    source: "reset",
    target: "broadcast",
    sourceHandle: "right-source",
    targetHandle: "bottom",
    type: "animated",
    animated: true,
    markerEnd: {
      type: MarkerType.ArrowClosed,
      color: "#00ff41",
      width: 12,
      height: 12,
    },
    data: { label: "GAME_RESET" },
    style: { stroke: "#00ff41", strokeWidth: 2, strokeOpacity: 0.7 },
  },
]

export function GameStateFlow() {
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
