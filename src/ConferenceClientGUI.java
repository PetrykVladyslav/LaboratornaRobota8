import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
public class ConferenceClientGUI extends UnicastRemoteObject implements ConferenceClientCallback {
    private JFrame frame;
    private JTextField hostField;
    private JTextField portField;
    private JTextField nameField;
    private JTextField familyField;
    private JTextField organizationField;
    private JTextField reportField;
    private JTextField emailField;
    private JLabel participantsLabel;
    private ConferenceService conferenceService;
    public ConferenceClientGUI() throws RemoteException {
        initialize();
    }
    private void initialize() {
        frame = new JFrame("Conference Client");
        frame.setBounds(100, 100, 600, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());

        JPanel controlPanel = new JPanel();
        controlPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        frame.getContentPane().add(controlPanel, BorderLayout.NORTH);
        controlPanel.setLayout(new GridLayout(3, 2, 10, 10));

        controlPanel.add(new JLabel("Host:"));
        hostField = new JTextField("localhost");
        controlPanel.add(hostField);

        controlPanel.add(new JLabel("Port:"));
        portField = new JTextField("1099");
        controlPanel.add(portField);

        participantsLabel = new JLabel("");
        controlPanel.add(new JLabel("Participants:"));
        controlPanel.add(participantsLabel);

        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        frame.getContentPane().add(fieldsPanel, BorderLayout.CENTER);
        fieldsPanel.setLayout(new GridLayout(5, 2, 10, 10));

        fieldsPanel.add(new JLabel("Name:", JLabel.LEFT));
        nameField = new JTextField();
        fieldsPanel.add(nameField);

        fieldsPanel.add(new JLabel("Family:", JLabel.LEFT));
        familyField = new JTextField();
        fieldsPanel.add(familyField);

        fieldsPanel.add(new JLabel("Organization:", JLabel.LEFT));
        organizationField = new JTextField();
        fieldsPanel.add(organizationField);

        fieldsPanel.add(new JLabel("Report:", JLabel.LEFT));
        reportField = new JTextField();
        fieldsPanel.add(reportField);

        fieldsPanel.add(new JLabel("Email:", JLabel.LEFT));
        emailField = new JTextField();
        fieldsPanel.add(emailField);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        frame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        buttonPanel.setLayout(new GridLayout(1, 4, 10, 10));

        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(e -> registerParticipant());
        buttonPanel.add(registerButton);

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> clearFields());
        buttonPanel.add(clearButton);

        JButton getInfoButton = new JButton("Get Info");
        getInfoButton.addActionListener(e -> loadParticipantsFromXML());
        buttonPanel.add(getInfoButton);

        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> exitProgram());
        buttonPanel.add(exitButton);

        JTextArea participantsTextArea = new JTextArea();
        participantsTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(participantsTextArea);
        frame.getContentPane().add(scrollPane, BorderLayout.EAST);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    private void registerParticipant() {
        try {
            String host = hostField.getText();
            int port = Integer.parseInt(portField.getText());

            try {
                conferenceService = (ConferenceService) Naming.lookup("//" + host + ":" + port + "/ConferenceService");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(frame, "Сервер не запущений або зупинений. Спробуйте ще раз.", "Server Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String name = nameField.getText();
            String family = familyField.getText();
            String organization = organizationField.getText();
            String report = reportField.getText();
            String email = emailField.getText();

            Participant participant = new Participant(name, family, organization, report, email);
            conferenceService.registerParticipant(participant);

            participantsLabel.setText(" " + conferenceService.getParticipants().size());

            JOptionPane.showMessageDialog(frame, "Реєстрація пройшла успішно");
        } catch (RemoteException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Помилка зв’язку з сервером: " + e.getMessage(), "Communication Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Помилка реєстрації учасника: " + e.getMessage(), "Registration Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void clearFields() {
        nameField.setText("");
        familyField.setText("");
        organizationField.setText("");
        reportField.setText("");
        emailField.setText("");
    }
    private void loadParticipantsFromXML() {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(frame);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                JAXBContext context = JAXBContext.newInstance(ParticipantsWrapper.class);
                Unmarshaller unmarshaller = context.createUnmarshaller();
                ParticipantsWrapper wrapper = (ParticipantsWrapper) unmarshaller.unmarshal(selectedFile);

                JDialog participantsDialog = new JDialog(frame, "Participants Info", true);
                participantsDialog.setSize(400, 300);
                participantsDialog.setLayout(new BorderLayout());

                JTextArea participantsTextArea = new JTextArea();
                participantsTextArea.setEditable(false);
                for (Participant p : wrapper.getParticipants()) {
                    participantsTextArea.append(p.toString() + "\n");
                }

                participantsDialog.add(new JScrollPane(participantsTextArea), BorderLayout.CENTER);
                participantsDialog.setLocationRelativeTo(frame);
                participantsDialog.setVisible(true);

            } catch (JAXBException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Помилка завантаження учасників із XML: " + e.getMessage());
            }
        }
    }
    private void exitProgram() {
        try {
            if (conferenceService != null) {
                UnicastRemoteObject.unexportObject(this, true);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
    @Override
    public void updateParticipants(List<Participant> participants) throws RemoteException {
        participantsLabel.setText("Participants: " + participants.size());
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                new ConferenceClientGUI();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}