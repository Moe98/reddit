'use strict'
const React = require('react')
const importJsx = require('import-jsx')
const ChatContext = require('../contexts/chat-context')
const { useEffect, useContext } = require('react')
const useChatService = require('../hooks/chat-service')
const LoadingSpinner = importJsx('../components/loading-spinner')
const ChatsList = importJsx('../components/chats-list')
const ChatView = importJsx('../components/chat-view')

const { Text } = require('ink')

const chatsData = [
	{
		id: '166fff75-d0f6-4fa9-951a-4a58c696397b',
		name: 'Alpha Squad',
		membersList: ['Ouda', 'Joe', 'Bulleil', 'Ronic']
	},
	{
		id: '166fff75-d0f6-4fa9-951a-4a58c696397a',
		name: 'Hamada Security',
		membersList: ['Joe', 'Bulleil', 'Ronic']
	},
	{
		id: '166fff75-d0f6-4fa9-951a-4a58c696397c',
		name: 'Cheating Case',
		membersList: ['Ouda', 'Ronic']
	}
]

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
						<ChatsList
							groupChats={chatContext.groupChats}
							directChats={chatContext.directChats}
							onChatSelect={onChatSelect}
						/>
					)}
				</React.Fragment>
			) : (
				<LoadingSpinner />
			)}
		</React.Fragment>
	)
}

module.exports = ChatFlow
