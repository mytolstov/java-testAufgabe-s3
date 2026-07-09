package learn.nikita.javatestaufgabes3newdepartment.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class S3UploadServiceTest {

    @Mock
    private S3Client s3Client;

    @TempDir
    private Path tempDir;

    private S3UploadService s3UploadService;

    @BeforeEach
    void setUp() {
        s3UploadService = new S3UploadService(s3Client);
        ReflectionTestUtils.setField(s3UploadService, "bucketName", "my-bucket");
        ReflectionTestUtils.setField(s3UploadService, "prefix", "exports");
    }

    @Test
    void uploadsFileWithExpectedBucketAndKey() throws IOException {
        Path file = tempDir.resolve("auftrage_test.csv");
        Files.writeString(file, "auftrag_id,artikel_nummer\n1,ART-1");

        s3UploadService.uploadCsv(file);

        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(captor.capture(), any(RequestBody.class));

        PutObjectRequest request = captor.getValue();
        assertThat(request.bucket()).isEqualTo("my-bucket");
        assertThat(request.key()).isEqualTo("exports/auftrage_test.csv");
        assertThat(request.contentType()).isEqualTo("text/csv");
    }
}