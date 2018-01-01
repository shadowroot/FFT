package data;

import org.jetbrains.kotlin.codegen.range.ArrayRangeValue;

import java.io.Serializable;
import java.util.*;
import java.util.logging.Logger;

/**
 * Samples class
 */
public class Samples<T extends Number, RESULT_T extends Number> implements Serializable{

    private List<List<T> > samples;
    private List<List<RESULT_T> > magnitudes = new ArrayList<>();
    private List<Date> collectedDates = new ArrayList<>();
    private int processedSamples = 0;
    private Logger logger;

    public Samples(){
        logger = Logger.getLogger("FFT-Samples");
        samples = new ArrayList<>();
    }

    public void addSamples(Collection<T> array){
        samples.add(new ArrayList<>(array));
    }

    public void addSamples(T[] array){
        List<T> list = new ArrayList<>();
        for(int i=0; i < array.length; i++) {
            list.add(array[i]);
        }
        samples.add(list);
    }

    public void setSamples(List<T> samples) {
        this.samples.add(samples);
    }

    public void setMagnitudes(List<List<RESULT_T> > magnitudes) {
        this.magnitudes = magnitudes;
    }

    public void addSamples(Samples samples){
        this.samples.addAll(samples.getSamples());
    }

    public List<T> getNSamples(int n){
        if(n <= 0){
            return null;
        }
        List<T> ret = new ArrayList<>();
        int readSamples = 0;
        int sampleIdx = 0;
        for(int i=0 ; i < samples.size() ; i++){
            int samplesSize = samples.get(i).size();
            if((readSamples + samplesSize) < processedSamples){
                readSamples += samplesSize;
                continue;
            }
            if((processedSamples - readSamples) < samplesSize){
                sampleIdx = processedSamples - readSamples;
                readSamples += sampleIdx;
            }
            if(readSamples == processedSamples) {
                if ((samplesSize - sampleIdx) <= n) {
                    ret.addAll(sampleIdx, samples.get(i));
                }
                else{
                    System.arraycopy(samples.get(i), sampleIdx, ret, ret.size() - 1,n - ret.size());
                }
                sampleIdx = 0;
            }
        }
        return ret;
    }

    public List<T> getSample(int idx){
        if((samples.size() - 1) > idx){
            return null;
        }
        return samples.get(idx);
    }

    public void addMagnitudes(List<RESULT_T> magnitudes){
        if(this.magnitudes == null){
            this.magnitudes = new ArrayList<>();
        }
        this.magnitudes.add(magnitudes);
    }

    public List<List<RESULT_T> > getMagnitudes() {
        return magnitudes;
    }

    public List<List<T> > getSamples() {
        return samples;
    }

    public int size(){
        return samples.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Samples<T, RESULT_T> samples1 = (Samples<T, RESULT_T>) o;
        return  Objects.equals(samples, samples1.samples) &&
                Objects.equals(magnitudes, samples1.magnitudes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(samples, magnitudes, processedSamples, logger);
    }
}
