'use strict'
const React = require('react')
const importJsx = require('import-jsx')
const ChatContext = require('./contexts/chat-context')
const { useState } = require('react')
const { getAuthToken, mapSpecialIdToId } = require('./utils/id-mapper')
const WelcomeFlow = importJsx('./flows/welcome-flow')
const ChatFlow = importJsx('./flows/chat-flow')

const AppContext = importJsx('./contexts/app-context')

const defaultChatContext = {
	isLoadingChats: true,
	isLoadingMessages: true,
	highlightedChat: null,
	directChats: [],
	groupChats: [],
	selectedChat: null,
	messages: {},
	sendToChat: null
}

const App = ({ command = 'welcome', flags }) => {
	const [chatContext, setChatContext] = useState(defaultChatContext)

	const { rainbow, user = '6b57562b-3667-426c-ae6a-372c8ea6ff91' } = flags

	const defaultAppContext = {
		authToken: getAuthToken(user),
		userId: mapSpecialIdToId(user)
	}

	return (
		<AppContext.Provider value={defaultAppContext}>
			{command.toLowerCase() === 'welcome' && <WelcomeFlow rainbow={rainbow} />}
			<ChatContext.Provider value={[chatContext, setChatContext]}>
				{command.toLowerCase() === 'chat' && <ChatFlow />}
			</ChatContext.Provider>
		</AppContext.Provider>
	)
}

module.exports = App
