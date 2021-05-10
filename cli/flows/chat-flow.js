'use strict'
const React = require('react')
const importJsx = require('import-jsx')
const LoadingSpinner = importJsx('../components/loading-spinner')
const ChatsList = importJsx('../components/chats-list')
const ChatView = importJsx('../components/chat-view')

const { useState, useEffect } = React

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
	const [chats, setChats] = useState([])
	const [selectedChat, setSelectedChat] = useState(null)

	useEffect(() => {
		const timer = setTimeout(() => {
			setChats(chatsData)
		}, 1000)

		return () => {
			clearTimeout(timer)
		}
	}, [])

	const onChatSelect = (chat) => setSelectedChat(chat)
	const onChatExit = () => setSelectedChat(null)

	return (
		<React.Fragment>
			{chats.length > 0 ? (
				<React.Fragment>
					{selectedChat ? (
						<ChatView chat={selectedChat} onChatExit={onChatExit} />
					) : (
						<ChatsList chats={chats} onChatSelect={onChatSelect} />
					)}
				</React.Fragment>
			) : (
				<LoadingSpinner />
			)}
		</React.Fragment>
	)
}

module.exports = ChatFlow
