import javax.swing.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.awt.*;
import java.io.File;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.rmi.registry.Registry;
public class ConferenceServerGUI extends JFrame {
    private final JTextField hostField;
    private final JTextField portField;
    private final JLabel participantsLabel;
    private final JTextArea participantsTextArea;
    private ConferenceServiceImpl conferenceService;
    private boolean serverRunning = false;
    private Registry registry;
    public static int participantCount = 0;
    public ConferenceServerGUI() {
        setTitle("Conference Server");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLayout(new BorderLayout());

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayout(1, 6));

        hostField = new JTextField();
        hostField.setEditable(false);
        hostField.setText("localhost");
        controlPanel.add(new JLabel("Host:"));
        controlPanel.add(hostField);

        portField = new JTextField("1099");
        portField.setEditable(false);
        controlPanel.add(new JLabel("Port:"));
        controlPanel.add(portField);

        participantsLabel = new JLabel("");
        controlPanel.add(new JLabel("Participants:"));
        controlPanel.add(participantsLabel);

        add(controlPanel, BorderLayout.NORTH);

        participantsTextArea = new JTextArea();
        participantsTextArea.setEditable(false);
        add(new JScrollPane(participantsTextArea), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 5));

        JButton startButton = new JButton("Start");
        startButton.addActionListener(e -> startServer());
        buttonPanel.add(startButton);

        JButton stopButton = new JButton("Stop");
        stopButton.addActionListener(e -> stopServer());
        buttonPanel.add(stopButton);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> saveParticipants());
        buttonPanel.add(saveButton);

        JButton loadButton = new JButton("Load");
        loadButton.addActionListener(e -> loadParticipants());
        buttonPanel.add(loadButton);

        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> exitProgram());
        buttonPanel.add(exitButton);

        add(buttonPanel, BorderLayout.SOUTH);
        setLocationRelativeTo(null);
        setVisible(true);
    }
    private void startServer() {
        if (serverRunning) {
            JOptionPane.showMessageDialog(this, "Сервер вже працює.");
            return;
        }
        try {
            int port = Integer.parseInt(portField.getText());
            try {
                registry = LocateRegistry.getRegistry(port);
                registry.list();
            } catch (RemoteException e) {
                registry = LocateRegistry.createRegistry(port);
            }

            conferenceService = new ConferenceServiceImpl(this);
            registry.rebind("ConferenceService", conferenceService);
            participantsLabel.setText(" " + ConferenceServerGUI.participantCount);
            hostField.setText("localhost/" + java.net.InetAddress.getLocalHost().getHostAddress());
            serverRunning = true;
            JOptionPane.showMessageDialog(this, "Сервер запущено успішно.");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Помилка запуску сервера: " + e.getMessage());
        }
    }
    private void stopServer() {
        if (!serverRunning) {
            JOptionPane.showMessageDialog(this, "Сервер не запущено.");
            return;
        }
        try {
            registry.unbind("ConferenceService");
            UnicastRemoteObject.unexportObject(conferenceService, true);
            UnicastRemoteObject.unexportObject(registry, true);
            serverRunning = false;

            participantsTextArea.setText("");
            participantsLabel.setText(" 0");
            participantCount = 0;

            JOptionPane.showMessageDialog(this, "Сервер успішно зупинено.");
        } catch (NotBoundException e) {
            JOptionPane.showMessageDialog(this, "Послуга не прив'язана: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Помилка зупинки сервера: " + e.getMessage());
        }
    }
    private void saveParticipants() {
        if (conferenceService == null) {
            JOptionPane.showMessageDialog(this, "Сервер не працює. Запустіть спочатку сервер.");
            return;
        }
        try {
            conferenceService.exportToXML("participants_server.xml");
            JOptionPane.showMessageDialog(this, "Учасників збережено до participants_server.xml");
        } catch (RemoteException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Помилка збереження учасників: " + e.getMessage());
        }
    }
    private void loadParticipants() {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                JAXBContext context = JAXBContext.newInstance(ParticipantsWrapper.class);
                Unmarshaller unmarshaller = context.createUnmarshaller();
                ParticipantsWrapper wrapper = (ParticipantsWrapper) unmarshaller.unmarshal(selectedFile);

                showParticipantsDialog(wrapper.getParticipants());
            } catch (JAXBException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Помилка завантаження учасників із XML: " + e.getMessage());
            }
        }
    }
    private void showParticipantsDialog(List<Participant> participants) {
        JDialog participantsDialog = new JDialog(this, "Participants Info", true);
        participantsDialog.setSize(400, 300);
        participantsDialog.setLayout(new BorderLayout());

        JTextArea participantsTextArea = new JTextArea();
        participantsTextArea.setEditable(false);
        for (Participant p : participants) {
            participantsTextArea.append(p.toString() + "\n");
        }

        participantsDialog.add(new JScrollPane(participantsTextArea), BorderLayout.CENTER);
        participantsDialog.setLocationRelativeTo(this);
        participantsDialog.setVisible(true);
    }
    private void exitProgram() {
        stopServer();
        System.exit(0);
    }
    public void updateParticipants(List<Participant> participants) throws RemoteException {
        ConferenceServerGUI.participantCount = participants.size();
        participantsTextArea.setText("");
        for (Participant p : participants) {
            participantsTextArea.append(p.toString() + "\n");
        }
        participantsLabel.setText(" " + ConferenceServerGUI.participantCount);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                new ConferenceServerGUI();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}