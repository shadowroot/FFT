package fft;

import config.Config;
import data.Complex;
import data.Samples;

import java.nio.DoubleBuffer;
import java.util.Date;

public class FFT {
    protected Config config = Config.getInstance();
    protected Samples samples;
    protected Thread threads[];
    protected int threadsNumber = 1;
    //private boolean usePrecalc = true;
    double[] sins;
    double[] coss;

    public FFT(int sampleRate, Samples samples, int threadsNumber){
        init(sampleRate, samples, threadsNumber);
    }

    public FFT(int sampleRate, Samples samples){
        optimalThreads();
        init(sampleRate, samples, threadsNumber);
    }

    private void init(int sampleRate, Samples samples, int threadsNumber){
        this.samples = samples;
        config.setSampleRate(sampleRate);
        this.threadsNumber = threadsNumber;
        /*
        if(usePrecalc){
            precalcs(sampleRate);
        }
        */
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

    /*
    void precalcs(int n){
        int valid_freqs = n/2;
        if(sins != null && sins.length == valid_freqs * n){
            return;
        }
        sins = new double[valid_freqs*valid_freqs];
        coss = new double[valid_freqs*valid_freqs];
        for(int k=0; k < valid_freqs; k++) {
            for (int m = 0; m < valid_freqs; m++) {
                sins[m*k] = Math.sin(-2 * Math.PI * m * k / valid_freqs);
                coss[m*k] = Math.cos(-2 * Math.PI * m * k / valid_freqs);
            }
        }
    }
    */

    double calcSin(int m, int k,int n){
        /*
        if(usePrecalc && sins != null){
            return sins[m*k];
        }
        */
        return Math.sin(-2 * Math.PI * m * k / (n/2));
    }

    double calcCos(int m, int k,int n){
        /*
        if(usePrecalc && coss != null){
            return coss[m*k];
        }
        */
        return Math.cos(-2 * Math.PI * m * k / (n/2));
    }

    public Samples getSamples() {
        return samples;
    }

    Complex calcSum(final DoubleBuffer currentSamples, int k, int n, int pos){
        Complex sum1 = new Complex(0, 0);
        Complex sum2 = new Complex(0, 0);
        int end = n/2 - 1;
        for (int i=0; i < end; i++) {
            sum1.add(new Complex(calcCos(i, k, n), calcSin(i, k, n)).mul(currentSamples.get(pos + 2 * i)));
        }
        for (int i=0; i < end; i++) {
            sum2.add(new Complex(calcCos(i, k, n), calcSin(i, k, n)).mul(currentSamples.get(pos + 2 * i + 1)));
        }
        sum2.mul(new Complex(calcCos(1, k, n), calcSin(1, k, n)));
        sum1.add(sum2);
        return sum1;
    }
    public void parallelRun(final DoubleBuffer currentSamples, final double[] magnitudes, final int n, final int pos){
        threads = new Thread[threadsNumber];
        final int step = (n / 2) / threadsNumber;
        for(int i=0; i < threadsNumber; i++){
            final int kStart = i*step;
            final int kStop = (i+1)*step;
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

    public void fft(){
        int n = config.getSampleRate();
        if(n < 0){
            System.out.println(new Date().toString() + ": Using FFT without sample rate.");
            n = samples.getSamples().capacity();
        }

        //precalcs(n);
        DoubleBuffer currentSamples = samples.getSamples().asReadOnlyBuffer();
        for(int pos; (pos = samples.getNSamples(n)) >= 0;) {
            final double[] magnitudes = new double[n / 2];
            if(threadsNumber > 1){
                parallelRun(currentSamples, magnitudes, n, pos);
            }
            else {
                for (int k = 0; k < (n / 2); k++) {
                    Complex sum = calcSum(currentSamples, k, n, pos);
                    magnitudes[k] = sum.magnitude();
                }
            }

            samples.addMagnitudes(pos, magnitudes);
        }
    }

}
