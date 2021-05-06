'use strict'
const React = require('react')
const importJsx = require('import-jsx')
const LoadingSpinner = importJsx('../components/loading-spinner')
const ChatsList = importJsx('../components/chats-list')
const ChatView = importJsx('../components/chat-view')


const { useState, useEffect } = React

const chatsData = [
	{
		name: 'Alpha Squad',
		membersList: ['Ouda', 'Joe', 'Bulleil', 'Ronic']
	},
	{
		name: 'Hamada Security',
		membersList: ['Joe', 'Bulleil', 'Ronic']
	},
	{
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
		}, 2000)

		return () => {
			clearTimeout(timer)
		}
	}, [])

	const onChatSelect = (chat) => setSelectedChat(chat)
	return (
		<React.Fragment>
			{chats.length > 0 ? (
				<React.Fragment>
					{selectedChat ? (
						<ChatView chat={selectedChat} />
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
