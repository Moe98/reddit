const React = require('react')
const { useState, useEffect, useContext } = require('react')
const { Text, Box, useInput } = require('ink')
const AppContext = require('../contexts/app-context')

const importJsx = require('import-jsx')
const TextInput = importJsx('./text-input')
const LoadingSpinner = importJsx('./loading-spinner')
const MessagesList = importJsx('./messages-list')

const ChatContext = require('../contexts/chat-context')
const { mapIdToSpecialId } = require('../utils/id-mapper')

const ChatView = ({ chat, onChatExit }) => {
	const { userId, authToken } = useContext(AppContext)
	const [chatContext, _] = useContext(ChatContext)
	const [newMessage, setNewMessage] = useState('')

	const onNewMessageSent = (messageText) => {
		if (messageText.length == 0) return
		const isAbleToSend = chatContext.sendToChat({
			authToken,
			type: chat.name ? 'CREATE_GROUP_MESSAGE' : 'CREATE_DIRECT_MESSAGE',
			chatId: chat.chatId,
			content: messageText
		})
		if (isAbleToSend) setNewMessage('')
		else setNewMessage(`Didn't connect to web socket`)
	}

	useEffect(() => {
		chatContext.sendToChat({
			type: chat.name ? 'GET_GROUP_MESSAGES' : 'GET_DIRECT_MESSAGES',
			chatId: chat.chatId,
			authToken
		})
	}, [])

	useInput((input, _) => {
		if (input.charCodeAt(0) === 60) {
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
							placeholder={`Chat with ${mapIdToSpecialId(
								chat.name
									? chat.name
									: userId == chat.firstMember
									? chat.secondMember
									: chat.firstMember
							)} here`}
							onChange={setNewMessage}
							onSubmit={onNewMessageSent}
						/>
					</Box>
					<Text bold>{`\nPress “<” to exit chat`}</Text>
				</Box>
			) : (
				<LoadingSpinner />
			)}
		</Box>
	)
}

module.exports = ChatView
