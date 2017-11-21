import Kitura
import Foundation
import KituraContracts

public let router = Router()
//router.use("/*", middleware: BodyParser())
router.all(middleware: BodyParser())

router.get("/vlc:file") { request, response, next in
    let p1 = request.parameters["file"] ?? "(nil)"
    try Shell.execute("/Applications/VLC.app/Contents/MacOS/VLC -f /Users/lothar/Movies/\(String(p1.characters.dropFirst()))")
    response.headers["Content-Type"] = "text/html"
    try response.send(
        "<!DOCTYPE html><html><body>" +
            "<b>Filename:</b> \(p1)" +
        "</body></html>\n\n").end()
}


Kitura.addHTTPServer(onPort: 8090, with: router)
Kitura.run()
