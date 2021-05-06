const React = require('react')
const { Text, Box, Spacer } = require('ink')
const { useContext } = require('react')
const AppContext = require('./app-context')

const moment = require('moment')

const Message = ({ text, author, date }) => {
	const username = useContext(AppContext).username
	const isYourMessage = author === username
	return (
		<Box alignItems='flex-start' width='100%'>
			<Box>
				<Text color={isYourMessage ? 'green' : 'blue'}>
					{(isYourMessage ? 'You' : author) + ': '}
				</Text>
			</Box>
			<Box>
				<Text>{text}</Text>
			</Box>
			<Spacer />
			<Box>
				<Text color='grey'>{` (${moment(date).calendar()})`}</Text>
			</Box>
		</Box>
	)
}

module.exports = Message
