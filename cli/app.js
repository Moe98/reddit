'use strict'
const React = require('react')
const importJsx = require('import-jsx')
const WelcomeFlow = importJsx('./flows/welcome-flow')
const ChatFlow = importJsx('./flows/chat-flow')

const AppContext = importJsx('./components/app-context')

const App = ({ command = 'welcome', rainbow }) => {
    const user = {
        authToken: null,
        username: 'Ronic',
        userId: '2'
    }
	return (
		<AppContext.Provider value={user}>
			{command.toLowerCase() === ('welcome') && <WelcomeFlow rainbow={rainbow} />}
			{command.toLowerCase() === ('chat') && <ChatFlow />}
		</AppContext.Provider>
	)
}

module.exports = App
