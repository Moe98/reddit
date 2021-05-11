const React = require('react')
const { Text, Box, useInput } = require('ink')
const { useContext } = require('react')
const ChatContext = require('../contexts/chat-context')
const importJsx = require('import-jsx')
const AppContext = require('../contexts/app-context')
const TextInput = importJsx('./text-input')

const { useState } = React

const CreateGroupChat = ({ onBack }) => {
	const { userId } = useContext(AppContext)
	const [chatContext, _] = useContext(ChatContext)
	const [chatName, setChatName] = useState('')

	const onCreate = (chatName) => {
		chatContext.sendToChat({
			type: 'CREATE_GROUP_CHAT',
			creator: userId,
			name: chatName,
			description: ''
		})
		onBack()
	}

	useInput((input, _) => {
		if (input.charCodeAt(0) === 339) {
			onBack()
		}
	})

	return (
		<React.Fragment>
			<Text bold>Create a Group Chat</Text>
			<Box>
				<Box marginRight={1}>
					<Text>{'>'}</Text>
				</Box>
				<TextInput
					value={chatName}
					placeholder={`Enter chat name`}
					onChange={setChatName}
					onSubmit={onCreate}
				/>
			</Box>
			<Text bold>{`\nPress “ALT + Q” to go back`}</Text>
		</React.Fragment>
	)
}

module.exports = CreateGroupChat
