const React = require('react')
const { Text, Box, Spacer } = require('ink')
const { useContext } = require('react')
const AppContext = require('../contexts/app-context')

const moment = require('moment')

const Message = ({ content, senderId, timestamp }) => {
	const { userId } = useContext(AppContext)
	const isYourMessage = senderId === userId
	return (
		<Box alignItems='flex-start' width='100%'>
			<Box>
				<Text color={isYourMessage ? 'green' : 'blue'}>
					{(isYourMessage ? 'You' : mapIdToSpecialId(senderId)) + ': '}
				</Text>
			</Box>
			<Box>
				<Text>{content}</Text>
			</Box>
			<Spacer />
			<Box>
				<Text color='grey'>{` (${moment(timestamp).calendar()})`}</Text>
			</Box>
		</Box>
	)
}

module.exports = Message
