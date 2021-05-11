const W3CWebSocket = require('websocket').w3cwebsocket
const { useEffect, useContext, useRef } = require('react')
const AppContext = require('../contexts/app-context')
const ChatContext = require('../contexts/chat-context')

const fs = require('fs')

const useChatService = () => {
	const ws = useRef(null)
	const { userId } = useContext(AppContext)
	const [chatContext, setChatContext] = useContext(ChatContext)

	const onNewFrameReceived = (frame) => {
		frame = JSON.parse(frame.data)
		const frameType = frame.type
		const data = frame.data

		let messages = {}
		switch (frameType) {
			case 'INIT_CONNECTION':
				setChatContext({
					...chatContext,
					isLoadingChats: false,
					groupChats: data.groupChats,
					directChats: data.directChats
				})
				break
			case 'GET_DIRECT_MESSAGES':
			case 'GET_GROUP_MESSAGES':
				messages = { ...chatContext.messages }
				messages[data.chatId] = [...data.messages]
				setChatContext({
					...chatContext,
					isLoadingMessages: false,
					messages
				})
				break
			case 'CREATE_DIRECT_MESSAGE':
			case 'CREATE_GROUP_MESSAGE':
				const newMessage = {
					chatId: data.chatId,
					timestamp: data.timestamp,
					senderId: data.senderId,
					content: data.content
				}
				messages = { ...chatContext.messages }
				messages[data.chatId] = messages[data.chatId]
					? [...messages[data.chatId], newMessage]
					: [newMessage]
				setChatContext({
					...chatContext,
					messages
				})
				break
		}
	}

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

		ws.current.onerror = (error) => console.log(error.message)

		return () => ws.current.close()
	}, [])

	if (ws.current) ws.current.onmessage = onNewFrameReceived

	const send = (message) => {
		if (!ws.current || ws.current.readyState != 1) return false
		ws.current.send(JSON.stringify(message))
		return true
	}

	return send
}

module.exports = useChatService
