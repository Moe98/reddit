const React = require('react')
const { useState, useEffect, useContext } = require('react')
const { Text, Box, useInput } = require('ink')
const AppContext = require('../contexts/app-context')

const importJsx = require('import-jsx')
const TextInput = importJsx('./text-input')
const LoadingSpinner = importJsx('./loading-spinner')
const MessagesList = importJsx('./messages-list')

const ChatContext = require('../contexts/chat-context')

const messagesData = [
	{
		id: 1,
		author: 'Ronic',
		text: 'Hello Everyone 👋\nWelcome to the group',
		date: new Date('May 7, 2021 00:00:05')
	},
	{
		id: 2,
		author: 'Bulleil',
		text: 'Hi',
		date: new Date('May 7, 2021 00:01:05')
	},
	{
		id: 3,
		author: 'Ouda',
		text: 'Hey guys',
		date: new Date('May 7, 2021 00:01:10')
	},
	{
		id: 4,
		author: 'Joe',
		text: 'Welcome',
		date: new Date('May 7, 2021 00:01:20')
	},
	{
		id: 5,
		author: 'Ronic',
		text: 'This project will end in one sitting',
		date: new Date('May 7, 2021 00:03:00')
	},
	{
		id: 6,
		author: 'Ouda',
		text: '🔥🔥🔥',
		date: new Date('May 7, 2021 00:03:20')
	},
	{
		id: 7,
		author: 'Bulleil',
		text: 'Inshaallah Yaba 🔥🔥',
		date: new Date('May 7, 2021 00:03:50')
	},
	{
		id: 8,
		author: 'Joe',
		text: 'Mandeal',
		date: new Date('May 7, 2021 00:04:05')
	}
]

const ChatView = ({ chat, onChatExit }) => {
	const { userId } = useContext(AppContext)
	const [chatContext, _] = useContext(ChatContext)
	const [newMessage, setNewMessage] = useState('')

	const onNewMessageSent = (messageText) => {
		if (messageText.length == 0) return
		const isAbleToSend = chatContext.sendToChat({
			type: chat.name ? 'CREATE_GROUP_MESSAGE' : 'CREATE_DIRECT_MESSAGE',
			chatId: chat.chatId,
			senderId: userId,
			content: messageText
		})
		if (isAbleToSend) setNewMessage('')
		else setNewMessage(`Didn't connect to web socket`)
	}

	useEffect(() => {
		chatContext.sendToChat({
			type: chat.name ? 'GET_GROUP_MESSAGES' : 'GET_DIRECT_MESSAGES',
			chatId: chat.chatId,
			userId
		})
	}, [])

	useInput((input, _) => {
		if (input === 'q') {
			onChatExit()
		}
	})

	return (
		<Box>
			{!chatContext.isLoadingMessages ? (
				<Box flexDirection='column'>
					<MessagesList messages={chatContext.messages[chat.chatId]} />
					<Box>
						<Box marginRight={1}>
							<Text>{'>'}</Text>
						</Box>
						<TextInput
							value={newMessage}
							placeholder={`Chat with ${
								chat.name
									? chat.name
									: userId == chat.firstMember
									? chat.secondMember
									: chat.firstMember
							} here`}
							onChange={setNewMessage}
							onSubmit={onNewMessageSent}
						/>
					</Box>
					<Text bold>{`\nPress “q” to exit chat`}</Text>
				</Box>
			) : (
				<LoadingSpinner />
			)}
		</Box>
	)
}

module.exports = ChatView
