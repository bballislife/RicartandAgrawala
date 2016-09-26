package RicartAgrawala;

import static java.lang.Thread.sleep;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

class ServerThread implements Runnable, Hello {

    private int myId;
    private static int N;
    private String url;
    private Boolean gotReference[], gotAck[];
    private Hello stub[];
    private int currentState;
    private int receivedAcksCount;
    public int Clock[];
    private ArrayList<Integer> pendingRequest;

    ServerThread(String[] args) {
        take_input(args);
    }

    private void take_input(String[] S) {
        myId = Integer.parseInt(S[1]);
        N = Integer.parseInt(S[3]);
        url = "myId" + this.myId;
        stub = new Hello[N];
        currentState = 0;
        receivedAcksCount = 0;
        Clock = new int[N];
        pendingRequest = new ArrayList<>();
        gotReference = new Boolean[N];
        gotAck = new Boolean[N];
        Arrays.fill(gotAck, false);
        Arrays.fill(Clock, 0);
    }

    @Override
    public void run() {
        bindToNamingServer();
        try {
            getAllReferences();
        } catch (InterruptedException ex) {
            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }

    /**
     * register the particular node with the naming server
     */
    private void bindToNamingServer() {
        Hello bindStub;
        try {
            bindStub = (Hello) UnicastRemoteObject.exportObject(this, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.bind(this.url, bindStub);
            System.out.printf("Succesfully registerd: %d\n", this.myId);
        } catch (RemoteException | AlreadyBoundException e) {
            System.err.println("Server exception: " + e.toString());
        }
    }

    /**
     * This function gets the stub references to all the servers
     *
     * @throws InterruptedException
     */
    @SuppressWarnings("SleepWhileInLoop")
    private void getAllReferences() throws InterruptedException {
        int cnt = 0;
        String host = null;
        // this loop gets refernces to all the nodes
        while (true) {
            sleep(1000);
            Arrays.fill(gotReference, false);
            for (int i = 0; i < ServerThread.N; i++) {
                if (gotReference[i] == false) {
                    try {
                        Registry registry = LocateRegistry.getRegistry(host);
                        String s = "myId" + i;
                        stub[i] = (Hello) registry.lookup(s);
                        System.out.printf("Server with id %d got reference to server with id %d\n", this.myId, i);
                        gotReference[i] = true;
                        cnt++;
                    } catch (NotBoundException ex) {
                        System.out.printf("Still not Bound %d\n", i);
                    } catch (AccessException ex) {
                        Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (RemoteException ex) {
                        Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            if (cnt == ClientThread.N) {
                break;
            }            
        }
    }

    /**
     * send a lock request to all its peers
     * @return
     * @throws RemoteException 
     */
    @Override
    public int[] lockRequest() throws RemoteException {
        Clock[this.myId]++;
        this.currentState = 1;              // the node is interested in the lock
        for (int i = 0; i < ServerThread.N; i++) {
            if (i != this.myId) {
                Boolean returnVal = stub[i].sendRequest(this.myId, this.Clock);
                if(returnVal != true){  // this means that the message delivery was not successful
                    i--;                    
                }
            }
        }
        while (true) {
            try {
                sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (this.receivedAcksCount + 1 == ServerThread.N) {
                break;
            }
        }
        this.receivedAcksCount = 0;
        Arrays.fill(gotAck, false);
        this.currentState = 2;              // the node currently holds the lock
        return this.Clock;
    }

    /**
     * Inform all its peers that it no longer requires the lock
     */
    @Override
    public void unlockRequest() throws RemoteException {
        this.currentState = 0;
        for(int i = 0; i < pendingRequest.size(); i++){
            SendAck(pendingRequest.get(i));
        }
        pendingRequest.clear();
    }

    /**
     * the actual request send by a node
     * @param senderId
     * @param senderClock 
     */
    @Override
    public Boolean sendRequest(int senderId, int[] senderClock) throws RemoteException {
        switch (this.currentState) {
            case 0:
                SendAck(senderId);
            case 2:
                addToQueue(senderId);
            default:
                if (compareClock(senderClock) == true) {
                    SendAck(senderId);
                } else {
                    addToQueue(senderId);
                }
        }
        return true;
    }

    /**
     * This function compares the clocks of the sender with its own clock and
     * sends the reply according to that
     *
     * @param senderClock
     * @return
     */
    private boolean compareClock(int[] senderClock) {        
        return true;
    }

    /**
     * Send an ack to the peer
     * @param senderId 
     */
    private void SendAck(int senderId) throws RemoteException {
        stub[senderId].ReceiveAck(this.myId);
    }

    
    @Override
    public void ReceiveAck(int senderId) {
        if (gotAck[senderId] == false) {
            this.receivedAcksCount++;
            gotAck[senderId] = true;            
        }        
    }

    /**
     * This method adds to the queue the currently pending requests
     *
     * @param senderId
     */
    private void addToQueue(int senderId) {
        pendingRequest.add(senderId);
    }
}
