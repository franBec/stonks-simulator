import type { NodeProps } from "@xyflow/react"
import { Handle, Position } from "@xyflow/react"
import {
  User,
  Monitor,
  Database,
  Cloud,
  Clock,
  Cpu,
  Radio,
  BrainCircuit,
  Rss,
  ArrowRightLeft,
  Layers,
} from "lucide-react"
import type { ComponentType, CSSProperties, ReactNode } from "react"

interface BaseNodeData {
  label: string
  typeTag?: string
  description?: string
  detail?: string
  services?: string[]
  width?: number
  height?: number
}

type BaseNodeProps = NodeProps & { data: BaseNodeData }

function BaseNode({
  data,
  selected,
  icon: Icon,
  className = "",
  style,
  children,
}: BaseNodeProps & {
  icon: ComponentType<{ className?: string; size?: number }>
  className?: string
  style?: CSSProperties
  children?: ReactNode
}) {
  return (
    <div
      className={`rounded border px-3 py-2.5 font-mono text-xs transition-all ${
        selected
          ? "border-[#00ff41]/60 shadow-[0_0_16px_rgba(0,255,65,0.2)]"
          : "border-[#00ff4122] hover:border-[#00ff41]/40"
      } bg-[#0d100d] ${className}`}
      style={{ minWidth: 160, width: data.width, ...style }}
    >
      <Handle
        id="top"
        type="target"
        position={Position.Top}
        className="!bg-[#00ff41]/40 !border-[#0d100d]"
      />
      <Handle
        id="left"
        type="target"
        position={Position.Left}
        className="!bg-[#00ff41]/40 !border-[#0d100d]"
      />
      <Handle
        id="bottom"
        type="target"
        position={Position.Bottom}
        className="!bg-[#00ff41]/40 !border-[#0d100d]"
      />
      <Handle
        id="right"
        type="target"
        position={Position.Right}
        className="!bg-[#00ff41]/40 !border-[#0d100d]"
      />
      <div className="flex items-center gap-2 mb-1">
        <Icon className="text-[#00ff41]/70" size={14} />
        <span className="text-[#00ff41] font-bold">{data.label}</span>
      </div>
      {data.typeTag && (
        <span className="text-[10px] text-[#008833] uppercase tracking-wider">
          [{data.typeTag}]
        </span>
      )}
      {data.description && (
        <p className="text-[10px] text-[#008833]/85 mt-1.5 leading-relaxed">
          {data.description}
        </p>
      )}
      {data.detail && (
        <p className="text-[10px] text-[#00ff41]/60 mt-1">{data.detail}</p>
      )}
      {children}
      <Handle
        id="top-source"
        type="source"
        position={Position.Top}
        className="!opacity-0 !w-1 !h-1"
      />
      <Handle
        id="left-source"
        type="source"
        position={Position.Left}
        className="!opacity-0 !w-1 !h-1"
      />
      <Handle
        id="bottom-source"
        type="source"
        position={Position.Bottom}
        className="!opacity-0 !w-1 !h-1"
      />
      <Handle
        id="right-source"
        type="source"
        position={Position.Right}
        className="!opacity-0 !w-1 !h-1"
      />
    </div>
  )
}

export function PersonNode(props: NodeProps) {
  return <BaseNode {...(props as BaseNodeProps)} icon={User} />
}

export function ContainerNode(props: NodeProps) {
  const p = props as BaseNodeProps
  return (
    <BaseNode {...p} icon={Monitor} className="border-[#00ff41]/25">
      {p.data.services && p.data.services.length > 0 && (
        <div className="mt-2 flex flex-wrap gap-1 max-w-[220px]">
          {p.data.services.map((s: string) => (
            <span
              key={s}
              className="rounded border border-[#00ff41]/10 bg-[#112211]/50 px-1.5 py-0.5 text-[9px] text-[#00ff41]/60"
            >
              {s}
            </span>
          ))}
        </div>
      )}
    </BaseNode>
  )
}

export function DatabaseNode(props: NodeProps) {
  const data = (props as BaseNodeProps).data
  return (
    <div
      className={`rounded-t-[4px] rounded-b-[12px] border px-3 pt-2.5 pb-3 font-mono text-xs transition-all ${
        props.selected
          ? "border-[#00ff41]/60 shadow-[0_0_16px_rgba(0,255,65,0.2)]"
          : "border-[#00ff4122] hover:border-[#00ff41]/40"
      } bg-[#0d100d]`}
      style={{ minWidth: 150 }}
    >
      <Handle
        id="top"
        type="target"
        position={Position.Top}
        className="!bg-[#00ff41]/40 !border-[#0d100d]"
      />
      <Handle
        id="left"
        type="target"
        position={Position.Left}
        className="!bg-[#00ff41]/40 !border-[#0d100d]"
      />
      <Handle
        id="bottom"
        type="target"
        position={Position.Bottom}
        className="!bg-[#00ff41]/40 !border-[#0d100d]"
      />
      <Handle
        id="right"
        type="target"
        position={Position.Right}
        className="!bg-[#00ff41]/40 !border-[#0d100d]"
      />
      <div className="flex items-center gap-2 mb-1">
        <Database className="text-[#00ff41]/70" size={14} />
        <span className="text-[#00ff41] font-bold">{data.label}</span>
      </div>
      {data.typeTag && (
        <span className="text-[10px] text-[#008833] uppercase tracking-wider">
          [{data.typeTag}]
        </span>
      )}
      {data.description && (
        <p className="text-[10px] text-[#008833]/85 mt-1.5 leading-relaxed">
          {data.description}
        </p>
      )}
      <Handle
        id="top-source"
        type="source"
        position={Position.Top}
        className="!opacity-0 !w-1 !h-1"
      />
      <Handle
        id="left-source"
        type="source"
        position={Position.Left}
        className="!opacity-0 !w-1 !h-1"
      />
      <Handle
        id="bottom-source"
        type="source"
        position={Position.Bottom}
        className="!opacity-0 !w-1 !h-1"
      />
      <Handle
        id="right-source"
        type="source"
        position={Position.Right}
        className="!opacity-0 !w-1 !h-1"
      />
    </div>
  )
}

export function ExternalNode(props: NodeProps) {
  const data = (props as BaseNodeProps).data
  return (
    <BaseNode
      {...(props as BaseNodeProps)}
      icon={Cloud}
      style={data.width ? { width: data.width } : undefined}
    />
  )
}

export function SchedulerNode(props: NodeProps) {
  return (
    <BaseNode
      {...(props as BaseNodeProps)}
      icon={Clock}
      className="border-[#00ff41]/20 bg-[#0a0f0a]"
    />
  )
}

export function ServiceNode(props: NodeProps) {
  return (
    <BaseNode
      {...(props as BaseNodeProps)}
      icon={Layers}
      className="border-[#00ff41]/20 bg-[#0a0f0a]"
    />
  )
}

export function SystemBoundaryNode(props: NodeProps) {
  const data = (props as BaseNodeProps).data
  return (
    <div
      className="rounded-lg border border-dashed border-[#00ff41]/10 px-4 py-3 font-mono"
      style={{
        background: "rgba(0, 255, 65, 0.015)",
        width: data.width ?? 400,
        height: data.height ?? 300,
        display: "flex",
        alignItems: "flex-start",
      }}
    >
      <span className="text-[10px] text-[#008833]/60 uppercase tracking-wider">
        {data.label}
      </span>
    </div>
  )
}

export function SseEdgeNode(props: NodeProps) {
  return (
    <BaseNode
      {...(props as BaseNodeProps)}
      icon={Radio}
      className="border-[#00ff41]/20 bg-[#0a0f0a]"
    />
  )
}

export function AiNode(props: NodeProps) {
  return (
    <BaseNode
      {...(props as BaseNodeProps)}
      icon={BrainCircuit}
      className="border-[#cc33ff]/25"
    />
  )
}

export function RssNode(props: NodeProps) {
  return (
    <BaseNode
      {...(props as BaseNodeProps)}
      icon={Rss}
      className="border-[#ff9933]/25"
    />
  )
}

export function CobolNode(props: NodeProps) {
  return (
    <BaseNode
      {...(props as BaseNodeProps)}
      icon={Cpu}
      className="border-[#00ff41]/25 bg-[#0a120a]"
    />
  )
}

export function ConnectorNode(props: NodeProps) {
  return (
    <BaseNode
      {...(props as BaseNodeProps)}
      icon={ArrowRightLeft}
      className="border-[#00ff41]/15 bg-[#0a0f0a]"
    />
  )
}
