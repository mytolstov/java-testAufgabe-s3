package learn.nikita.javatestaufgabes3newdepartment.service;

import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class S3UploadService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.s3.prefix}")
    private String prefix;

    public void uploadCsv(Path csvFile) {
        String fileName = csvFile.getFileName().toString();

        String s3Key = prefix + "/" + fileName;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .contentType("text/csv")
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromFile(csvFile));

        System.out.println("Uploaded CSV to S3: s3://" + bucketName + "/" + s3Key);
    }
}
