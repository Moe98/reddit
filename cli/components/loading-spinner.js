const React = require('react')
const { Text } = require('ink')
const { default: Spinner } = require('ink-spinner')

const LoadingSpinner = ({ loadingText = 'Loading' }) => (
	<Text>
		<Text color='green'>
			<Spinner type='dots' />
		</Text>
		{' ' + loadingText}
	</Text>
)

module.exports = LoadingSpinner
