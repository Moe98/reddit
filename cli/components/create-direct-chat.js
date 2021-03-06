const React = require('react')
const { Text, Box, useInput } = require('ink')
const { useContext } = require('react')
const ChatContext = require('../contexts/chat-context')
const { mapSpecialIdToId } = require('../utils/id-mapper')
const importJsx = require('import-jsx')
const AppContext = require('../contexts/app-context')
const TextInput = importJsx('./text-input')

const { useState } = React

const CreateDirectChat = ({ onBack }) => {
	const { authToken } = useContext(AppContext)
	const [chatContext, _] = useContext(ChatContext)
	const [otherUserId, setOtherUserId] = useState('')

	const onCreate = (targetUserId) => {
		chatContext.sendToChat({
			type: 'CREATE_DIRECT_CHAT',
			authToken,
			secondMember: mapSpecialIdToId(targetUserId)
		})
		onBack()
	}

	useInput((input, _) => {
		if (input.charCodeAt(0) === 60) {
			onBack()
		}
	})

	return (
		<React.Fragment>
			<Text bold>Create a DM</Text>
			<Box>
				<Box marginRight={1}>
					<Text>{'>'}</Text>
				</Box>
				<TextInput
					value={otherUserId}
					placeholder={`Write other user id`}
					onChange={setOtherUserId}
					onSubmit={onCreate}
				/>
			</Box>
			<Text bold>{`\nPress “<” to go back`}</Text>
		</React.Fragment>
	)
}

module.exports = CreateDirectChat
