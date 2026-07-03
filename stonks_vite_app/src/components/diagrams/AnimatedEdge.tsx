import {
  BaseEdge,
  type EdgeProps,
  getBezierPath,
  EdgeLabelRenderer,
} from "@xyflow/react"

export function AnimatedEdge({
  id,
  sourceX,
  sourceY,
  targetX,
  targetY,
  sourcePosition,
  targetPosition,
  data,
  markerEnd,
}: EdgeProps) {
  const [edgePath, labelX, labelY] = getBezierPath({
    sourceX,
    sourceY,
    sourcePosition,
    targetX,
    targetY,
    targetPosition,
  })

  const label = data && typeof data === "object" && "label" in data ? String(data.label) : null

  return (
    <>
      <BaseEdge
        id={id}
        path={edgePath}
        style={{
          stroke: "#00ff41",
          strokeWidth: 1.5,
          strokeOpacity: 0.5,
          strokeDasharray: "6 4",
        }}
        markerEnd={markerEnd}
      />
      {label && (
        <EdgeLabelRenderer>
          <div
            className="absolute pointer-events-none font-mono text-[9px] text-[#00ff41]/50"
            style={{
              transform: `translate(-50%, -50%) translate(${labelX}px,${labelY}px)`,
            }}
          >
            {label}
          </div>
        </EdgeLabelRenderer>
      )}
    </>
  )
}
