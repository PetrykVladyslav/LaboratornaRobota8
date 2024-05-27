import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
public class ConferenceServiceImpl extends UnicastRemoteObject implements ConferenceService {
    private List<Participant> participants;
    private final List<ConferenceClientCallback> callbacks;
    private final ConferenceServerGUI serverGUI;
    public ConferenceServiceImpl(ConferenceServerGUI serverGUI) throws RemoteException {
        this.serverGUI = serverGUI;
        participants = new ArrayList<>();
        callbacks = new ArrayList<>();
    }
    @Override
    public synchronized void registerParticipant(Participant participant) throws RemoteException {
        participants.add(participant);
        serverGUI.updateParticipants(participants);
        notifyCallbacks();
    }
    private void notifyCallbacks() throws RemoteException {
        for (ConferenceClientCallback callback : callbacks) {
            callback.updateParticipants(participants);
        }
    }
    public void updateParticipants(List<Participant> participants) throws RemoteException {
        if (participants == null) {
            throw new IllegalArgumentException("Participants list cannot be null");
        }
        this.participants = participants;
        if (serverGUI != null) {
            serverGUI.updateParticipants(participants);
        }
    }
    @Override
    public void exportToXML(String filename) throws RemoteException {
        try {
            JAXBContext context = JAXBContext.newInstance(ParticipantsWrapper.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            ParticipantsWrapper wrapper = new ParticipantsWrapper();
            wrapper.setParticipants(participants);
            marshaller.marshal(wrapper, new File(filename));
        } catch (JAXBException e) {
            e.printStackTrace();
            throw new RemoteException("Error exporting participants to XML: " + e.getMessage());
        }
    }
    @Override
    public void importFromXML(String filename) throws RemoteException {
        try {
            JAXBContext context = JAXBContext.newInstance(ParticipantsWrapper.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            ParticipantsWrapper wrapper = (ParticipantsWrapper) unmarshaller.unmarshal(new File(filename));
            List<Participant> importedParticipants = wrapper.getParticipants();
            if (importedParticipants == null) {
                throw new RemoteException("Imported participants list is null");
            }
            updateParticipants(importedParticipants);
        } catch (JAXBException e) {
            e.printStackTrace();
            throw new RemoteException("Error importing participants from XML: " + e.getMessage());
        }
    }
    @Override
    public List<Participant> getParticipants() throws RemoteException {
        return participants;
    }
    @Override
    public void registerCallback(ConferenceClientCallback callback) throws RemoteException {
        if (!callbacks.contains(callback)) {
            callbacks.add(callback);
        }
    }
    @Override
    public void unregisterCallback(ConferenceClientCallback callback) throws RemoteException {
        callbacks.remove(callback);
    }
}

@XmlRootElement(name = "participants")
class ParticipantsWrapper {
    private List<Participant> participants;
    @XmlElement(name = "participant")
    public List<Participant> getParticipants() {
        return participants;
    }
    public void setParticipants(List<Participant> participants) {
        this.participants = participants;
    }
}