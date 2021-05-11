const W3CWebSocket = require('websocket').w3cwebsocket
const { useEffect, useContext, useRef } = require('react')
const AppContext = require('../contexts/app-context')
const ChatContext = require('../contexts/chat-context')

const useChatService = () => {
	const ws = useRef(null)
	const { userId } = useContext(AppContext)
	const [chatContext, setChatContext] = useContext(ChatContext)

	const onNewFrameReceived = (frame) => {
		frame = JSON.parse(frame.data)
		const frameType = frame.type
		const data = frame.data

		const isTargetMember = (id) => data.targetMemberId === id
		const isGroupAdmin = (groupChat, id) => groupChat.adminId === id

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
				messages[data.chatId] = [...data.messages.reverse()]
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
			case 'CREATE_DIRECT_CHAT':
				const newDirectChat = {
					chatId: data.chatId,
					firstMember: data.firstMember,
					secondMember: data.secondMember
				}
				setChatContext({
					...chatContext,
					directChats: [...chatContext.directChats, newDirectChat]
				})
				break
			case 'CREATE_GROUP_CHAT':
				const newGroupChat = {
					chatId: data.chatId,
					name: data.name,
					description: data.description,
					adminId: data.adminId,
					dateCreated: data.dateCreated,
					memberIds: data.memberIds
				}
				setChatContext({
					...chatContext,
					groupChats: [...chatContext.groupChats, newGroupChat]
				})
				break
			case 'LEAVE_GROUP':
				const isUserRemoved =
					isGroupAdmin(data.targetMemberId) || isTargetMember(userId)
				const groupChatsAfterLeave = chatContext.groupChats.filter(
					(chat) => chat.chatId !== data.chatId
				)
				setChatContext({
					...chatContext,
					groupChats: isUserRemoved
						? groupChatsAfterLeave
						: chatContext.groupChats,
					selectedChat:
						isUserRemoved && chatContext.selectedChat === data.chatId
							? null
							: chatContext.selectedChat
				})
				break
			case 'REMOVE_GROUP_MEMBER':
				const groupChatsAfterRemove = chatContext.groupChats.filter(
					(chat) => chat.chatId !== data.chatId
				)
				setChatContext({
					...chatContext,
					groupChats: isTargetMember(userId)
						? groupChatsAfterRemove
						: chatContext.groupChats,
					selectedChat:
						isTargetMember(userId) && chatContext.selectedChat === data.chatId
							? null
							: chatContext.selectedChat
				})
				break
			case 'ADD_GROUP_MEMBER':
				const groupChatAddedTo = {
					chatId: data.chatId,
					name: data.name,
					description: data.description,
					adminId: data.adminId,
					dateCreated: data.dateCreated,
					memberIds: data.memberIds
				}
				setChatContext({
					...chatContext,
					groupChats: isTargetMember(userId)
						? [...chatContext.groupChats, groupChatAddedTo]
						: chatContext.groupChats
				})
				break
			case 'ERROR':
				console.log(frame.msg)
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
