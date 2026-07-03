import { TrendingUp, TrendingDown, Rocket, Bomb, Activity } from "lucide-react"

interface Stock {
  symbol: string
  name: string
  description: string
  basePrice: number
  volatility: number
  trend: "BULL" | "BEAR" | "MOON" | "CHAOS" | "CRASH"
}

const STOCKS: Stock[] = [
  {
    symbol: "COBL",
    name: "COBOL Corp",
    description: "Legacy mainframe giant. Slow but steady.",
    basePrice: 100.0,
    volatility: 0.05,
    trend: "BULL",
  },
  {
    symbol: "GMEE",
    name: "GameStonks",
    description: "Meme-fueled retail trader darling.",
    basePrice: 50.0,
    volatility: 0.25,
    trend: "MOON",
  },
  {
    symbol: "DOGE",
    name: "DogeCoin Ltd",
    description: "Much volatility. Very chaos. Wow.",
    basePrice: 10.0,
    volatility: 0.3,
    trend: "CHAOS",
  },
  {
    symbol: "TEND",
    name: "Tendie Inc",
    description: "Chicken tenders as a service. Bear market darling.",
    basePrice: 25.0,
    volatility: 0.2,
    trend: "BEAR",
  },
  {
    symbol: "FOMO",
    name: "FOMO Holdings",
    description: "Fear Of Missing Out, Inc. Enter now or regret later.",
    basePrice: 75.0,
    volatility: 0.15,
    trend: "BULL",
  },
  {
    symbol: "PAPR",
    name: "Paper Hands",
    description: "Weak hands sell first. Strong fundamentals (allegedly).",
    basePrice: 15.0,
    volatility: 0.1,
    trend: "BEAR",
  },
  {
    symbol: "YOLO",
    name: "YOLO Capital",
    description: "You Only Live Once. All-in on everything.",
    basePrice: 20.0,
    volatility: 0.5,
    trend: "CHAOS",
  },
  {
    symbol: "MEME",
    name: "MemeStonks",
    description: "Pure meme energy. Fundamentals are optional.",
    basePrice: 10.0,
    volatility: 0.2,
    trend: "MOON",
  },
  {
    symbol: "BUGS",
    name: "Buggy Software",
    description: "Every release introduces more features (and bugs).",
    basePrice: 30.0,
    volatility: 0.2,
    trend: "CRASH",
  },
  {
    symbol: "JAVA",
    name: "JavaBeans",
    description: "Enterprise-grade, rock-solid, verbose returns.",
    basePrice: 150.0,
    volatility: 0.05,
    trend: "BULL",
  },
]

const trendConfig: Record<
  Stock["trend"],
  { icon: React.ComponentType<{ size?: number; className?: string }>; color: string; label: string }
> = {
  BULL: {
    icon: TrendingUp,
    color: "#00ff41",
    label: "BULL",
  },
  BEAR: {
    icon: TrendingDown,
    color: "#ff3333",
    label: "BEAR",
  },
  MOON: {
    icon: Rocket,
    color: "#00ccff",
    label: "MOON",
  },
  CHAOS: {
    icon: Activity,
    color: "#ffcc00",
    label: "CHAOS",
  },
  CRASH: {
    icon: Bomb,
    color: "#ff8800",
    label: "CRASH",
  },
}

function TrendBadge({ trend }: { trend: Stock["trend"] }) {
  const cfg = trendConfig[trend]
  const Icon = cfg.icon
  return (
    <span
      className="inline-flex items-center gap-1 rounded border px-1.5 py-0.5 font-mono text-[9px] font-bold uppercase"
      style={{
        borderColor: `${cfg.color}33`,
        color: cfg.color,
      }}
    >
      <Icon size={10} />
      {cfg.label}
    </span>
  )
}

export function StockCatalogSection() {
  return (
    <div className="grid gap-2 sm:grid-cols-2 lg:grid-cols-5">
      {STOCKS.map((stock) => (
        <div
          key={stock.symbol}
          className="terminal-border rounded-lg bg-[#0d100d] p-3 font-mono transition-all hover:border-[#00ff41]/30 hover:bg-[#0d100d]"
        >
          <div className="flex items-center justify-between mb-1">
            <span className="text-sm font-bold text-[#00ff41] text-glow-dim">
              {stock.symbol}
            </span>
            <TrendBadge trend={stock.trend} />
          </div>
          <p className="text-[10px] text-[#008833] mb-2">{stock.name}</p>
          <p className="text-[9px] text-[#008833]/50 leading-relaxed mb-2">
            {stock.description}
          </p>
          <div className="flex items-center justify-between text-[9px]">
            <span className="text-[#008833]/60">
              Base:{" "}
              <span className="text-[#00ff41]/70">${stock.basePrice.toFixed(2)}</span>
            </span>
            <span className="text-[#008833]/60">
              Vol:{" "}
              <span className="text-[#00ff41]/70">
                {(stock.volatility * 100).toFixed(0)}%
              </span>
            </span>
          </div>
        </div>
      ))}
    </div>
  )
}
