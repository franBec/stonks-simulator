import { useEffect, useRef, useState } from "react"
import { Link } from "react-router-dom"
import {
  ArrowLeft,
  Layers,
  Boxes,
  GitBranch,
  Cpu,
  Server,
  Database,
  Globe,
  Terminal,
  Code2,
} from "lucide-react"
import { Scanlines } from "@/components/retro/Scanlines"
import { SystemContextDiagram } from "@/components/diagrams/SystemContextDiagram"
import { ContainerDiagram } from "@/components/diagrams/ContainerDiagram"
import { PriceTickFlow } from "@/components/diagrams/PriceTickFlow"
import { TradeFlow } from "@/components/diagrams/TradeFlow"
import { ChaosFeedFlow } from "@/components/diagrams/ChaosFeedFlow"
import { GameStateFlow } from "@/components/diagrams/GameStateFlow"
import { IntensityLevelsSection } from "@/components/retro/IntensityLevelsSection"
import { StockCatalogSection } from "@/components/retro/StockCatalogSection"

function useScrollReveal() {
  const ref = useRef<HTMLDivElement>(null)
  const [visible, setVisible] = useState(false)

  useEffect(() => {
    const el = ref.current
    if (!el) return
    const obs = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          setVisible(true)
          obs.unobserve(el)
        }
      },
      { threshold: 0.1 }
    )
    obs.observe(el)
    return () => obs.unobserve(el)
  }, [])

  return { ref, visible }
}

function Section({
  id,
  icon: Icon,
  title,
  subtitle,
  children,
}: {
  id: string
  icon: React.ComponentType<{ size?: number; className?: string }>
  title: string
  subtitle?: string
  children: React.ReactNode
}) {
  const { ref, visible } = useScrollReveal()

  return (
    <section
      id={id}
      ref={ref}
      className={`section-reveal mb-16 ${visible ? "visible" : ""}`}
    >
      <div className="mb-6">
        <div className="flex items-center gap-3 mb-1">
          <Icon className="text-[#00ff41]/60" size={20} />
          <h2 className="font-mono text-lg font-bold text-[#00ff41] text-glow">
            {title}
          </h2>
        </div>
        {subtitle && (
          <p className="font-mono text-xs text-[#008833]/70 ml-9">{subtitle}</p>
        )}
      </div>
      {children}
    </section>
  )
}

const TECH_STACK = [
  {
    category: "Frontend",
    icon: Globe,
    items: [
      "React 19",
      "TypeScript 5.9",
      "Vite 7",
      "Tailwind CSS v4",
      "shadcn/ui (Radix Nova)",
      "ReactFlow v12",
      "Recharts v3",
      "TanStack React Query v5",
      "react-router-dom v7",
    ],
  },
  {
    category: "Backend",
    icon: Server,
    items: [
      "Spring Boot 4.0.6",
      "Java 21",
      "Spring Modulith",
      "Resilience4j",
      "Rome RSS Parser",
      "Hibernate / JPA",
    ],
  },
  {
    category: "Trading Engine",
    icon: Terminal,
    items: [
      "GnuCOBOL 3.2",
      "3 COBOL binaries",
      "stdin/stdout JSON IPC",
      "Random walk algorithm",
      "Circuit breaker (±25%)",
      "Price floor/ceiling",
    ],
  },
  {
    category: "Infrastructure",
    icon: Database,
    items: [
      "PostgreSQL (prod)",
      "H2 Database (dev)",
      "SSE streaming",
      "OpenAPI 3.0",
      "Orval codegen",
    ],
  },
  {
    category: "AI / External",
    icon: Cpu,
    items: [
      "OpenRouter API",
      "Llama 3.1 8B",
      "BBC News RSS",
      "TechCrunch RSS",
      "The Guardian RSS",
      "OpenTelemetry (opt)",
    ],
  },
]

export function HowItWorksPage() {
  return (
    <div className="min-h-svh font-mono">
      <Scanlines />

      <div className="mx-auto max-w-6xl px-4 py-8 pb-24">
        <nav className="mb-8 flex items-center gap-4">
          <Link
            to="/"
            className="flex items-center gap-1.5 font-mono text-xs text-[#008833] transition-colors hover:text-[#00ff41]"
          >
            <ArrowLeft size={12} />
            HOME
          </Link>
          <Link
            to="/app"
            className="flex items-center gap-1.5 font-mono text-xs text-[#008833] transition-colors hover:text-[#00ff41]"
          >
            <ArrowLeft size={12} />
            DASHBOARD
          </Link>
          <span className="text-[#008833]/30">/</span>
          <span className="font-mono text-xs text-[#00ff41]/70">HOW IT WORKS</span>
        </nav>

        <header className="mb-16 text-center">
          <h1 className="mb-4 font-mono text-3xl font-bold tracking-wider text-[#00ff41] text-glow sm:text-4xl">
            HOW STONKS WORKS
          </h1>
          <p className="mx-auto max-w-2xl font-mono text-sm text-[#008833] leading-relaxed">
            A deep dive into the architecture of the COBOL-powered meme stock trading
            simulator. Explore the system design, data flows, simulation mechanics, and the
            AI-driven chaos engine that makes this platform tick.
          </p>
          <div className="mt-6 inline-flex items-center gap-2 rounded border border-[#00ff41]/10 bg-[#0d100d] px-3 py-1.5 font-mono text-[10px] text-[#008833]/60">
            <Code2 size={12} />
            <span>React · Spring Boot · COBOL · PostgreSQL · OpenRouter AI</span>
          </div>
        </header>

        <Section
          id="c4-level-1"
          icon={Layers}
          title="C4 Level 1: System Context"
          subtitle="The big picture — how the STONKS Simulator fits into its ecosystem"
        >
          <div className="terminal-border rounded-lg bg-[#0d100d] p-4 mb-4">
            <SystemContextDiagram />
          </div>
          <div className="grid gap-3 sm:grid-cols-2">
            <div className="rounded border border-[#00ff41]/10 bg-[#0d100d] p-3 font-mono text-xs">
              <span className="text-[#00ff41] font-bold">Person: </span>
              <span className="text-[#008833]">
                A single anonymous Trader interacts with the system via a web browser.
                No authentication — just $10,000 in play money.
              </span>
            </div>
            <div className="rounded border border-[#00ff41]/10 bg-[#0d100d] p-3 font-mono text-xs">
              <span className="text-[#00ff41] font-bold">External Systems: </span>
              <span className="text-[#008833]">
                OpenRouter AI (Llama 3.1 8B) generates chaotic trading events from real
                RSS headlines. Both are used only for the optional chaos feed feature.
              </span>
            </div>
          </div>
        </Section>

        <Section
          id="c4-level-2"
          icon={Boxes}
          title="C4 Level 2: Container View"
          subtitle="Zooming into the STONKS Simulator — the containers that make up the system"
        >
          <div className="terminal-border rounded-lg bg-[#0d100d] p-4 mb-4">
            <ContainerDiagram />
          </div>
          <div className="grid gap-3 sm:grid-cols-3">
            <div className="rounded border border-[#00ff41]/10 bg-[#0d100d] p-3 font-mono text-xs">
              <span className="text-[#00ff41] font-bold">React SPA: </span>
              <span className="text-[#008833]">
                3270 terminal aesthetic built with React 19, Vite 7, and Tailwind CSS v4.
                Subscribes to live price ticks via SSE.
              </span>
            </div>
            <div className="rounded border border-[#00ff41]/10 bg-[#0d100d] p-3 font-mono text-xs">
              <span className="text-[#00ff41] font-bold">Spring Boot: </span>
              <span className="text-[#008833]">
                Modular monolith using Spring Modulith with hexagonal architecture. 8
                modules: stock, trade, portfolio, broadcast, chaosevent, intensity, news, cobol.
              </span>
            </div>
            <div className="rounded border border-[#00ff41]/10 bg-[#0d100d] p-3 font-mono text-xs">
              <span className="text-[#00ff41] font-bold">COBOL: </span>
              <span className="text-[#008833]">
                3 GnuCOBOL binaries (price-engine, catalog, portfolio-mgr) communicate via
                stdin/stdout with JSON serialization. Togglable with Java stubs for development.
              </span>
            </div>
          </div>
        </Section>

        <Section
          id="price-tick-flow"
          icon={GitBranch}
          title="Data Flow: A Single Price Tick"
          subtitle="What happens every 5 seconds — end-to-end from scheduler to UI update"
        >
          <div className="terminal-border rounded-lg bg-[#0d100d] p-4 mb-4">
            <PriceTickFlow />
          </div>
          <div className="rounded border border-[#00ff41]/10 bg-[#0d100d] p-3 font-mono text-xs">
            <span className="text-[#00ff41] font-bold">The Pipeline: </span>
            <span className="text-[#008833]">
              Every 5 seconds, the StockPriceTickScheduler calls StockService.simulate(),
              which iterates all 10 stocks. For each, it delegates to the COBOL price-engine
              binary (random walk with trend bias + circuit breaker ±25%). Results are
              published as StockPriceUpdatedEvent → BroadcastSseService fans out to all
              connected SSE clients. Prices are persisted to the database every 60 seconds
              and on shutdown. Volatility is multiplied by the current intensity level (1x–25x).
            </span>
          </div>
        </Section>

        <Section
          id="trade-flow"
          icon={GitBranch}
          title="Data Flow: Trade Execution"
          subtitle="How a BUY/SELL order travels from the trader to the COBOL engine and back"
        >
          <div className="terminal-border rounded-lg bg-[#0d100d] p-4 mb-4">
            <TradeFlow />
          </div>
          <div className="rounded border border-[#00ff41]/10 bg-[#0d100d] p-3 font-mono text-xs">
            <span className="text-[#00ff41] font-bold">The Pipeline: </span>
            <span className="text-[#008833]">
              Trader submits BUY/SELL order via TradePage → TradeService validates input and
              delegates to COBOL portfolio-mgr (303 lines, 9 validation rules). The COBOL binary
              checks: valid action, valid symbol, non-zero quantity, non-zero price, sufficient
              funds (BUY with 0.5% fee), sufficient shares (SELL). On success, it returns new
              cash balance and holding quantity. TradeService persists to trade_history and
              portfolio_position tables, publishes TradeExecutedEvent → SSE broadcast to all
              clients → frontend invalidates portfolio/trade caches. After each trade, the game
              state is evaluated against win ($100k) and lose ($1k) thresholds.
            </span>
          </div>
        </Section>

        <Section
          id="chaos-feed-flow"
          icon={Cpu}
          title="Data Flow: AI Chaos Feed"
          subtitle="How real-world news becomes market mayhem via the AI chaos engine"
        >
          <div className="terminal-border rounded-lg bg-[#0d100d] p-4 mb-4">
            <ChaosFeedFlow />
          </div>
          <div className="grid gap-3 sm:grid-cols-2">
            <div className="rounded border border-[#aa33ff]/15 bg-[#0d100d] p-3 font-mono text-xs">
              <span className="text-[#cc33ff] font-bold">AI Path (real): </span>
              <span className="text-[#008833]">
                ChaoseventScheduler triggers → fetches real BBC/TechCrunch/Guardian headlines
                (60s cache) → sends to OpenRouter Llama 3.1 8B with a system prompt to
                generate a chaotic trading event → returns JSON with headline, target symbol,
                impact %, and explanation → price impact applied directly to market →
                broadcast via SSE. Rate limited: 20 req/min, 50 req/day via Resilience4j.
              </span>
            </div>
            <div className="rounded border border-[#ff9933]/15 bg-[#0d100d] p-3 font-mono text-xs">
              <span className="text-[#ff9933] font-bold">Stub Path (default): </span>
              <span className="text-[#008833]">
                When ai=stub or the AI rate limit is exceeded, the system falls back to a
                catalog of 18 pre-baked meme events: "COBOL Programmer Retired" → COBL +50%,
                "Elon Tweets Doge Again" → DOGE +25%, "Market Crash" → all -20%, etc. All
                events are logged to the chaosevent_incident_log table for audit.
              </span>
            </div>
          </div>
        </Section>

        <Section
          id="game-state-flow"
          icon={Layers}
          title="Data Flow: Game State Lifecycle"
          subtitle="Win/loss conditions, game over states, and the reset cycle"
        >
          <div className="terminal-border rounded-lg bg-[#0d100d] p-4 mb-4">
            <GameStateFlow />
          </div>
          <div className="grid gap-3 sm:grid-cols-2">
            <div className="rounded border border-[#00ff41]/10 bg-[#0d100d] p-3 font-mono text-xs">
              <span className="text-[#00ff41] font-bold">Win/Loss Detection: </span>
              <span className="text-[#008833]">
                After every trade and price tick, PortfolioService evaluates total portfolio
                value (cash + positions × current prices) against configurable thresholds.
                GameStateService transitions from PLAYING → WON at $100,000 or → LOST at
                $1,000. Both schedulers (price ticks and AI events) check gameStateService.
                isPlaying() before each execution — the entire simulation freezes on game over.
              </span>
            </div>
            <div className="rounded border border-[#00ff41]/10 bg-[#0d100d] p-3 font-mono text-xs">
              <span className="text-[#00ff41] font-bold">Reset Cycle: </span>
              <span className="text-[#008833]">
                GameOverlay renders "MOON ACHIEVED!" or "BANKRUPT!" with final P&L and trade
                count. The "PLAY AGAIN" button calls POST /api/game/reset → PortfolioResetController
                → TradeService.resetPortfolio() → clears positions, restores $10k cash, sets
                GameStateService back to PLAYING → SSE GAME_RESET broadcast → both schedulers
                resume. A GAME_CONFIG event with thresholds is sent on every SSE connection.
              </span>
            </div>
          </div>
        </Section>

        <Section
          id="intensity-levels"
          icon={Server}
          title="Simulation Variants: Intensity Levels"
          subtitle="Control the chaos — 5 levels from paper hands to maximum overdrive"
        >
          <IntensityLevelsSection />
        </Section>

        <Section
          id="stock-catalog"
          icon={Database}
          title="The 10 Meme Stocks"
          subtitle="Hardcoded in COBOL copybooks — the full stock catalog with trend biases"
        >
          <StockCatalogSection />
        </Section>

        <Section
          id="tech-stack"
          icon={Terminal}
          title="Technology Stack"
          subtitle="Everything that powers the STONKS Simulator"
        >
          <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-5">
            {TECH_STACK.map((stack) => {
              const Icon = stack.icon
              return (
                <div
                  key={stack.category}
                  className="terminal-border rounded-lg bg-[#0d100d] p-3 font-mono"
                >
                  <div className="flex items-center gap-2 mb-2 pb-2 border-b border-[#00ff41]/10">
                    <Icon size={12} className="text-[#00ff41]/60" />
                    <span className="text-[10px] font-bold text-[#00ff41] uppercase tracking-wider">
                      {stack.category}
                    </span>
                  </div>
                  <ul className="space-y-1">
                    {stack.items.map((item) => (
                      <li
                        key={item}
                        className="text-[9px] text-[#008833]/70 before:content-['>_'] before:text-[#00ff41]/30"
                      >
                        {item}
                      </li>
                    ))}
                  </ul>
                </div>
              )
            })}
          </div>
        </Section>

        <footer className="mt-16 border-t border-[#00ff41]/10 pt-6 text-center font-mono text-xs text-[#008833]/40">
          <p>
            Built with React 19 + Vite 7 + Tailwind CSS v4 · Spring Boot 4.0.6 · GnuCOBOL
            3.2 · PostgreSQL · OpenRouter AI
          </p>
          <p className="mt-1">
            Diagrams powered by ReactFlow v12 · Charts by Recharts v3
          </p>
        </footer>
      </div>
    </div>
  )
}
