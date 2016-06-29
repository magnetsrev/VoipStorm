import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 *
 * @author ron
 */
public class NetManagerClient {

    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private String message;
    private String server                       = "localhost";
    private Socket socket;
    private int serverPort                      = 1969;
    private boolean runThreadsAsDaemons         = true;
    private Manager manager;
    private int connectTimeout                 = 999; // mSec
    private InetAddress localhost;
    private SocketAddress serverSocketAddress;
    private boolean clientStopRequested = true;

    /**
     *
     * @param managerParam
     * @param portParam
     */
    @SuppressWarnings("static-access")
    public NetManagerClient(Manager managerParam, int portParam)
    {
    	String[] status = new String[2];
        manager = managerParam;
        serverPort = portParam;
        try { localhost = InetAddress.getLocalHost(); } catch( UnknownHostException error) {  }
        serverSocketAddress = new InetSocketAddress(localhost, serverPort);
        socket = new Socket();
    }

    /**
     *
     * @param messageParam
     * @return
     */
    public String[] connectAndSend(String messageParam) // This is used by mother after connection has established
    {
        // Connect Phase
        String[] status = new String[2]; status[0] = "0"; status[1] = "";

        socket = new Socket();
        try { socket.connect(serverSocketAddress, connectTimeout); } catch (IOException ex) { status[0]="1"; status[1] = "Error: IOException: connectAndSend: socket.connect(serverSocketAddress, 500) " + ex.getMessage(); return status; }

        // Setup input / output streams
        try { outputStream = new ObjectOutputStream(socket.getOutputStream()); } catch (IOException ex) { status[0]="1"; status[1] = "Error: IOException: connectAndSend: outputStream = new ObjectOutputStream(..) " + ex.getMessage(); closeConnection(); return status; }
        try { outputStream.flush(); } catch (IOException ex) { status[0]="1"; status[1] = "Error: IOException: NetManClient(): output.flush() " + ex.getMessage();  closeConnection(); return status; }
        try { inputStream = new ObjectInputStream(socket.getInputStream()); } catch (IOException ex) { status[0]="1"; status[1] = "Error: IOException: connectAndSend: inputStream = new ObjectInputStream() " + ex.getMessage(); closeConnection(); return status; }

        // Write data through output stream
        try { outputStream.writeObject(messageParam); } catch (IOException ex) { status[0]="1"; status[1] = "Error: IOException: connectAndSend: outputStream.writeObject(messageParam) " + ex.getMessage(); closeConnection(); return status;}
        try { outputStream.flush(); } catch (IOException ex) { status[0]="1"; status[1] = "Error: IOException: outputStream.flush() " + ex.getMessage();  closeConnection(); return status;}

        // Read input stream
        try { status[1] = (String) inputStream.readObject(); }
        catch (IOException ex) { status[0]="1"; status[1] = "Error: IOException: connectAndSend: inputStream.readObject() " + ex.getMessage();  closeConnection(); return status;}
        catch (ClassNotFoundException ex) { status[0]="1"; status[1] = "Error: ClassNotFoundException: inputStream.readObject() " + ex.getMessage(); closeConnection(); return status;}

        closeConnection();
        return status;
    }

    /**
     *
     */
    public void closeConnection()
    {
        try { if ((socket != null) && ( ! socket.isClosed())) {socket.shutdownInput(); socket.shutdownOutput(); socket.close();} }
        catch(IOException ex) { manager.showStatus("Error: NetManagerClient.closeConnection() " + ex.getMessage(), true, true );} // true = logtoapplic, true = logtofile
    }

    /**
     *
     * @return
     */
    public boolean isConnected()    { if (socket != null ) { return socket.isConnected(); } else { return false; }}

    /**
     *
     * @return
     */
    public boolean isBound()        { return socket.isBound(); }
}
