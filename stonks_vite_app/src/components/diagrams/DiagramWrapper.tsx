import { type ReactNode, useCallback } from "react"
import {
  ReactFlow,
  Background,
  Controls,
  type Node,
  type Edge,
  type NodeTypes,
  type EdgeTypes,
  type ReactFlowInstance,
} from "@xyflow/react"
import "@xyflow/react/dist/style.css"

interface DiagramWrapperProps {
  nodes: Node[]
  edges: Edge[]
  nodeTypes: NodeTypes
  edgeTypes: EdgeTypes
  defaultViewport?: { x: number; y: number; zoom: number }
  fitView?: boolean
  height?: number
  children?: ReactNode
}

export function DiagramWrapper({
  nodes,
  edges,
  nodeTypes,
  edgeTypes,
  defaultViewport,
  fitView = true,
  height = 620,
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

  const onInit = useCallback(
    (instance: ReactFlowInstance) => {
      if (!fitView) return
      // Defer fitView so ReactFlow has measured nodes and the parent layout is stable.
      requestAnimationFrame(() => {
        instance.fitView({ padding: 0.1, minZoom: 0.4, maxZoom: 1 })
      })
    },
    [fitView]
  )

  return (
    <div
      className="diagram-container terminal-border w-full rounded-lg overflow-hidden"
      style={{ height }}
    >
      <ReactFlow
        nodes={nodes}
        edges={edges}
        nodeTypes={nodeTypes}
        edgeTypes={edgeTypes}
        defaultEdgeOptions={defaultEdgeOptions}
        defaultViewport={defaultViewport || { x: 0, y: 0, zoom: 1 }}
        fitView={fitView}
        fitViewOptions={{ padding: 0.1, minZoom: 0.4, maxZoom: 1 }}
        minZoom={0.3}
        maxZoom={1.5}
        nodesDraggable={false}
        nodesConnectable={false}
        elementsSelectable={true}
        panOnDrag
        zoomOnScroll
        zoomOnDoubleClick
        onInit={onInit}
        proOptions={{ hideAttribution: true }}
      >
        <Background
          color="rgba(0, 255, 65, 0.04)"
          gap={24}
          size={1}
        />
        <Controls
          className="!rounded !overflow-hidden"
          position="bottom-right"
        />
        {children}
      </ReactFlow>
    </div>
  )
}
