import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
@XmlRootElement
public class Participant implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private String family;
    private String organization;
    private String report;
    private String email;
    public Participant(String name, String family, String organization, String report, String email) {
        this.name = name;
        this.family = family;
        this.organization = organization;
        this.report = report;
        this.email = email;
    }
    public Participant() {}
    @XmlElement
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    @XmlElement
    public String getFamily() {
        return family;
    }
    public void setFamily(String family) {
        this.family = family;
    }
    @XmlElement
    public String getOrganization() {
        return organization;
    }
    public void setOrganization(String organization) {
        this.organization = organization;
    }
    @XmlElement
    public String getReport() {
        return report;
    }
    public void setReport(String report) {
        this.report = report;
    }
    @XmlElement
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    @Override
    public String toString() {
        return "Учасник {" +
                "name='" + name + '\'' +
                ", family='" + family + '\'' +
                ", organization='" + organization + '\'' +
                ", report='" + report + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}