package jpabasic.pinnolbe.service;


import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class TtsService {

    public String synthesizeTextToMp3(String text, String outputFileName) throws IOException {
        try (TextToSpeechClient ttsClient = TextToSpeechClient.create()) {
            SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();

            VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                    .setLanguageCode("ko-KR")
                    .setName("ko-KR-Wavenet-A")
                    .setSsmlGender(SsmlVoiceGender.FEMALE)
                    .build();

            AudioConfig audioConfig = AudioConfig.newBuilder()
                    .setAudioEncoding(AudioEncoding.MP3)
                    .setSpeakingRate(1.0)
                    .setPitch(0.0)
                    .build();

            SynthesizeSpeechResponse response = ttsClient.synthesizeSpeech(input, voice, audioConfig);
            ByteString audioBytes = response.getAudioContent();

            Path outputPath = Paths.get("tts-audio/" + outputFileName + ".mp3");
            Files.createDirectories(outputPath.getParent());
            Files.write(outputPath, audioBytes.toByteArray());

            return outputPath.toString();
        }
    }

    /**
     * 새로운 메서드: 텍스트 → MP3 바이트 배열만 리턴 (파일 저장 없음)
     */
    public byte[] synthesizeToByteArray(String text) throws IOException {
        try (TextToSpeechClient ttsClient = TextToSpeechClient.create()) {
            // 1) 텍스트 입력
            SynthesisInput input = SynthesisInput.newBuilder()
                    .setText(text)
                    .build();

            // 2) 목소리 설정 (한국어 WaveNet, 원하는 목소리로 교체 가능)
            VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                    .setLanguageCode("ko-KR")
                    .setName("ko-KR-Wavenet-D")
                    .setSsmlGender(SsmlVoiceGender.MALE)
                    .build();

            // 3) MP3 포맷 설정
            AudioConfig audioConfig = AudioConfig.newBuilder()
                    .setAudioEncoding(AudioEncoding.MP3)
                    .setSpeakingRate(1.0)
                    .setPitch(0.0)
                    .build();

            // 4) TTS 호출
            SynthesizeSpeechResponse response = ttsClient.synthesizeSpeech(input, voice, audioConfig);
            ByteString audioBytes = response.getAudioContent();

            // 5) 바이트 배열로 바로 반환
            return audioBytes.toByteArray();
        }
    }
}