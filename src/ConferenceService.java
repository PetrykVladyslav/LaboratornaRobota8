import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
public interface ConferenceService extends Remote {
    void registerParticipant(Participant participant) throws RemoteException;
    void exportToXML(String filename) throws RemoteException;
    void importFromXML(String filename) throws RemoteException;
    List<Participant> getParticipants() throws RemoteException;
    void registerCallback(ConferenceClientCallback callback) throws RemoteException;
    void unregisterCallback(ConferenceClientCallback callback) throws RemoteException;
}