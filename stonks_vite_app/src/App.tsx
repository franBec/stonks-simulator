import { BrowserRouter, Routes, Route } from "react-router-dom"
import { LandingPage } from "@/pages/LandingPage"
import { DashboardPage } from "@/pages/DashboardPage"
import { HowItWorksPage } from "@/pages/HowItWorksPage"
import { TradePage } from "@/pages/TradePage"
import { AppLayout } from "@/pages/AppLayout"

export function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<LandingPage />} />
        <Route path="/app" element={<AppLayout />}>
          <Route index element={<DashboardPage />} />
          <Route path="trade" element={<TradePage />} />
        </Route>
        <Route path="/how-it-works" element={<HowItWorksPage />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
