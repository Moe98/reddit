const React = require('react')
const { Text, Box } = require('ink')
const { default: SelectInput } = require('ink-select-input')
const { useContext } = require('react')
const AppContext = require('../contexts/app-context')

const ChatsList = ({ directChats, groupChats, onChatSelect }) => {
	const handleSelect = (item) => onChatSelect(item.value)
	const { userId } = useContext(AppContext)

	let items = groupChats.map((chat) => ({
		key: chat.chatId,
		label: `${chat.name} (${chat.memberIds.join(', ')})`,
		value: chat
	}))

	items = items.concat(
		directChats.map((chat) => ({
			key: chat.chatId,
			label: `@${
				userId === chat.firstMember ? chat.secondMember : chat.firstMember
			}`,
			value: chat
		}))
	)
	
	return (
		<Box flexDirection='column'>
			<Text key={1} bold>
				Chat List
			</Text>
			{items.length > 0 ? (
				<SelectInput key={2} items={items} onSelect={handleSelect} />
			) : (
				<Text>You have no chats 🤷‍♂️</Text>
			)}
		</Box>
	)
}

module.exports = ChatsList
