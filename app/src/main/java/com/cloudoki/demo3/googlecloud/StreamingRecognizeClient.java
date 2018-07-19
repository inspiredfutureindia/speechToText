package com.cloudoki.demo3.googlecloud;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import com.google.cloud.speech.v1beta1.RecognitionAudio;
import com.google.cloud.speech.v1beta1.RecognitionConfig;
import com.google.cloud.speech.v1beta1.SpeechGrpc;
import com.google.cloud.speech.v1beta1.StreamingRecognitionConfig;
import com.google.cloud.speech.v1beta1.StreamingRecognitionResult;
import com.google.cloud.speech.v1beta1.StreamingRecognizeRequest;
import com.google.cloud.speech.v1beta1.StreamingRecognizeResponse;
import com.google.protobuf.ByteString;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;

/**
 * Created by rmalta on 06/09/16.
 */
public class StreamingRecognizeClient {

    private int RECORDER_SAMPLERATE;
    private int RECORDER_CHANNELS;
    private int RECORDER_AUDIO_ENCODING;

    private String TAG = "StreamRecognizeClient";

    private int bufferSize = 0;

    private static final Logger logger = Logger.getLogger(StreamingRecognizeClient.class.getName());

    private final ManagedChannel channel;
    private final SpeechGrpc.SpeechStub speechClient;
    private StreamObserver<StreamingRecognizeResponse> responseObserver;
    private StreamObserver<StreamingRecognizeResponse> responseObserverHin;
    private StreamObserver<StreamingRecognizeResponse> responseObserverGuj;

    private StreamObserver<StreamingRecognizeRequest> requestObserver_ENG;
    private StreamObserver<StreamingRecognizeRequest> requestObserver_HIN;
    private StreamObserver<StreamingRecognizeRequest> requestObserver_GUJ;

    private AudioRecord recorder = null;

    private IResults mScreen;

    private String finalMsgEng = "";
    private String finalMsgHin = "";
    private String finalMsgGuj = "";
    float engPro = 0;
    float hinPro = 0;
    float gujPro = 0;

    boolean engDone = false;
    boolean hinDone = false;
    boolean gujDone = false;


    public StreamingRecognizeClient(ManagedChannel channel, IResults screen) throws IOException {

        this.RECORDER_SAMPLERATE = 8000;
        this.RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
        this.RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

        this.bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,
                RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);

        this.channel = channel;
        this.mScreen = screen;

        speechClient = SpeechGrpc.newStub(channel);

        responseObserver = new StreamObserver<StreamingRecognizeResponse>() {
            @Override
            public void onNext(StreamingRecognizeResponse response) {

                int numOfResults = response.getResultsCount();

                if( numOfResults > 0 ){

                    for (int i=0;i<numOfResults;i++){

                        StreamingRecognitionResult result = response.getResultsList().get(i);
                        String text = result.getAlternatives(0).getTranscript();

                        if( result.getIsFinal() ){
                            Log.d("\tFinal",  text);
                            mScreen.onPartial(text);
                            finalMsgEng = text;
                            engPro = result.getAlternatives(0).getConfidence();
                            Log.d("\tFinal",  Float.toString(result.getAlternatives(0).getConfidence()));
//                            result.getAlternatives(0).ge
                            engDone = true;
                        }
                        else{

                            Log.d("Partial",  text);
                            mScreen.onPartial(text);
                            engDone = false;
                        }

                    }
                }
            }

            @Override
            public void onError(Throwable error) {
                Log.w(TAG, "Recognize failed: {0}", error);

                try {
                    shutdown(false);
                    recognize();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCompleted() {
                Log.i(TAG, "Recognize complete.");

                try {
                    shutdown();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        responseObserverHin = new StreamObserver<StreamingRecognizeResponse>() {
            @Override
            public void onNext(StreamingRecognizeResponse response) {

                int numOfResults = response.getResultsCount();

                if( numOfResults > 0 ){

                    for (int i=0;i<numOfResults;i++){

                        StreamingRecognitionResult result = response.getResultsList().get(i);
                        String text = result.getAlternatives(0).getTranscript();

                        if( result.getIsFinal() ){
                            Log.d("\tFinal",  text);
                            mScreen.onPartial(text);
                            finalMsgHin = text;
                            hinPro = result.getAlternatives(0).getConfidence();
                            Log.d("\tFinal",  Float.toString(result.getAlternatives(0).getConfidence()));
//                            result.getAlternatives(0).ge
                            hinDone = true;
                        }
                        else{
                            Log.d("Partial",  text);
                            mScreen.onPartial(text);

                            hinDone =false;
                        }

                    }
                }
            }

            @Override
            public void onError(Throwable error) {
                Log.w(TAG, "Recognize failed: {0}", error);

                try {
                    shutdown(false);
                    recognize();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCompleted() {
                Log.i(TAG, "Recognize complete.");

                try {
                    shutdown();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        responseObserverGuj = new StreamObserver<StreamingRecognizeResponse>() {
            @Override
            public void onNext(StreamingRecognizeResponse response) {

                int numOfResults = response.getResultsCount();

                if( numOfResults > 0 ){

                    for (int i=0;i<numOfResults;i++){

                        StreamingRecognitionResult result = response.getResultsList().get(i);
                        String text = result.getAlternatives(0).getTranscript();

                        if( result.getIsFinal() ){
                            Log.d("\tFinal",  text);
                            mScreen.onPartial(text);
                            finalMsgGuj = text;
                            gujPro = result.getAlternatives(0).getConfidence();
                            Log.d("\tFinal",  Float.toString(result.getAlternatives(0).getConfidence()));
//                            result.getAlternatives(0).ge

                            gujDone = true;

                        }
                        else{
                            Log.d("Partial",  text);
                            mScreen.onPartial(text);

                            gujDone = false;
                        }

                    }
                }
            }

            @Override
            public void onError(Throwable error) {
                Log.w(TAG, "Recognize failed: {0}", error);

                try {
                    shutdown(false);
                    recognize();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCompleted() {
                Log.i(TAG, "Recognize complete.");

                try {
                    shutdown();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        requestObserver_ENG = speechClient.streamingRecognize(responseObserver);
        requestObserver_HIN = speechClient.streamingRecognize(responseObserverHin);
        requestObserver_GUJ = speechClient.streamingRecognize(responseObserverGuj);
    }

    public void shutdown() throws InterruptedException {
        shutdown(true);
    }

    public void shutdown(boolean closeChannel) throws InterruptedException {

        if( recorder != null ){
            recorder.stop();
            recorder.release();
            recorder = null;
        }
        if( closeChannel )
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    /** Send streaming recognize requests to server. */
    public void recognize() throws InterruptedException, IOException {

        try {
            // Build and send a StreamingRecognizeRequest containing the parameters for
            // processing the audio.
            RecognitionConfig config_ENG =
                    RecognitionConfig.newBuilder()
                            .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                            .setSampleRate(this.RECORDER_SAMPLERATE)
                            .setLanguageCode("en-US")
                            .build();

            RecognitionConfig config_HIN =
                    RecognitionConfig.newBuilder()
                            .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                            .setSampleRate(this.RECORDER_SAMPLERATE)
                            .setLanguageCode("hi-IN")
                            .build();

            RecognitionConfig config_GUJ =
                    RecognitionConfig.newBuilder()
                            .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                            .setSampleRate(this.RECORDER_SAMPLERATE)
                            .setLanguageCode("gu-IN")
                            .build();



            // Streaming config
            StreamingRecognitionConfig streamingConfig_ENG =
                    StreamingRecognitionConfig.newBuilder()
                            .setConfig(config_ENG)
                            .setInterimResults(true)
                            .setSingleUtterance(false)
                            .build();

            StreamingRecognitionConfig streamingConfig_HIN =
                    StreamingRecognitionConfig.newBuilder()
                            .setConfig(config_HIN)
                            .setInterimResults(true)
                            .setSingleUtterance(false)
                            .build();

            StreamingRecognitionConfig streamingConfig_GUJ =
                    StreamingRecognitionConfig.newBuilder()
                            .setConfig(config_GUJ)
                            .setInterimResults(true)
                            .setSingleUtterance(false)
                            .build();


            // First request
            StreamingRecognizeRequest initial_ENG =
                    StreamingRecognizeRequest.newBuilder().setStreamingConfig(streamingConfig_ENG).build();

            StreamingRecognizeRequest initial_HIN =
                    StreamingRecognizeRequest.newBuilder().setStreamingConfig(streamingConfig_HIN).build();

            StreamingRecognizeRequest initial_GUJ =
                    StreamingRecognizeRequest.newBuilder().setStreamingConfig(streamingConfig_GUJ).build();


            requestObserver_ENG.onNext(initial_ENG);
            requestObserver_HIN.onNext(initial_HIN);
            requestObserver_GUJ.onNext(initial_GUJ);

            // Microphone listener and recorder
            recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    this.RECORDER_SAMPLERATE,
                    this.RECORDER_CHANNELS,
                    this.RECORDER_AUDIO_ENCODING,
                    bufferSize);

            recorder.startRecording();

            byte[] buffer = new byte[bufferSize];
            int recordState;

            // loop through the audio samplings
            while ( (recordState = recorder.read(buffer, 0, buffer.length) ) > -1 ) {

                // skip if there is no data
                if( recordState < 0 )
                    continue;

                // create a new recognition request
                StreamingRecognizeRequest request =
                        StreamingRecognizeRequest.newBuilder()
                                .setAudioContent(ByteString.copyFrom(buffer, 0, buffer.length))
                                .build();

                // put it on the works
                requestObserver_ENG.onNext(request);
                requestObserver_HIN.onNext(request);
                requestObserver_GUJ.onNext(request);

                // Here

                if (engDone && hinDone && gujDone) {

                    float d = gujPro > (engPro > hinPro ? engPro : gujPro) ? gujPro : ((engPro > hinPro) ? engPro : hinPro);

                    if (d == engPro) {
                        mScreen.onFinal(finalMsgEng);
                    } else if (d == hinPro) {
                        mScreen.onFinal(finalMsgHin);
                    } else if (d == gujPro) {
                        mScreen.onFinal(finalMsgGuj);
                    }
                    engDone = hinDone = gujDone = false;
                }


            }




        } catch (RuntimeException e) {
            // Cancel RPC.
            requestObserver_ENG.onError(e);
            requestObserver_HIN.onError(e);
            requestObserver_GUJ.onError(e);
            throw e;
        }
        // Mark the end of requests.
        requestObserver_ENG.onCompleted();
        requestObserver_HIN.onCompleted();
        requestObserver_GUJ.onCompleted();
    }

}
