'use strict'
const React = require('react')

const defaultUser = {
	authToken: null,
	username: 'Joe',
	userId: '1'
}

const AppContext = React.createContext(defaultUser)

module.exports = AppContext
