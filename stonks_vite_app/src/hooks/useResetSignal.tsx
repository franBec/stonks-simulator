import { createContext, useContext, useState, useEffect, type ReactNode } from "react"

interface ResetContextValue {
  version: number
}

const ResetContext = createContext<ResetContextValue>({ version: 0 })

export function ResetProvider({ children }: { children: ReactNode }) {
  const [version, setVersion] = useState(0)

  useEffect(() => {
    const handler = () => setVersion((v) => v + 1)
    window.addEventListener("stonks-game-reset", handler)
    return () => window.removeEventListener("stonks-game-reset", handler)
  }, [])

  return <ResetContext.Provider value={{ version }}>{children}</ResetContext.Provider>
}

export function useResetSignal() {
  return useContext(ResetContext)
}
