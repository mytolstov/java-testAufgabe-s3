package learn.nikita.javatestaufgabes3newdepartment.service;

import java.util.Random;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import learn.nikita.javatestaufgabes3newdepartment.entity.Auftrage;
import learn.nikita.javatestaufgabes3newdepartment.entity.Kunde;
import learn.nikita.javatestaufgabes3newdepartment.repository.AuftragRepository;
import learn.nikita.javatestaufgabes3newdepartment.repository.KundeRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DataGenerator {

    private final KundeRepository kundeRepository;
    private final AuftragRepository auftragRepository;

    private final Random random = new Random();

    //alle 30 Minuten
    @Scheduled(fixedRate = 1800000)
    @Transactional
    public void generateRandomData() {
        Kunde kunde = new Kunde();

        kunde.setVorname(randomVorname());
        kunde.setNachname(randomNachname());
        kunde.setEmail("kunde-" + UUID.randomUUID() + "@ionos.de");
        kunde.setOrt("Karlsruhe");
        kunde.setLand("Deutschland");
        kunde.setPlz("76131");
        kunde.setStrasse("Teststraße");
        kunde.setFirmenname("Test Firma GmbH");

        Kunde saveKunde = kundeRepository.save(kunde);

        Auftrage auftrage = new Auftrage();

        auftrage.setArtikelNummer("ART-" + random.nextInt(100000));
        auftrage.setKundeId(saveKunde);

        auftragRepository.save(auftrage);

        System.out.println("Random Kunde und Auftrag gespeichert");
    }

    private String randomVorname() {
        String[] namen = {"Max", "Anna", "Peter", "Lisa", "Nikita"};
        return namen[random.nextInt(namen.length)];
    }

    private String randomNachname() {
        String[] namen = {"Müller", "Schmidt", "Weber", "Fischer", "Becker"};
        return namen[random.nextInt(namen.length)];
    }
}
