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
import net.nmmst.request.SelectRequest;
import net.nmmst.tools.Ports;
import net.nmmst.tools.SerialStream;
/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class CommandTest {
    public static void main(String[] args) throws IOException {
        Request request = toRequest(args[1]);
        if (request.getType() == Request.Type.SELECT) {
            request = new Request(request.getType(), getSelectRequest());
            try (SerialStream stream = new SerialStream(args[0], Ports.REQUEST_MASTER.get())) {
                stream.write(request);
            }
        } else {
            if (request.getType() == Request.Type.ADD_SNAPSHOTS) {
                request = new Request(request.getType(), new Integer[]{1,2,3});
            }
            try (SerialStream stream = new SerialStream(args[0], Ports.REQUEST_OTHERS.get())) {
                stream.write(request);
            }
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
    private static SelectRequest getSelectRequest() {
        int[] index = new int[]{
            0,
            1,
            2,
            3,
            4,
            5,
            6
        };
        boolean[] value = new boolean[]{
            false,
            false,
            false,
            false,
            false,
            false,
            true
        };
        return new SelectRequest(index, value);
    }
}
