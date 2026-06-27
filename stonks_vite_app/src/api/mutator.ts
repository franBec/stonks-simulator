const API_BASE = import.meta.env.VITE_BACKEND_URL ?? "http://localhost:8080"

export const customFetch = async <T>(
  url: string,
  options?: RequestInit,
): Promise<T> => {
  const response = await fetch(`${API_BASE}/api${url}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...options?.headers,
    },
  })

  const json = await response.json()

  if (!response.ok) {
    throw json
  }

  return {
    data: json,
    status: response.status,
  } as T
}
