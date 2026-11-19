import { useEffect, useRef, useState } from 'react'

// Loaded from CDN so we don't need extra build tooling for this one feature.
const SOCKJS_SRC = 'https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.6.1/sockjs.min.js'
const STOMP_SRC = 'https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js'

function loadScript(src) {
  return new Promise((resolve, reject) => {
    if (document.querySelector(`script[src="${src}"]`)) return resolve()
    const script = document.createElement('script')
    script.src = src
    script.onload = resolve
    script.onerror = reject
    document.body.appendChild(script)
  })
}

/**
 * Subscribes to /user/queue/leaderboard and calls onRankChange(message) whenever a
 * "you moved from #X to #Y" push arrives (see backend RealtimeLeaderboardService).
 * No-ops gracefully if the token is missing or the scripts fail to load.
 */
export function useLeaderboardSocket(onRankChange) {
  const clientRef = useRef(null)
  const [connected, setConnected] = useState(false)

  useEffect(() => {
    const token = localStorage.getItem('ecoquest_token')
    if (!token) return

    let cancelled = false

    async function connect() {
      try {
        await loadScript(SOCKJS_SRC)
        await loadScript(STOMP_SRC)
        if (cancelled) return

        const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'
        const socket = new window.SockJS(`${apiUrl}/ws?token=${encodeURIComponent(token)}`)
        const stompClient = window.Stomp.over(socket)
        stompClient.debug = null

        stompClient.connect(
          {},
          () => {
            if (cancelled) return
            setConnected(true)
            stompClient.subscribe('/user/queue/leaderboard', (frame) => {
              const message = JSON.parse(frame.body)
              onRankChange?.(message)
            })
          },
          () => setConnected(false)
        )

        clientRef.current = stompClient
      } catch {
        // CDN unreachable or blocked - real-time updates just won't be available this session
      }
    }

    connect()

    return () => {
      cancelled = true
      clientRef.current?.disconnect?.()
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  return { connected }
}
