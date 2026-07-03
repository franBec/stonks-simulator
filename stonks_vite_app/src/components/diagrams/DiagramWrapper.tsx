import { type ReactNode } from "react"
import {
  ReactFlow,
  Background,
  Controls,
  MiniMap,
  type Node,
  type Edge,
  type NodeTypes,
  type EdgeTypes,
} from "@xyflow/react"
import "@xyflow/react/dist/style.css"

interface DiagramWrapperProps {
  nodes: Node[]
  edges: Edge[]
  nodeTypes: NodeTypes
  edgeTypes: EdgeTypes
  defaultViewport?: { x: number; y: number; zoom: number }
  fitView?: boolean
  children?: ReactNode
}

export function DiagramWrapper({
  nodes,
  edges,
  nodeTypes,
  edgeTypes,
  defaultViewport,
  fitView = true,
  children,
}: DiagramWrapperProps) {
  const defaultEdgeOptions = {
    type: "animated",
    animated: true,
    style: {
      stroke: "#00ff41",
      strokeWidth: 1.5,
      strokeOpacity: 0.5,
    },
  }

  return (
    <div
      className="diagram-container terminal-border w-full rounded-lg overflow-hidden"
      style={{ height: 420 }}
    >
      <ReactFlow
        nodes={nodes}
        edges={edges}
        nodeTypes={nodeTypes}
        edgeTypes={edgeTypes}
        defaultEdgeOptions={defaultEdgeOptions}
        defaultViewport={defaultViewport || { x: 0, y: 0, zoom: 1 }}
        fitView={fitView}
        fitViewOptions={{ padding: 0.3 }}
        nodesDraggable={false}
        nodesConnectable={false}
        elementsSelectable={true}
        panOnDrag
        zoomOnScroll
        zoomOnDoubleClick
        proOptions={{ hideAttribution: true }}
      >
        <Background
          color="rgba(0, 255, 65, 0.03)"
          gap={20}
          size={1}
        />
        <Controls
          className="!rounded !overflow-hidden"
          position="bottom-right"
        />
        <MiniMap
          style={{
            background: "#0a0d0a",
            border: "1px solid rgba(0, 255, 65, 0.1)",
          }}
          maskColor="rgba(0, 0, 0, 0.6)"
          nodeColor="#00ff41"
          nodeStrokeColor="rgba(0, 255, 65, 0.3)"
          pannable
          zoomable
        />
        {children}
      </ReactFlow>
    </div>
  )
}
