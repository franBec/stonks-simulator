import { useState, useEffect, useCallback } from "react"
import { useNavigate } from "react-router-dom"
import { Button } from "@/components/ui/button"
import { Scanlines } from "@/components/retro/Scanlines"

const BOOT_LINES = [
  "INITIALIZING COBOL RUNTIME................ OK",
  "LOADING MEME STOCK CATALOG................ OK (10 STOCKS)",
  "CALIBRATING PRICE ENGINE.................. READY",
  "CONNECTING TO MARKET DATA FEED............ ESTABLISHED",
  "BOOTING CHAOS ENGINE...................... ONLINE",
  'SYSTEM READY. PRESS "ENTER" TO CONTINUE.',
]

const ASCII = [
  " ███████╗████████╗ ██████╗ ███╗   ██╗██╗  ██╗███████╗",
  " ██╔════╝╚══██╔══╝██╔═══██╗████╗  ██║██║ ██╔╝██╔════╝",
  " ███████╗   ██║   ██║   ██║██╔██╗ ██║█████╔╝ ███████╗",
  " ╚════██║   ██║   ██║   ██║██║╚██╗██║██╔═██╗ ╚════██║",
  " ███████║   ██║   ╚██████╔╝██║ ╚████║██║  ██╗███████║",
  " ╚══════╝   ╚═╝    ╚═════╝ ╚═╝  ╚═══╝╚═╝  ╚═╝╚══════╝",
  "",
  "     COBOL-POWERED MEME STOCK TRADING SIMULATOR",
  '         "where mainframes meet meme culture"',
]

export function LandingPage() {
  const navigate = useNavigate()
  const [visibleLines, setVisibleLines] = useState(0)
  const [showButton, setShowButton] = useState(false)

  const totalLines = ASCII.length + BOOT_LINES.length

  useEffect(() => {
    if (visibleLines < totalLines) {
      const delay = visibleLines < ASCII.length ? 80 : 150
      const t = setTimeout(() => setVisibleLines((v) => v + 1), delay)
      return () => clearTimeout(t)
    } else {
      const t = setTimeout(() => setShowButton(true), 300)
      return () => clearTimeout(t)
    }
  }, [visibleLines, totalLines])

  const handleEnter = useCallback(() => {
    navigate("/app")
  }, [navigate])

  useEffect(() => {
    const handler = (e: KeyboardEvent) => {
      if (e.key === "Enter") navigate("/app")
    }
    window.addEventListener("keydown", handler)
    return () => window.removeEventListener("keydown", handler)
  }, [navigate])

  const asciiEnd = ASCII.length

  return (
    <div className="flex min-h-svh items-center justify-center p-4">
      <Scanlines />
      <div className="w-full max-w-3xl font-mono text-base sm:text-lg">
        <pre className="mb-6 text-sm leading-tight sm:text-base">
          {ASCII.slice(0, Math.min(visibleLines, asciiEnd)).join("\n")}
        </pre>

        <div className="space-y-1">
          {BOOT_LINES.map((line, i) => {
            const lineIdx = asciiEnd + i
            if (visibleLines <= lineIdx) return null
            return (
              <div key={i} className="animate-fade-in text-muted-foreground">
                {line}
              </div>
            )
          })}
        </div>

        {showButton && (
          <div className="mt-8 animate-fade-in text-center">
            <Button
              onClick={handleEnter}
              variant="outline"
              className="text-glow cursor-blink border-green-500/30 px-8 py-4 text-base tracking-widest"
              size="lg"
            >
              [ ENTER ]
            </Button>
            <p className="mt-3 text-xs text-muted-foreground">
              or press <kbd className="border border-green-500/20 px-1">ENTER</kbd> to
              start
            </p>
            <p className="mt-12 text-[10px] text-muted-foreground/50">
              made with React + Vite + Java Spring Boot + COBOL{" "}
              by{" "}
              <a
                href="https://pollito.dev/"
                target="_blank"
                rel="noopener noreferrer"
                className="text-muted-foreground/70 hover:text-foreground transition-colors"
              >
                Pollito &lt;🐤/&gt;
              </a>
            </p>
          </div>
        )}
      </div>
    </div>
  )
}
