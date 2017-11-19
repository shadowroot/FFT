package config;

import java.util.HashMap;
import java.util.Map;

public class Config {
    private static final String SAMPLE_RATE = "SAMPLE_RATE";
    private static final String MAG_IN_DB = "MAG_IN_DB";
    private static final String RAW_DATA = "RAW_DATA";
    private static Config instance = null;
    private Map<String, Object> kwOptions = new HashMap<String, Object>();

    private Config(int sampleRate){
        addOption(SAMPLE_RATE, sampleRate);
        addOption(MAG_IN_DB, true);
        addOption(RAW_DATA, false);
    }

    private Config(){
        addOption(MAG_IN_DB, false);
        addOption(RAW_DATA, true);
    }

    public static Config getInstance() {
        if(instance == null){
            instance = new Config();
        }
        return instance;
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

    public static void setInstance(Config instance) {
        Config.instance = instance;
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

    public boolean ShouldWriteRAWData(){
        return (Boolean) kwOptions.get(RAW_DATA);
    }

    public void setSampleRate(int sampleRate) {
        kwOptions.put(SAMPLE_RATE, sampleRate);
    }
}
