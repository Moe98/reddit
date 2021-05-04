'use strict'
const React = require('react')
const importJsx = require('import-jsx')

const WelcomeFlow = importJsx('./flows/welcome-flow')

const App = (props) => <WelcomeFlow {...props} />

module.exports = App
