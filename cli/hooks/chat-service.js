const W3CWebSocket = require('websocket').w3cwebsocket
const { useEffect, useContext, useRef } = require('react')
const AppContext = require('../components/app-context')

const useChatService = (onMessage, onError) => {
	const ws = useRef(null)
	const { userId } = useContext(AppContext)

	useEffect(() => {
		ws.current = new W3CWebSocket('ws://localhost:5000/ws')
		ws.current.onopen = () => {
			ws.current.send(
				JSON.stringify({
					type: 'INIT_CONNECTION',
					userId
				})
			)
		}

		return () => ws.current.close()
	}, [])

	if (ws.current) {
		ws.current.onmessage = onMessage
		ws.current.onerror = onError
	}

	const send = (message) => {
		if (!ws.current || ws.current.readyState != 1) return false
		ws.current.send(message)
		return true
	}

	return send
}

module.exports = useChatService
