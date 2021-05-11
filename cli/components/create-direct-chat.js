const React = require('react')
const { Text, Box } = require('ink')
const { useContext } = require('react')
const ChatContext = require('../contexts/chat-context')
const { mapSpecialIdToId } = require('../utils/id-mapper')
const importJsx = require('import-jsx')
const AppContext = require('../contexts/app-context')
const TextInput = importJsx('./text-input')

const { useState } = React

const CreateDirectChat = ({ onBack }) => {
	const { userId } = useContext(AppContext)
	const [chatContext, _] = useContext(ChatContext)
	const [otherUserId, setOtherUserId] = useState('')

	const onCreate = (targetUserId) => {
		chatContext.sendToChat({
			type: 'CREATE_DIRECT_CHAT',
			firstMember: userId,
			secondMember: mapSpecialIdToId(targetUserId)
		})
		onBack()
	}

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
		</React.Fragment>
	)
}

module.exports = CreateDirectChat
