const React = require('react')
const { useState, useEffect, useContext } = require('react')
const { Text, Box, useInput } = require('ink')
const AppContext = require('./app-context')

const importJsx = require('import-jsx')
const TextInput = importJsx('./text-input')
const LoadingSpinner = importJsx('./loading-spinner')
const MessagesList = importJsx('./messages-list')

const useChatService = require('../hooks/chat-service')

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
	const [messages, setMessages] = useState([])
	const [newMessage, setNewMessage] = useState('')
	const username = useContext(AppContext).username

	const onNewFrameReceived = (frame) => {
		const receivedMessage = {
			id: messages.length + 1,
			text: frame.data,
			author: username,
			date: new Date()
		}
		setMessages([...messages, receivedMessage])
	}

	const onErrorReceived = (error) => {
		const sentMessage = {
			id: messages.length + 1,
			text: error.message,
			author: 'Error',
			date: new Date()
		}
		setMessages([...messages, sentMessage])
	}

	const sendToChat = useChatService(onNewFrameReceived, onErrorReceived)

	const onNewMessageSent = (messageText) => {
		const isAbleToSend = sendToChat(
			JSON.stringify({
				type: 'CreateGroupMessage',
				chatId: 'efb3c541-9ddb-44d6-aa47-e6f2579ea177',
				sender_id: 'ee55dcf8-ee7b-429a-939e-12c2f7b7ddee',
				content: messageText
			})
		)
		if (isAbleToSend) setNewMessage('')
		else setNewMessage(`Didn't connect to web socket`)
	}

	useEffect(() => {
		const timer = setTimeout(() => {
			setMessages(
				messagesData.filter((msg) => chat.membersList.includes(msg.author))
			)
		}, 750)

		return () => {
			clearTimeout(timer)
		}
	}, [])

	useInput((input, _) => {
		if (input === 'q') {
			onChatExit()
		}
	})

	return (
		<Box>
			{messages.length > 0 ? (
				<Box flexDirection='column'>
					<MessagesList messages={messages} />
					<Box>
						<Box marginRight={1}>
							<Text>{'>'}</Text>
						</Box>
						<TextInput
							value={newMessage}
							placeholder={`Chat with ${chat.name} here`}
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
