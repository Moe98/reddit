#!/usr/bin/env node
'use strict'
process.env.NODE_ENV = 'production';

const React = require('react')
const importJsx = require('import-jsx')
const { render } = require('ink')
const meow = require('meow')

const App = importJsx('./app')


const cli = meow(
	`
	Usage
	  $ reddit-cli <command>

	Commands
	  $ reddit-cli welcome
	  $ reddit-cli chat

	Flags
		--user, -u specify the user id. Special user ids are joe, ouda, abu and ronic.
		--rainbow, -r  Add rainbow welcome title

	Examples
	  $ reddit-cli chat -u ee55dcf8-ee7b-429a-939e-12c2f7b7ddee
	  $ reddit-cli chat -u abu
	  $ reddit-cli welcome -r
`,
	{
		flags: {
			user: {
				type: 'string',
				alias: 'u'
			},
			rainbow: {
				type: 'boolean',
				alias: 'r'
			}
		}
	}
)

render(
	React.createElement(App, {
		command: cli.input[0],
		flags: {
			user: cli.flags.user,
			rainbow: cli.flags.rainbow
		}
	})
)
