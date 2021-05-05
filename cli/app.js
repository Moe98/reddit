'use strict'
const React = require('react')
const importJsx = require('import-jsx')

const WelcomeFlow = importJsx('./flows/welcome-flow')
const ChatFlow = importJsx('./flows/chat-flow')

const App = ({ command = 'welcome', rainbow }) => {
	return (
		<React.Fragment>
			{command.toLowerCase() === ('welcome') && <WelcomeFlow rainbow={rainbow} />}
			{command.toLowerCase() === ('chat') && <ChatFlow />}
		</React.Fragment>
	)
}

module.exports = App
