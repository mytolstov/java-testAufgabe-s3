package learn.nikita.javatestaufgabes3newdepartment.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import learn.nikita.javatestaufgabes3newdepartment.entity.Kunde;

public interface KundeRepository extends JpaRepository<Kunde, Long> {
}
