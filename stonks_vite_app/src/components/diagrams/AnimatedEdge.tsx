import {
  BaseEdge,
  type EdgeProps,
  getBezierPath,
  EdgeLabelRenderer,
  Position,
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
  style,
  markerEnd,
}: EdgeProps) {
  const arcHeight =
    data && typeof data === "object" && "arcHeight" in data
      ? Number(data.arcHeight)
      : undefined

  let edgePath: string
  let labelX: number
  let labelY: number

  if (
    arcHeight &&
    sourcePosition === Position.Top &&
    targetPosition === Position.Top
  ) {
    const h = arcHeight
    const c1x = sourceX
    const c1y = sourceY - h
    const c2x = targetX
    const c2y = targetY - h
    edgePath = `M ${sourceX},${sourceY} C ${c1x},${c1y} ${c2x},${c2y} ${targetX},${targetY}`
    labelX = 0.5 * sourceX + 0.5 * targetX
    labelY = 0.5 * sourceY + 0.5 * targetY - 0.75 * h
  } else if (
    arcHeight &&
    sourcePosition === Position.Bottom &&
    targetPosition === Position.Bottom
  ) {
    const h = arcHeight
    const c1x = sourceX
    const c1y = sourceY + h
    const c2x = targetX
    const c2y = targetY + h
    edgePath = `M ${sourceX},${sourceY} C ${c1x},${c1y} ${c2x},${c2y} ${targetX},${targetY}`
    labelX = 0.5 * sourceX + 0.5 * targetX
    labelY = 0.5 * sourceY + 0.5 * targetY + 0.75 * h
  } else {
    ;[edgePath, labelX, labelY] = getBezierPath({
      sourceX,
      sourceY,
      sourcePosition,
      targetX,
      targetY,
      targetPosition,
    })
  }

  const label = data && typeof data === "object" && "label" in data ? String(data.label) : null

  const edgeStyle = {
    stroke: "#00ff41",
    strokeWidth: 1.5,
    strokeOpacity: 0.5,
    strokeDasharray: "6 4",
    ...style,
  }

  return (
    <>
      <BaseEdge id={id} path={edgePath} style={edgeStyle} markerEnd={markerEnd} />
      {label && (
        <EdgeLabelRenderer>
          <div
            className="absolute pointer-events-none rounded border border-[#00ff41]/10 bg-[#0a0d0a]/90 px-2 py-1 font-mono text-[10px] font-medium text-[#00ff41]/85 backdrop-blur-sm"
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
