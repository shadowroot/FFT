package fft;

import config.Config;
import data.Complex;
import data.Samples;

import java.nio.DoubleBuffer;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FFT {
    Config config = Config.getInstance();
    Samples samples;
    static Map<Integer, Double> cacheSin = new HashMap<Integer, Double>();
    static Map<Integer, Double> cacheCos = new HashMap<Integer, Double>();
    boolean useCache = false;

    public FFT(int sampleRate, Samples samples){
        this.samples = samples;
        config.setSampleRate(sampleRate);
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
        if(!useCache) {
            return Math.sin(-2 * Math.PI * m * k / (n / 2));
        }
        int idx = m*k;
        if(cacheSin.containsKey(idx)){
            return cacheSin.get(idx);
        }
        double val = Math.sin(-2 * Math.PI * m * k / (n/2));
        cacheSin.put(idx, val);
        return val;
    }

    double calcCos(int m, int k,int n){
        if(!useCache){
            return Math.cos(-2 * Math.PI * m * k / (n/2));
        }
        int idx = m*k;
        if(cacheCos.containsKey(idx)){
            return cacheCos.get(idx);
        }
        double val = Math.cos(-2 * Math.PI * m * k / (n/2));
        cacheCos.put(idx, val);
        return val;
    }

    public Samples getSamples() {
        return samples;
    }

    public void fft(){
        int n = config.getSampleRate();
        if(n < 0){
            System.out.println(new Date().toString() + ": Using FFT without sample rate.");
            n = samples.getSamples().capacity();
        }

        //precalcs(n);
        final DoubleBuffer currentSamples = samples.getSamples().asReadOnlyBuffer();
        for(int pos; (pos = samples.getNSamples(n)) >= 0;) {
            double[] magnitudes = new double[n / 2];
            for (int k = 0; k < n / 2; k++) {

                final Complex sum1 = new Complex(0, 0);
                final int finalK = k;
                final int finalN = n;
                final int finalPos = pos;
                Thread thsum1 = new Thread(new Runnable() {
                    public void run() {
                        for (int m = 0; m < finalN / 2; m++) {
                            sum1.add(new Complex(calcCos(m, finalK, finalN), calcSin(m, finalK, finalN)).mul(currentSamples.get(finalPos + 2 * m)));
                        }
                    }
                });
                thsum1.start();

                final Complex sum2 = new Complex(0, 0);
                Thread thsum2 = new Thread(new Runnable() {
                    public void run() {
                        for (int m = 0; m < finalN / 2; m++) {
                            sum2.add(new Complex(calcCos(m, finalK, finalN), calcSin(m, finalK, finalN)).mul(currentSamples.get(finalPos + 2 * m + 1)));
                        }
                        sum2.mul(new Complex(calcCos(1, finalK, finalN), calcSin(1, finalK, finalN)));
                    }
                });
                thsum2.start();

                try {
                    thsum1.join();
                    thsum2.join();
                    sum1.add(sum2);
                    magnitudes[k] = sum1.magnitude();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            samples.addMagnitudes(pos, magnitudes);
        }
    }

}
