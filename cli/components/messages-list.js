const React = require('react')
const { Box } = require('ink')

const importJsx = require('import-jsx')

const Message = importJsx('./message')

const MessagesList = ({ messages }) => {
	return (
		<Box flexDirection='column'>
			{messages.map((message) => (
				<Message key={message.timestamp} {...message} />
			))}
		</Box>
	)
}

module.exports = MessagesList
