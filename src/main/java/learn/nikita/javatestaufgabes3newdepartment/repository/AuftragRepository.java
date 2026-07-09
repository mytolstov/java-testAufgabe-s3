package learn.nikita.javatestaufgabes3newdepartment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import learn.nikita.javatestaufgabes3newdepartment.entity.Auftrage;

public interface AuftragRepository extends JpaRepository<Auftrage, Long> {

    @EntityGraph(attributePaths = "kundeId")
    List<Auftrage> findByAuftragIdGreaterThanOrderByAuftragIdAsc(Long lastExportedId);
}
