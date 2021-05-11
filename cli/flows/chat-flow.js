'use strict'
const React = require('react')
const importJsx = require('import-jsx')
const ChatContext = require('../contexts/chat-context')
const { useEffect, useContext } = require('react')
const useChatService = require('../hooks/chat-service')
const LoadingSpinner = importJsx('../components/loading-spinner')
const ChatsList = importJsx('../components/chats-list')
const ChatView = importJsx('../components/chat-view')

const ChatFlow = () => {
	const [chatContext, setChatContext] = useContext(ChatContext)

	const sendToChat = useChatService()

	useEffect(() => setChatContext({ ...chatContext, sendToChat }), [])

	const onChatSelect = (chat) => {
		setChatContext({
			...chatContext,
			selectedChat: chat,
			isLoadingMessages: true
		})
	}
	const onChatExit = () => {
		setChatContext({ ...chatContext, selectedChat: null })
	}

	return (
		<React.Fragment>
			{!chatContext.isLoadingChats ? (
				<React.Fragment>
					{chatContext.selectedChat ? (
						<ChatView chat={chatContext.selectedChat} onChatExit={onChatExit} />
					) : (
						<ChatsList onChatSelect={onChatSelect} />
					)}
				</React.Fragment>
			) : (
				<LoadingSpinner />
			)}
		</React.Fragment>
	)
}

module.exports = ChatFlow
