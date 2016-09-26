/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RicartAgrawala;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

/**
 *
 * @author dj_hunter
 */
public interface Hello extends Remote {

    /**
     * This method is to send a lock request
     *
     * @return
     * @throws java.rmi.RemoteException
     */
    public int[] lockRequest() throws RemoteException;

    /**
     * This method is to send the unlock request to all the peers
     * @throws java.rmi.RemoteException
     */
    public void unlockRequest() throws RemoteException;

    /**
     * Send a message to all the servers to acquire the lock
     *
     * @param myId
     * @param Clock
     * @return 
     * @throws java.rmi.RemoteException
     */
    public Boolean sendRequest(int myId, int[] Clock) throws RemoteException;

    public void ReceiveAck(int myId) throws RemoteException;
}
