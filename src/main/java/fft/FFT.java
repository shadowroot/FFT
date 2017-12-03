package fft;

import config.Config;
import data.Complex;
import data.Samples;

import java.nio.DoubleBuffer;
import java.util.Date;

public class FFT{
    protected Config config = Config.getInstance();
    protected Samples samples;
    protected Thread threads[];
    protected int threadsNumber = 1;
    private Notification notification;
    private int pos;
    private int totalSamples;
    //private boolean usePrecalc = true;
    double[] sins;
    double[] coss;

    public FFT(int sampleRate, Samples samples, Notification notification, int threadsNumber){
        init(sampleRate, samples, notification, threadsNumber);
    }

    public FFT(int sampleRate, Samples samples){
        optimalThreads();
        init(sampleRate, samples, null, threadsNumber);
    }

    private void init(int sampleRate, Samples samples, Notification notification, int threadsNumber){
        this.notification = notification;
        this.samples = samples;
        config.setSampleRate(sampleRate);
        this.threadsNumber = threadsNumber;
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

    Complex calcSum(final Samples currentSamples, int k, int n, int pos) {
        Complex sum1 = new Complex(0, 0);
        Complex sum2 = new Complex(0, 0);
        int end = n / 2 - 1;
        for (int i = 0; i < end; i++) {
            sum1.add(new Complex(calcCos(i, k, n), calcSin(i, k, n)).mul((double) currentSamples.getSample(pos + 2 * i)));
        }
        for (int i = 0; i < end; i++) {
            sum2.add(new Complex(calcCos(i, k, n), calcSin(i, k, n)).mul((double) currentSamples.getSample(pos + 2 * i + 1)));
        }
        sum2.mul(new Complex(calcCos(1, k, n), calcSin(1, k, n)));
        sum1.add(sum2);
        return sum1;
    }

    public void parallelRun(final Samples currentSamples, final Double[] magnitudes, final int n, final int pos){
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
                        magnitudes[k] = calcSum(currentSamples, k, n, pos).magnitude();
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

    private void updateNotification(){
        if(notification != null){
            notification.update();
        }
    }

    public void fft(){
        if(samples == null || samples.getSamples() == null){
            return;
        }
        int n = config.getSampleRate();
        if(n < 0){
            System.out.println(new Date().toString() + ": Using FFT without sample rate.");
            n = samples.getSamples().size();
        }
        totalSamples = samples.getSamples().size();
        for(; (pos = samples.getNSamples(n)) >= 0;) {
            final Double[] magnitudes = new Double[n / 2];
            if(threadsNumber > 1){
                parallelRun(samples, magnitudes, n, pos);
            }
            else {
                for (int k = 0; k < (n / 2); k++) {
                    Complex sum = calcSum(samples, k, n, pos);
                    magnitudes[k] = sum.magnitude();
                }
            }
            samples.addMagnitudes(magnitudes);
            updateNotification();
        }
    }

    public double progress(){
        return (pos/totalSamples) * 100;
    }

}
