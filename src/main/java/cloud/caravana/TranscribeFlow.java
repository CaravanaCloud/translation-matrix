package cloud.caravana;

import java.nio.file.Path;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.transcribe.TranscribeClient;
import software.amazon.awssdk.services.transcribe.model.Media;
import software.amazon.awssdk.services.transcribe.model.StartTranscriptionJobRequest;
import software.amazon.awssdk.services.transcribe.model.Subtitles;

public class TranscribeFlow implements Runnable {

    private Path path;

    TranscribeClient transcribe = TranscribeClient.create();
    S3Client s3 = S3Client.create();
    String bucketName = "ttmx";
    String prefix = "tb-job-" + System.currentTimeMillis();

    public TranscribeFlow(Path path) {
        this.path = path;
    }

    private String nameWithoutExt(String fileName) {
        if (fileName.indexOf(".") > 0) {
            return fileName.substring(0, fileName.lastIndexOf("."));
        } else {
            return fileName;
        }
    }

    @Override
    public void run() {
        System.out.println("Uploading " + path);
        var mediaKey = path.getFileName().toString();
        mediaKey = mediaKey.replaceAll("[^0-9a-zA-Z._-]", "_");
        var putObjectReq = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(mediaKey)
                .build();
        var putObjectBody = RequestBody.fromFile(path);
        s3.putObject(putObjectReq, putObjectBody);

        var mediaFileUri = "s3://" + bucketName + "/" + mediaKey;
        var media = Media.builder()
                .mediaFileUri(mediaFileUri)
                .build();

        // request.setMediaSampleRateHertz(16000);
        var jobName = prefix + "_" + nameWithoutExt(mediaKey);
        System.out.println("Transcribing " + jobName);
        var outKey = nameWithoutExt(mediaKey);
        var subtitles = Subtitles.builder()
                .formatsWithStrings("srt","vtt")
                .build();
        var tbRequest = StartTranscriptionJobRequest
                .builder()
                .media(media)
                .mediaFormat("mp4")
                .identifyLanguage(true)
                .transcriptionJobName(jobName)
                .outputBucketName(bucketName)
                .outputKey(outKey)
                .subtitles(subtitles)
                .build();
        transcribe.startTranscriptionJob(tbRequest);
        System.out.println("Done");
    }

}
