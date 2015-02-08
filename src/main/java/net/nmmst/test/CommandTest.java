/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.nmmst.test;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import net.nmmst.request.Request;

/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class CommandTest {
    public static void main(String[] args) throws IOException {
        try (Socket socket = new Socket(args[0], Integer.valueOf(args[1]))) {
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            output.writeObject(toRequest(args[2]));
            System.out.println("ok");
        }
    }
    public static Request toRequest(String arg) {
        for (Request.Type type : Request.Type.values()) {
            if (type.name().compareToIgnoreCase(arg) == 0) {
                return new Request(type, null);
            }
        }
        throw new RuntimeException("No suitable command");
    }
}
