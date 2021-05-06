const React = require('react')
const { Text, Box } = require('ink')
const { default: SelectInput } = require('ink-select-input')

const ChatsList = ({ chats, onChatSelect }) => {
	const handleSelect = (item) => onChatSelect(item.value)

	let items = chats.map((chat) => ({
		key: chat.name,
		label: `${chat.name} (${chat.membersList.join(', ')})`,
		value: chat
	}))

	return (
		<Box flexDirection='column'>
			<Text key={1} bold>
				Chat List
			</Text>
			<SelectInput key={2} items={items} onSelect={handleSelect} />
		</Box>
	)
}

module.exports = ChatsList
