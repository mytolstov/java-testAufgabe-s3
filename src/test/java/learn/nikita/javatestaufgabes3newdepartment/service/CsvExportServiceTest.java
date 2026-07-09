package learn.nikita.javatestaufgabes3newdepartment.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import learn.nikita.javatestaufgabes3newdepartment.entity.Auftrage;
import learn.nikita.javatestaufgabes3newdepartment.entity.Kunde;
import learn.nikita.javatestaufgabes3newdepartment.repository.AuftragRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CsvExportServiceTest {

    @Mock
    private AuftragRepository auftragRepository;

    @Mock
    private S3UploadService s3UploadService;

    @TempDir
    private Path tempDir;

    private CsvExportService csvExportService;

    @BeforeEach
    void setUp() {
        csvExportService = new CsvExportService(auftragRepository, s3UploadService);
        ReflectionTestUtils.setField(csvExportService, "exportDirProperty", tempDir.toString());
    }

    @Test
    void exportsNewRecordsAndSavesLastExportedId() throws IOException {
        Kunde kunde = new Kunde();
        kunde.setKundeId(1L);
        kunde.setVorname("Max");
        kunde.setNachname("Müller");
        kunde.setEmail("max@test.de");

        Auftrage auftrage = new Auftrage();
        auftrage.setAuftragId(5L);
        auftrage.setArtikelNummer("ART-1");
        auftrage.setKundeId(kunde);

        when(auftragRepository.findByAuftragIdGreaterThanOrderByAuftragIdAsc(0L))
                .thenReturn(List.of(auftrage));

        csvExportService.exportNewAuftrageToCsv();

        Path stateFile = tempDir.resolve("last_exported_id.id");
        assertThat(Files.readString(stateFile).trim()).isEqualTo("5");

        verify(s3UploadService).uploadCsv(any(Path.class));

        try (Stream<Path> files = Files.list(tempDir)) {
            List<Path> csvFiles = files.filter(p -> p.toString().endsWith(".csv")).toList();
            assertThat(csvFiles).hasSize(1);

            List<String> lines = Files.readAllLines(csvFiles.get(0));
            assertThat(lines).hasSize(2);
            assertThat(lines.get(1)).contains("\"ART-1\"", "\"Max\"", "\"Müller\"");
        }
    }

    @Test
    void skipsExportWhenNoNewRecords() throws IOException {
        when(auftragRepository.findByAuftragIdGreaterThanOrderByAuftragIdAsc(0L))
                .thenReturn(List.of());

        csvExportService.exportNewAuftrageToCsv();

        verify(s3UploadService, never()).uploadCsv(any());
        assertThat(Files.exists(tempDir.resolve("last_exported_id.id"))).isFalse();
    }

    @Test
    void readsLastExportedIdFromExistingStateFile() throws IOException {
        Files.writeString(tempDir.resolve("last_exported_id.id"), "42");

        when(auftragRepository.findByAuftragIdGreaterThanOrderByAuftragIdAsc(42L))
                .thenReturn(List.of());

        csvExportService.exportNewAuftrageToCsv();

        verify(auftragRepository).findByAuftragIdGreaterThanOrderByAuftragIdAsc(42L);
    }

    @Test
    void escapesCommasAndQuotesInCsvFields() throws IOException {
        Kunde kunde = new Kunde();
        kunde.setKundeId(2L);
        kunde.setVorname("Anna");
        kunde.setNachname("O\"Brien, Jr.");
        kunde.setEmail("anna@test.de");

        Auftrage auftrage = new Auftrage();
        auftrage.setAuftragId(7L);
        auftrage.setArtikelNummer("ART-2");
        auftrage.setKundeId(kunde);

        when(auftragRepository.findByAuftragIdGreaterThanOrderByAuftragIdAsc(0L))
                .thenReturn(List.of(auftrage));

        csvExportService.exportNewAuftrageToCsv();

        try (Stream<Path> files = Files.list(tempDir)) {
            Path csv = files.filter(p -> p.toString().endsWith(".csv")).findFirst().orElseThrow();
            String content = Files.readString(csv);
            assertThat(content).contains("\"O\"\"Brien, Jr.\"");
        }
    }
}