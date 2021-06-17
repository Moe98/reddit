const jwtTokens = [
	'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJvcmcuc2FiIiwiZXhwIjo2MTYyMzUzMTA1MywidXNlcklkIjoiYWRhZmU4YjgtNzliMS00ODVhLWIxZjgtM2Y2N2MxMWI0YzZjIiwiaWF0IjoxNjIzNTMxMDUzLCJ1c2VybmFtZSI6InJvbmljIn0.TvLDUPQPb7-OwKClMIVxzs2rxIXrXqxmTO1v8Npfuq4aQ0iBSEBe6JZp_WYgRzIrPKFdbWzJbTP4TZ-MovwdSg',
	'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJvcmcuc2FiIiwiZXhwIjo2MTYyMzUzMTA1MywidXNlcklkIjoiNjU4ZjNlMGMtZDFkZi00MmMwLThjMDMtMDAxYjRhOGViNjMwIiwiaWF0IjoxNjIzNTMxMDUzLCJ1c2VybmFtZSI6Im91ZGEifQ.HL8kwF-ZTkYOMwAppJfzLmNRtFq6Z2TbWfiZ3q3Bo-zlMOfAmcaE9uDrs6a_sH4Ryy18VM3QtZwAC9aNm7eC1Q',
	'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJvcmcuc2FiIiwiZXhwIjo2MTYyMzUzMTA1MywidXNlcklkIjoiMmQyMDg5ZmYtYjVjNi00Njk1LWFlM2MtMWRmOTAxYzU4ZTNkIiwiaWF0IjoxNjIzNTMxMDUzLCJ1c2VybmFtZSI6ImpvZSJ9.20mxUyqsqzLSyXjbUYCDz-8nj0fWaAEX8DKaAwXUYxVzH5BvFxQwqOcFrusiv2i_8hr6XAlFBfD0qdTBhcCsNg',
	'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJvcmcuc2FiIiwiZXhwIjo2MTYyMzUzMTA1MywidXNlcklkIjoiNmI1NzU2MmItMzY2Ny00MjZjLWFlNmEtMzcyYzhlYTZmZjkxIiwiaWF0IjoxNjIzNTMxMDUzLCJ1c2VybmFtZSI6ImFidSJ9.t-fFHMBaSb_zHACcQ615auryJVSPP_HzlhrgDO4muT0bMq8RS0Fp4HfKieHSHZex1hxdOL0S63OlDhNcgYF94Q'
]

const getAuthToken = (id) => {
	switch (mapSpecialIdToId(id)) {
		case '6b57562b-3667-426c-ae6a-372c8ea6ff91':
			return jwtTokens[3]
		case '2d2089ff-b5c6-4695-ae3c-1df901c58e3d':
			return jwtTokens[2]
		case '658f3e0c-d1df-42c0-8c03-001b4a8eb630':
			return jwtTokens[1]
		case 'adafe8b8-79b1-485a-b1f8-3f67c11b4c6c':
			return jwtTokens[0]
		default:
			return id
	}
}

const mapIdToSpecialId = (id) => {
	switch (id) {
		case '6b57562b-3667-426c-ae6a-372c8ea6ff91':
			return 'abu'
		case '2d2089ff-b5c6-4695-ae3c-1df901c58e3d':
			return 'joe'
		case '658f3e0c-d1df-42c0-8c03-001b4a8eb630':
			return 'ouda'
		case 'adafe8b8-79b1-485a-b1f8-3f67c11b4c6c':
			return 'ronic'
		default:
			return id
	}
}

const mapSpecialIdToId = (id) => {
	switch (id) {
		case 'abu':
			return '6b57562b-3667-426c-ae6a-372c8ea6ff91'
		case 'joe':
			return '2d2089ff-b5c6-4695-ae3c-1df901c58e3d'
		case 'ouda':
			return '658f3e0c-d1df-42c0-8c03-001b4a8eb630'
		case 'ronic':
			return 'adafe8b8-79b1-485a-b1f8-3f67c11b4c6c'
		default:
			return id
	}
}

module.exports = { getAuthToken, mapSpecialIdToId, mapIdToSpecialId }
