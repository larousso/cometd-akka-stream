package akka.stream.cometd.scaladsl

import org.cometd.bayeux.server.BayeuxServer
import org.cometd.server.{BayeuxServerImpl, CometDServlet}
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletHandler
import org.eclipse.jetty.server.ServerConnector

object CometdSpec extends App {

  startServer()

  def startServer(): Unit = {

    val server = new Server()
    val http = new ServerConnector(server, 1, 1)
    http.setHost("localhost")
    http.setPort(8080)
    http.setIdleTimeout(30000)

    server.addConnector(http)

    val handler = new ServletHandler()
    server.setHandler(handler)

    // Passing in the class for the Servlet allows jetty to instantiate an
    // instance of that Servlet and mount it on a given context path.

    // IMPORTANT:
    // This is a raw Servlet, not a Servlet that has been configured
    // through a web.xml @WebServlet annotation, or anything similar.
    handler.addServletWithMapping(classOf[CometDServlet], "/cometd/*")

    // Start things up!
    server.start()
    server.join()

  }
}
