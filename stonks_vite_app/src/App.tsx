import { BrowserRouter, Routes, Route } from "react-router-dom"
import { LandingPage } from "@/pages/LandingPage"
import { DashboardPage } from "@/pages/DashboardPage"
import { HowItWorksPage } from "@/pages/HowItWorksPage"

export function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<LandingPage />} />
        <Route path="/app" element={<DashboardPage />} />
        <Route path="/how-it-works" element={<HowItWorksPage />} />
      </Routes>
      <footer className="py-2 text-center font-mono text-xs text-muted-foreground/40">
        made with React + Vite + Java Spring Boot + COBOL{" "}
        by{" "}
        <a
          href="https://pollito.dev/"
          target="_blank"
          rel="noopener noreferrer"
          className="text-muted-foreground/60 hover:text-foreground transition-colors"
        >
          Pollito &lt;🐤/&gt;
        </a>
      </footer>
    </BrowserRouter>
  )
}

export default App
