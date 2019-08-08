package ioaccess;

import com.kuka.connectivity.fastRobotInterface.clientSDK.base.ClientApplication;
import com.kuka.connectivity.fastRobotInterface.clientSDK.connection.UdpConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py4j.GatewayServer;

/**
 * Implementation of a FRI client application.
 * <p>
 * The application provides a {@link ClientApplication#connect}, a {@link ClientApplication#step()} and a
 * {@link ClientApplication#disconnect} method, which will be called successively in the application life-cycle.
 *
 *
 * @see ClientApplication#connect
 * @see ClientApplication#step()
 * @see ClientApplication#disconnect
 */
public class IOAccessApp
{
    private IOAccessApp()
    {
        // only for sonar
    }

    private static final int DEFAULT_PORT_ID = 30200;

    private static final Logger blogger = LoggerFactory.getLogger(IOAccessApp.class);

    /**
     * Main method.
     *
     * @param argv
     *            command line arguments
     */
    public static void main(String[] argv)
    {
        if (argv.length > 0 && "help".equals(argv[0]))
        {
            blogger.info("KUKA Fieldbus access test application. Command line arguments: 1) remote hostname (optional)" +
                    " 2) port ID (optional)");
            return;
        }

        blogger.info("Enter IOAccess Client Application.");
        IOAccessClient client = new IOAccessClient();

        GatewayServer server = new GatewayServer(client);
        server.start();

        String hostname = (argv.length >= 1) ? argv[0] : null;
        int port = (argv.length >= 2) ? Integer.parseInt(argv[1]) : DEFAULT_PORT_ID;

        UdpConnection connection = new UdpConnection();

        ClientApplication app = new ClientApplication(connection, client);

        app.connect(port, hostname);

        boolean success = true;
        while (success)
        {
            success = app.step();
        }

        app.disconnect();
        server.shutdown();

        blogger.info("Exit IOAccess Client Application");
    }
}
