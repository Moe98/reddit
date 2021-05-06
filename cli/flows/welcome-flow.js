'use strict'
const React = require('react')
const { Text, Static } = require('ink')
const importJsx = require('import-jsx')

const WelcomeHeader = importJsx('../components/welcome-header.js')
const Counter = importJsx('../components/counter')

const WelcomeFlow = ({ rainbow }) => (
	<React.Fragment>
		<Static items={[{ id: 1, text: 'Reddit CLI' }]}>
			{(item) => (
				<WelcomeHeader key={item.id} rainbow={rainbow} text={item.text} />
			)}
		</Static>
		<Text>
			Hello, <Text color='green'>Stranger</Text>
			<Counter />
		</Text>
	</React.Fragment>
)

module.exports = WelcomeFlow
