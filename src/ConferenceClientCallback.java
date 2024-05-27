import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
public interface ConferenceClientCallback extends Remote {
    void updateParticipants(List<Participant> participants) throws RemoteException;
}