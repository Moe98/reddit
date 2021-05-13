const React = require('react')
const Gradient = require('ink-gradient')
const BigText = require('ink-big-text')

const WelcomeHeader = ({ rainbow, text }) => {
	return rainbow ? (
		<Gradient name='rainbow'>
			<BigText text={text} />
		</Gradient>
	) : (
		<BigText text={text} />
	)
}

module.exports = WelcomeHeader
