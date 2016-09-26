/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RicartAgrawala;

import java.io.File;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.util.Arrays;

/**
 *
 * @author dj_hunter
 */
public class RicartAgrawala {

    RicartAgrawala(){}
    /**
     * @param args the command line arguments
     * @throws java.rmi.AlreadyBoundException
     * @throws java.rmi.RemoteException
     */
    public static void main(String[] args) throws AlreadyBoundException, RemoteException {
        RicartAgrawala obj;
        obj = new RicartAgrawala();
        ServerThread s;
        s = new ServerThread(args);
        new Thread(s).start();
        ClientThread c = new ClientThread(args);
        new Thread(c).start();
    }

}
