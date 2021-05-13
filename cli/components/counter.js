const React = require('react')
const { Text } = require('ink')

const { useState, useEffect } = React

const Counter = () => {
	const [counter, setCounter] = useState(0)

	useEffect(() => {
		const timer = setInterval(() => {
			setCounter((previousCounter) => previousCounter + 1)
		}, 100)

		return () => {
			clearInterval(timer)
		}
	}, [])

	return <Text color='green'> x{counter}</Text>
}

module.exports = Counter
