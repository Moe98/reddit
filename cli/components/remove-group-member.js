const React = require('react')
const { Text, Box, useInput } = require('ink')
const { useContext } = require('react')
const ChatContext = require('../contexts/chat-context')
const importJsx = require('import-jsx')
const AppContext = require('../contexts/app-context')
const { mapSpecialIdToId } = require('../utils/id-mapper')
const TextInput = importJsx('./text-input')

const { useState } = React

const RemoveGroupMember = ({ onBack }) => {
	const { userId } = useContext(AppContext)
	const [chatContext, _] = useContext(ChatContext)
	const [memberId, setMemberId] = useState('')

    const chat = chatContext.highlightedChat

	const onAction = (memberId) => {
        chatContext.sendToChat({
            type: 'REMOVE_GROUP_MEMBER',
            chatId: chat.chatId,
            adminId: userId,
            memberId: mapSpecialIdToId(memberId)
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
			<Text bold>{`Remove a group member from ${chat.name}`}</Text>
			<Box>
				<Box marginRight={1}>
					<Text>{'>'}</Text>
				</Box>
				<TextInput
					value={memberId}
					placeholder={`Enter member id`}
					onChange={setMemberId}
					onSubmit={onAction}
				/>
			</Box>
            <Text bold>{`\nPress “ALT + Q” to go back`}</Text>
		</React.Fragment>
	)
}

module.exports = RemoveGroupMember
