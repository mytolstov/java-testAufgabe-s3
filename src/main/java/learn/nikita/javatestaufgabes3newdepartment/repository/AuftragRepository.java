package learn.nikita.javatestaufgabes3newdepartment.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import learn.nikita.javatestaufgabes3newdepartment.entity.Auftrage;

public interface AuftragRepository extends JpaRepository<Auftrage, Long> {
}
