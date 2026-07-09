import { BrowserRouter, Routes, Route } from "react-router-dom"
import { LandingPage } from "@/pages/LandingPage"
import { DashboardPage } from "@/pages/DashboardPage"
import { HowItWorksPage } from "@/pages/HowItWorksPage"
import { TradePage } from "@/pages/TradePage"
import { AppLayout } from "@/pages/AppLayout"
import { ResetProvider } from "@/hooks/useResetSignal"

export function App() {
  return (
    <BrowserRouter>
      <ResetProvider>
        <Routes>
          <Route path="/" element={<LandingPage />} />
          <Route path="/app" element={<AppLayout />}>
            <Route index element={<DashboardPage />} />
            <Route path="trade" element={<TradePage />} />
          </Route>
          <Route path="/how-it-works" element={<HowItWorksPage />} />
        </Routes>
      </ResetProvider>
    </BrowserRouter>
  )
}

export default App
