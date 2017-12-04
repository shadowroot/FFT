package config;

import java.util.HashMap;
import java.util.Map;

public class Config {
    public static final String SAMPLE_RATE = "SAMPLE_RATE";
    public static final String MAG_IN_DB = "MAG_IN_DB";
    public static final String RAW_DATA = "RAW_DATA";
    private Map<String, Object> kwOptions = new HashMap<String, Object>();

    public Config(int sampleRate){
        addOption(SAMPLE_RATE, sampleRate);
        addOption(MAG_IN_DB, true);
        addOption(RAW_DATA, false);
    }


    public void addOption(String key, Object value){
        kwOptions.put(key, value);
    }

    public void removeOption(String key){
        if(kwOptions.containsKey(key)){
            kwOptions.remove(key);
        }
    }

    public Map<String, Object> getKwOptions() {
        return kwOptions;
    }

    public void setKwOptions(Map<String, Object> kwOptions) {
        this.kwOptions = kwOptions;
    }

    public int getSampleRate() {
        if(kwOptions.containsKey(SAMPLE_RATE)){
            return (Integer)kwOptions.get(SAMPLE_RATE);
        }
        return -1;
    }

    public void writeRAWDATA(){
        kwOptions.put(RAW_DATA, true);
    }

    public void noWriteRAWDATA(){
        kwOptions.put(RAW_DATA, false);
    }

    public boolean ShouldWriteRAWData(){
        return (Boolean) kwOptions.get(RAW_DATA);
    }

    public void setSampleRate(int sampleRate) {
        kwOptions.put(SAMPLE_RATE, sampleRate);
    }
}
