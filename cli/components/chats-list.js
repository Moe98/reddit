const React = require('react')
const { default: importJsx } = require('import-jsx')
const { Text, Box, useInput } = require('ink')
const { default: SelectInput } = require('ink-select-input')
const { useContext, useState } = require('react')

const AppContext = require('../contexts/app-context')
const { mapIdToSpecialId } = require('../utils/id-mapper')
const ChatContext = require('../contexts/chat-context')
const AddGroupMember = importJsx('./add-group-member')
const RemoveGroupMember = importJsx('./remove-group-member')
const CreateDirectChat = importJsx('./create-direct-chat')
const CreateGroupChat = importJsx('./create-group-chat')

const CHAT_LIST = 0
const CREATING_DIRECT_CHAT = 1
const CREATING_GROUP_CHAT = 2
const ADDING_GROUP_MEMBER = 3
const REMOVING_GROUP_MEMBER = 4

const ChatsList = ({ onChatSelect }) => {
	const { userId } = useContext(AppContext)
	const [chatContext, setChatContext] = useContext(ChatContext)
	const [status, setStatus] = useState(CHAT_LIST)

	const handleSelect = (item) => onChatSelect(item.value)

	const isChatListEmpty = () =>
		chatContext.groupChats.length + chatContext.directChats.length === 0

	const mapDirectChatToItem = (chat) => ({
		key: chat.chatId,
		label: `@${mapIdToSpecialId(
			userId === chat.firstMember ? chat.secondMember : chat.firstMember
		)}`,
		value: chat
	})

	const mapGroupChatToItem = (chat) => ({
		key: chat.chatId,
		label: `${chat.name} (${chat.memberIds.map(mapIdToSpecialId).join(', ')})`,
		value: chat
	})

	const items = chatContext.groupChats
		.map(mapGroupChatToItem)
		.concat(chatContext.directChats.map(mapDirectChatToItem))
		.sort((a, b) => {
			if (a.label === b.label) return 0
			if (a.label < b.label) return -1
			return 1
		})

	const [highlightedChat, setHighlightedChat] = useState(
		!isChatListEmpty() ? items[0].value : null
	)
	const handleHighlight = (item) => setHighlightedChat(item.value)

	const onBack = () => setStatus(CHAT_LIST)

	useInput((input, _) => {
		if (input === 'd' && status === CHAT_LIST) {
			setStatus(CREATING_DIRECT_CHAT)
		} else if (input === 'g' && status === CHAT_LIST) {
			setStatus(CREATING_GROUP_CHAT)
		} else if (input === 'a' && status === CHAT_LIST) {
			setChatContext({ ...chatContext, highlightedChat })
			setStatus(ADDING_GROUP_MEMBER)
		} else if (input === 'r' && status === CHAT_LIST) {
			setChatContext({ ...chatContext, highlightedChat })
			setStatus(REMOVING_GROUP_MEMBER)
		} else if (input === 'l' && highlightedChat && highlightedChat.name) {
			chatContext.sendToChat({
				type: 'LEAVE_GROUP',
				chatId: highlightedChat.chatId,
				userId
			})
			setHighlightedChat(!isChatListEmpty() ? items[0].value : null)
		}
	})

	return (
		<React.Fragment>
			{status === CHAT_LIST ? (
				<Box margin={1} flexDirection='column'>
					<Text key={1} bold>
						Chat List
					</Text>
					{!isChatListEmpty() ? (
						<SelectInput
							key={2}
							items={items}
							onSelect={handleSelect}
							onHighlight={handleHighlight}
						/>
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
						{highlightedChat && highlightedChat.adminId === userId && (
							<Text>A - Add member to group</Text>
						)}
						{highlightedChat && highlightedChat.adminId === userId && (
							<Text>R - Remove member to group</Text>
						)}
						{highlightedChat && highlightedChat.name && (
							<Text>L - Leave selected chat</Text>
						)}
					</Box>
				</Box>
			) : status === CREATING_DIRECT_CHAT ? (
				<CreateDirectChat onBack={onBack} />
			) : status === CREATING_GROUP_CHAT ? (
				<CreateGroupChat onBack={onBack} />
			) : status === ADDING_GROUP_MEMBER ? (
				<AddGroupMember onBack={onBack} />
			) : (
				<RemoveGroupMember onBack={onBack} />
			)}
		</React.Fragment>
	)
}

module.exports = ChatsList
