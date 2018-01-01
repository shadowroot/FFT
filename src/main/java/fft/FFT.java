package fft;

import config.Config;
import data.Complex;
import data.Samples;

import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FFT<ITYPE extends Number>{
    private Config config;
    protected Samples<ITYPE, Double> samples;
    protected Thread threads[];
    protected int threadsNumber = 1;
    private NotificationInterface notification;

    public FFT(Config config, Samples<ITYPE, Double> samples, NotificationInterface notification, int threadsNumber){
        this.threadsNumber = threadsNumber;
        init(config, samples, notification);
    }

    public FFT(Config config, Samples<ITYPE, Double> samples, NotificationInterface notification){
        optimalThreads();
        init(config, samples, notification);
}

    private void init(Config config, Samples<ITYPE, Double> samples, NotificationInterface notification){
        this.config = config;
        this.notification = notification;
        this.samples = samples;
    }

    public int getThreadsNumber() {
        return threadsNumber;
    }

    public FFT(Samples samples){
        this.samples = samples;
    }

    private void optimalThreads(){
        threadsNumber = Runtime.getRuntime().availableProcessors();
    }

    public void setSamples(Samples samples) {
        this.samples = samples;
    }


    double calcSin(int m, int k,int n){
        return Math.sin(-2 * Math.PI * m * k / (n/2));
    }

    double calcCos(int m, int k,int n){
        return Math.cos(-2 * Math.PI * m * k / (n/2));
    }

    public Samples getSamples() {
        return samples;
    }

    Complex calcSum(final List<ITYPE> currentSamples, int k, int n, int pos) {
        Complex sum1 = new Complex(0, 0);
        Complex sum2 = new Complex(0, 0);
        int end = n / 2 - 1;
        for (int i = 0; i < end; i++) {
            sum1.add(new Complex(calcCos(i, k, n), calcSin(i, k, n)).mul(currentSamples.get(  pos + 2 * i)));
        }
        for (int i = 0; i < end; i++) {
            sum2.add(new Complex(calcCos(i, k, n), calcSin(i, k, n)).mul(currentSamples.get(pos + 2 * i + 1)));
        }
        sum2.mul(new Complex(calcCos(1, k, n), calcSin(1, k, n)));
        sum1.add(sum2);
        return sum1;
    }

    public void parallelRun(final List<ITYPE> currentSamples, final List<Double> magnitudes, final int n, final int pos){
        threads = new Thread[threadsNumber];
        final int step = (n / 2) / threadsNumber;
        final int diff = (n / 2) - (step*threadsNumber);
        for(int i=0; i < threadsNumber; i++){
            final int kStart = i*step;
            int stop = (i+1)*step;
            if(i == (threadsNumber - 1)){
                stop += diff;
            }
            final int kStop = stop;
            threads[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    for(int k=kStart; k < kStop; k++) {
                        magnitudes.add(calcSum(currentSamples, k, n, pos).magnitude());
                    }
                }
            });
            threads[i].start();
        }

        for(int i=0; i < threadsNumber; i++){
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void parallelRun(final List<ITYPE> currentSamples, final List<Double> magnitudes, final int n, final int pos, final List<Integer> frequencies){
        threads = new Thread[threadsNumber];
        final int step = frequencies.size() / threadsNumber;
        final int diff = frequencies.size() - (step*threadsNumber);
        for(int i=0; i < threadsNumber; i++){
            final int kStart = i*step;
            int stop = (i+1)*step;
            if(i == (threadsNumber - 1)){
                stop += diff;
            }
            final int kStop = stop;
            threads[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    for(int i=kStart; i < kStop; i++) {
                        int k = frequencies.get(i);
                        magnitudes.add(calcSum(currentSamples, k, n, pos).magnitude());
                    }
                }
            });
            threads[i].start();
        }

        for(int i=0; i < threadsNumber; i++){
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateNotification(int idx, int total){
        if(notification != null){
            notification.updateProgress(((double)idx / (double)total * 100));
        }
    }

    public void fft(){
        int sampleRate = config.getSampleRate();
        for(int i=0; i < samples.size(); i++){
            List magnitudes = fft(samples.getSample(i));
            samples.addMagnitudes(magnitudes);
            updateNotification(i, samples.size() - 1);
        }
    }

    public void fft_for_frequencies(List<Integer> frequencies){
        int sampleRate = config.getSampleRate();
        for(int i=0; i < samples.size(); i++){
            List magnitudes = fft(samples.getSample(i), frequencies);
            samples.addMagnitudes(magnitudes);
            updateNotification(i, samples.size() - 1);
        }
    }


    public List<Double> fft(List<ITYPE> samples){
        final List<Double> magnitudes = new ArrayList<>();
        if(threadsNumber > 1){
            parallelRun(samples, magnitudes, config.getSampleRate(), 0);
        }
        else {
            for (int k = 0; k < (config.getSampleRate() / 2); k++) {
                Complex sum = calcSum(samples, k, config.getSampleRate(), 0);
                magnitudes.add(sum.magnitude());
            }
        }
        return magnitudes;
    }

    public List<Double> fft(List<ITYPE> samples,  List<Integer> frequencies){
        final List<Double> magnitudes = new ArrayList<>();
        if(threadsNumber > 1){
            parallelRun(samples, magnitudes, config.getSampleRate(), 0, frequencies);
        }
        else {
            for (int i = 0; i < frequencies.size(); i++) {
                int k = frequencies.get(i);
                Complex sum = calcSum(samples, k, config.getSampleRate(), 0);
                magnitudes.add(sum.magnitude());
            }
        }
        return magnitudes;
    }

}
