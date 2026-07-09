package learn.nikita.javatestaufgabes3newdepartment.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import learn.nikita.javatestaufgabes3newdepartment.entity.Auftrage;
import learn.nikita.javatestaufgabes3newdepartment.entity.Kunde;
import learn.nikita.javatestaufgabes3newdepartment.repository.AuftragRepository;
import learn.nikita.javatestaufgabes3newdepartment.repository.KundeRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataGeneratorTest {

    @Mock
    private KundeRepository kundeRepository;

    @Mock
    private AuftragRepository auftragRepository;

    private DataGenerator dataGenerator;

    @BeforeEach
    void setUp() {
        dataGenerator = new DataGenerator(kundeRepository, auftragRepository);
    }

    @Test
    void generatesAndSavesOneKundeAndOneAuftrag() {
        when(kundeRepository.save(any(Kunde.class))).thenAnswer(invocation -> invocation.getArgument(0));

        dataGenerator.generateRandomData();

        ArgumentCaptor<Kunde> kundeCaptor = ArgumentCaptor.forClass(Kunde.class);
        verify(kundeRepository).save(kundeCaptor.capture());
        Kunde savedKunde = kundeCaptor.getValue();
        assertThat(savedKunde.getVorname()).isNotBlank();
        assertThat(savedKunde.getNachname()).isNotBlank();
        assertThat(savedKunde.getEmail()).endsWith("@ionos.de");

        ArgumentCaptor<Auftrage> auftragCaptor = ArgumentCaptor.forClass(Auftrage.class);
        verify(auftragRepository).save(auftragCaptor.capture());
        Auftrage savedAuftrag = auftragCaptor.getValue();
        assertThat(savedAuftrag.getArtikelNummer()).startsWith("ART-");
        assertThat(savedAuftrag.getKundeId()).isEqualTo(savedKunde);
    }
}