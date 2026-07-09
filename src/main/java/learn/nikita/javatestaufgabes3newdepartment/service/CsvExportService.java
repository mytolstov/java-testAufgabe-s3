package learn.nikita.javatestaufgabes3newdepartment.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import learn.nikita.javatestaufgabes3newdepartment.entity.Auftrage;
import learn.nikita.javatestaufgabes3newdepartment.entity.Kunde;
import learn.nikita.javatestaufgabes3newdepartment.repository.AuftragRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CsvExportService {

    private final AuftragRepository auftragRepository;
    private final S3UploadService s3UploadService;

    @Value("${export.dir:exports}")
    private String exportDirProperty;

    private Path exportDir() {
        return Path.of(exportDirProperty);
    }

    private Path stateFile() {
        return exportDir().resolve("last_exported_id.id");
    }

    //every 3 hours
    @Scheduled(cron = "0 0 */3 * * *")
    @Transactional(readOnly = true)
    public void exportNewAuftrageToCsv() throws IOException {
        Files.createDirectories(exportDir());

        Long lastExportedId = readLastExportedId();

        List<Auftrage> newAuftrage =
                auftragRepository.findByAuftragIdGreaterThanOrderByAuftragIdAsc(lastExportedId);

        if (newAuftrage.isEmpty()) {
            System.out.println("No new records to export.");
            return;
        }

        Path csvFile = createCsvFilePath();

        writeLine(csvFile, "auftrag_id,artikel_nummer,created,last_change,kunde_id,vorname,nachname,email");

        for (Auftrage auftrage : newAuftrage) {
            Kunde kunde = auftrage.getKundeId();

            String line = String.join(",",
                    String.valueOf(auftrage.getAuftragId()),
                    escape(auftrage.getArtikelNummer()),
                    String.valueOf(auftrage.getCreated()),
                    String.valueOf(auftrage.getLastChange()),
                    String.valueOf(kunde.getKundeId()),
                    escape(kunde.getVorname()),
                    escape(kunde.getNachname()),
                    escape(kunde.getEmail())
            );

            writeLine(csvFile, line);
        }

        s3UploadService.uploadCsv(csvFile);

        Long newestExportedId = newAuftrage.get(newAuftrage.size() - 1).getAuftragId();
        saveLastExportedId(newestExportedId);

        System.out.println("Exported " + newAuftrage.size() + " records to " + csvFile);
    }

    private Long readLastExportedId() throws IOException {
        if (!Files.exists(stateFile())) {
            return 0L;
        }

        String content = Files.readString(stateFile()).trim();

        if(content.isEmpty()) {
            return 0L;
        }

        return Long.parseLong(content);
    }

    private Path createCsvFilePath() {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));

        return exportDir().resolve("auftrage_" + timestamp + ".csv");
    }

    private void writeLine(Path file, String line) throws IOException{
        Files.writeString(
                file,
                line + System.lineSeparator(),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        );
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }

        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private void saveLastExportedId(Long id) throws IOException {
        Files.writeString(
                stateFile(),
                String.valueOf(id),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );
    }
}
