package fft;

import config.Config;
import data.Complex;
import data.Samples;

import java.nio.DoubleBuffer;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FFT {
    protected Config config = Config.getInstance();
    protected Samples samples;
    protected static Map<Integer, Double> cacheSin = new HashMap<Integer, Double>();
    protected static Map<Integer, Double> cacheCos = new HashMap<Integer, Double>();
    protected Thread threads[];
    protected int threads_number = 4;

    public FFT(int sampleRate, Samples samples, int threads_number){
        this.samples = samples;
        config.setSampleRate(sampleRate);
        this.threads_number = threads_number;
    }

    public FFT(Samples samples){
        this.samples = samples;
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
        sins = new double[valid_freqs * n];
        coss = new double[valid_freqs * n];
        for(int k=0; k < valid_freqs; k++) {
            for (int m = 0; m < valid_freqs; m++) {
                sins[m*k] = Math.sin(-2 * Math.PI * m * k / valid_freqs);
                coss[m*k] = Math.cos(-2 * Math.PI * m * k / valid_freqs);
            }
        }
    }
    */

    double calcSin(int m, int k,int n){
        return Math.sin(-2 * Math.PI * m * k / (n / 2));
    }

    double calcCos(int m, int k,int n){
        return Math.cos(-2 * Math.PI * m * k / (n/2));
    }

    public Samples getSamples() {
        return samples;
    }

    Complex calcSum1(final DoubleBuffer currentSamples, int k, int n, int pos){
        threads = new Thread[threads_number];
        Complex sum1 = new Complex(0, 0);
        final Complex[] sums = new Complex[threads_number];
        final int finalK = k;
        final int finalN = n;
        final int finalPos = pos;
        final int step = (finalN / 2) / threads_number;
        for(int i=0; i < threads_number; i++){
            final int finalI = i;
            sums[i] = new Complex(0,0);
            threads[i] = new Thread(new Runnable() {
                public void run() {
                    int m_end = (finalI+1)*step;
                    sums[finalI] = sum1Job(currentSamples, finalI*step, m_end, finalK, finalN, finalPos);
                }
            });
            threads[i].start();
        }
        for(int i=0; i < threads_number; i++){
            try {
                threads[i].join();
                sum1.add(sums[i]);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return sum1;
    }

    Complex calcSum2(final DoubleBuffer currentSamples, int k, int n, int pos){
        threads = new Thread[threads_number];
        Complex sum2 = new Complex(0, 0);
        final Complex[] sums = new Complex[threads_number];
        final int finalK = k;
        final int finalN = n;
        final int finalPos = pos;
        final int step = (finalN / 2) / threads_number;
        for(int i=0; i < threads_number; i++){
            final int finalI = i;
            sums[i] = new Complex(0,0);
            threads[i] = new Thread(new Runnable() {
                public void run() {
                    int m_end = (finalI+1)*step;
                    sums[finalI] = sum2Job(currentSamples, finalI*step, m_end, finalK, finalN, finalPos);
                }
            });
            threads[i].start();
        }
        for(int i=0; i < threads_number; i++){
            try {
                threads[i].join();
                sum2.add(sums[i]);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return sum2;
    }


    Complex sum1Job(DoubleBuffer currentSamples, int m, int m_end, int k, int n, int pos){
        Complex sum1 = new Complex(0, 0);
        for (; m < m_end; m++) {
            sum1.add(new Complex(calcCos(m, k, n), calcSin(m, k, n)).mul(currentSamples.get(pos + 2 * m)));
        }
        return sum1;
    }

    Complex sum2Job(DoubleBuffer currentSamples, int m, int m_end, int k, int n, int pos){
        Complex sum2 = new Complex(0, 0);
        for (; m < m_end; m++) {
            sum2.add(new Complex(calcCos(m, k, n), calcSin(m, k, n)).mul(currentSamples.get(pos + 2 * m + 1)));
        }
        sum2.mul(new Complex(calcCos(1, k, n), calcSin(1, k, n)));
        return sum2;
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
            double[] magnitudes = new double[n / 2];
            for (int k = 0; k < n / 2; k++) {
                Complex sum1 = calcSum1(currentSamples, k, n, pos);
                Complex sum2 = calcSum2(currentSamples, k, n, pos);
                sum1.add(sum2);
                magnitudes[k] = sum1.magnitude();
            }
            samples.addMagnitudes(pos, magnitudes);
        }
    }

}
