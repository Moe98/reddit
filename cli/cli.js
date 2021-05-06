#!/usr/bin/env node
'use strict'
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
		--rainbow, -r  Add rainbow welcome title

	Examples
	  $ reddit-cli welcome -r
`,
	{
		flags: {
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
		rainbow: cli.flags.rainbow
	})
)
