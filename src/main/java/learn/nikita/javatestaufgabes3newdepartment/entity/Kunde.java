package learn.nikita.javatestaufgabes3newdepartment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "kunde")
public class Kunde {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "kunde_id")
    private Long kundeId;

    @Column(length=50, nullable=false)
    private String vorname;

    @Column(length = 50, nullable = false)
    private String nachname;

    @Column(length = 255,unique = true, nullable = false)
    private String email;

    @Column(length = 100)
    private String strasse;

    @Column(length = 100)
    private String strassenzusatz;

    @Column(length = 50)
    private String ort;

    @Column(length = 50)
    private String land;

    @Column(length = 20)
    private String plz;

    @Column(length = 126)
    private String firmenname;
}
