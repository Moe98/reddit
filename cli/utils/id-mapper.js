const mapIdToSpecialId = (id) => {
	switch (id) {
		case 'ee55dcf8-ee7b-429a-939e-12c2f7b7ddaa':
			return 'abu'
		case 'ee55dcf8-ee7b-429a-939e-12c2f7b7ddbb':
			return 'joe'
		case 'ee55dcf8-ee7b-429a-939e-12c2f7b7ddcc':
			return 'ouda'
		case 'ee55dcf8-ee7b-429a-939e-12c2f7b7ddee':
			return 'ronic'
		default:
			return id
	}
}

const mapSpecialIdToId = (id) => {
	switch (id) {
		case 'abu':
			return 'ee55dcf8-ee7b-429a-939e-12c2f7b7ddaa'
		case 'joe':
			return 'ee55dcf8-ee7b-429a-939e-12c2f7b7ddbb'
		case 'ouda':
			return 'ee55dcf8-ee7b-429a-939e-12c2f7b7ddcc'
		case 'ronic':
			return 'ee55dcf8-ee7b-429a-939e-12c2f7b7ddee'
		default:
			return id
	}
}

module.exports = { mapSpecialIdToId, mapIdToSpecialId }
