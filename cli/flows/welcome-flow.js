'use strict'
const React = require('react')
const { Text } = require('ink')
const importJsx = require('import-jsx')

const Counter = importJsx('../components/counter')

const WelcomeFlow = (props) => (
	<Text>
		Hello, <Text color='green'>{props.name}</Text>
		<Counter />
	</Text>
)

module.exports = WelcomeFlow
