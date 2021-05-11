const React = require('react')
const { Box, Static } = require('ink')

const importJsx = require('import-jsx')

const Message = importJsx('./message')

const MessagesList = ({ messages }) => {
	return (
		<Box>
			<Static items={messages}>
				{(message) => <Message key={message.timestamp} {...message} />}
			</Static>
		</Box>
	)
}

module.exports = MessagesList
