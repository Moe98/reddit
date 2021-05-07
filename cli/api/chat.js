// function log(msg) {
//     var log = $('#log')
//     log.append(msg + " \n").scrollTop(log[0].scrollHeight - log.height());
// }

// $(function () {
//     $('#url').val((location.protocol.indexOf('https') == -1 ? 'ws://' : 'wss://') + location.host + '/ws')

//     if (!WebSocket) {
//         $('#not-supported').show()
//     } else {
//         var ws = null
//         $('#connect').click(function () {
//             if (ws == null || ws.readyState != 1) {
//                 ws = new WebSocket($('#url').val())

//                 ws.onerror = function (e) {
//                     log('Error : ' + e.message)
//                 }

//                 ws.onopen = function () {
//                     log('connected')
//                 }

//                 ws.onclose = function () {
//                     log('disconnected')
//                 }

//                 ws.onmessage = function (d) {
//                     log('Response : ' + d.data)
//                 }

//                 $('#send').click(function () {
//                     var msg = $('#msg').val()
//                     $('#msg').val('')
//                     ws.send(msg)
//                 })

//             } else {
//                 log('closing connection')
//                 ws.close()
//             }
//         })
//     }
// })
