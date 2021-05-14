'use strict'
const React = require('react')
const importJsx = require('import-jsx')
const ChatContext = require('./contexts/chat-context')
const { useState } = require('react')
const { mapSpecialIdToId } = require('./utils/id-mapper')
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

	const { rainbow, user = 'ee55dcf8-ee7b-429a-939e-12c2f7b7ddee' } = flags

	const defaultAppContext = {
		authToken: null,
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
