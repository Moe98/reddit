const React = require('react')
const { Text } = require('ink')

const ChatView = ({ chat }) => {
	return <Text >{`Joined ${chat.name}`}</Text>
}

module.exports = ChatView
