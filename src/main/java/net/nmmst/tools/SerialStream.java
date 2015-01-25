package net.nmmst.tools;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import net.nmmst.player.PlayerInformation;

public class SerialStream 
{
    private final Socket client;
    private ObjectInputStream input = null;
    private ObjectOutputStream output = null;
//	public static boolean sendAll(PlayerInformation[] playerInformations, final Serializable serial) throws InterruptedException, IOException
//	{
//		return sendAll(playerInformations, serial, Ports.REQUEST.get());
//	}
    public static boolean sendAll(PlayerInformation[] playerInformations, final Serializable serial, int port) throws InterruptedException, IOException
    {
        final SerialStream[] handlers = new SerialStream[playerInformations.length];
        for(int index = 0; index != playerInformations.length; ++index)
        {
            try 
            {
                handlers[index] = new SerialStream(new Socket(playerInformations[index].getIP(), port));
            } 
            catch(IOException e)
            {
                for(SerialStream handler : handlers)
                {
                    if(handler != null)
                        handler.close();
                }
                throw e;
            }
        }
        ExecutorService service = Executors.newFixedThreadPool(playerInformations.length);
        final CountDownLatch latch = new CountDownLatch(playerInformations.length);
        for(SerialStream handler : handlers)
        {
            final SerialStream h = handler;
            service.execute(new Runnable()
            {

                @Override
                public void run() 
                {
                    latch.countDown();
                    try 
                    {
                        latch.await();
                        h.write(serial);
                    } 
                    catch (IOException | InterruptedException e) 
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    finally
                    {
                        h.close();
                    }

                }

            });
        }
        service.shutdown();
        return service.awaitTermination(5, TimeUnit.SECONDS);
    }
    public SerialStream(Socket client) throws IOException
    {
        this.client = client;
    }

    public SerialStream(String ip, int port) throws UnknownHostException, IOException 
    {
        this(new Socket(ip, port));
    }

    public String getLocalAddress()
    {
        return client.getLocalAddress().getHostAddress();
    }
    public String getHostAddress()
    {
        return client.getInetAddress().getHostAddress();
    }
    public Object read() throws ClassNotFoundException, IOException
    {
        if(input == null)
            input = new ObjectInputStream(client.getInputStream());
        return input.readObject();
    }
    public void write(Serializable serial) throws IOException
    {
        if(output == null)
            output = new ObjectOutputStream(client.getOutputStream());
        output.writeObject(serial);
    }
    public void close()
    {
        try 
        {
            client.close();
        } 
        catch (IOException e) 
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
