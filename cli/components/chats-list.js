const React = require('react')
const { default: importJsx } = require('import-jsx')
const { Text, Box, useInput } = require('ink')
const { default: SelectInput } = require('ink-select-input')
const { useContext, useState } = require('react')

const AppContext = require('../contexts/app-context')
const { mapIdToSpecialId } = require('../utils/id-mapper')
const ChatContext = require('../contexts/chat-context')
const CreateDirectChat = importJsx('./create-direct-chat')
const CreateGroupChat = importJsx('./create-group-chat')

const CHAT_LIST = 0
const CREATING_DIRECT_CHAT = 1
const CREATING_GROUP_CHAT = 2

const ChatsList = ({ onChatSelect }) => {
	const handleSelect = (item) => onChatSelect(item.value)
	const { userId } = useContext(AppContext)
	const [chatContext, _] = useContext(ChatContext)
	const [status, setStatus] = useState(CHAT_LIST)

	let items = chatContext.groupChats.map((chat) => ({
		key: chat.chatId,
		label: `${chat.name} (${chat.memberIds.map(mapIdToSpecialId).join(', ')})`,
		value: chat
	}))

	items = items.concat(
		chatContext.directChats.map((chat) => ({
			key: chat.chatId,
			label: `@${mapIdToSpecialId(
				userId === chat.firstMember ? chat.secondMember : chat.firstMember
			)}`,
			value: chat
		}))
	)

	const onBack = () => setStatus(CHAT_LIST)

	useInput((input, _) => {
		if (input === 'd') {
			setStatus(CREATING_DIRECT_CHAT)
		} else if (input === 'g') {
			setStatus(CREATING_GROUP_CHAT)
		}
	})

	return (
		<React.Fragment>
			{status === CHAT_LIST ? (
				<Box margin={1} flexDirection='column'>
					<Text key={1} bold>
						Chat List
					</Text>
					{items.length > 0 ? (
						<SelectInput key={2} items={items} onSelect={handleSelect} />
					) : (
						<Text>You have no chats 🤷‍♂️</Text>
					)}
					<Box
						width={30}
						paddingLeft={1}
						paddingRight={1}
						flexDirection='column'
						borderStyle='bold'
					>
						<Text underline>Controls</Text>
						<Text>D - Create direct chat</Text>
						<Text>G - Create Group chat</Text>
						<Text>L - Leave selected chat</Text>
					</Box>
				</Box>
			) : status === CREATING_DIRECT_CHAT ? (
				<CreateDirectChat onBack={onBack} />
			) : (
				<CreateGroupChat />
			)}
		</React.Fragment>
	)
}

module.exports = ChatsList
