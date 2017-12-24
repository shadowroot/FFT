package data;

import config.Config;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON data formatter
 */
public class JSONDataFormat implements FormatInterface {
    private static String CONFIG_MAP = "config";
    private static String RAW_DATA = "data";
    private static String MAGNITUDES = "magnitudes";
    private static final String encoding = "UTF-8";

    public void encode(Config config, Samples samples, OutputStream os) throws Exception {
        OutputStreamWriter osw = new OutputStreamWriter(os, encoding);
        Map<String, Object> values = new HashMap<>();
        JSONObject obj = new JSONObject();
        JSONObject optsMap = new JSONObject();
        for(String key: values.keySet()){
            optsMap.put(key, values.get(key));
        }
        obj.put(CONFIG_MAP, optsMap);
        if(config.ShouldWriteRAWData()) {
            JSONArray array = new JSONArray();
            array.addAll(samples.getSamples());
            obj.put(RAW_DATA, array);
        }
        JSONArray magnitudes = new JSONArray();
        List<List<Double> > mags = samples.getMagnitudes();
        for(int i=0; i < mags.size(); i++) {
            JSONArray innerJson = new JSONArray();
            innerJson.addAll(mags.get(i));
            magnitudes.add(innerJson);
        }
        obj.put(MAGNITUDES, magnitudes);
        osw.write(obj.toString());
        osw.close();
    }

    public Samples decode(Config config, InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        InputStreamReader isr = new InputStreamReader(is, encoding);
        int cr;
        while((cr = isr.read()) >= 0) {
            sb.append((char) cr);
        }
        isr.close();
        JSONParser parser = new JSONParser();
        Samples samples = new Samples<Double, Double>();
        try {
            String jsonString = sb.toString();
            JSONObject json = (JSONObject)parser.parse(jsonString);
            if(json.containsKey(CONFIG_MAP)) {
                JSONObject configOpts = (JSONObject) json.get(CONFIG_MAP);
                for (Object key1 : configOpts.keySet()) {
                    String key = (String) key1;
                    config.addOption(key, configOpts.get(key));
                }
            }
            if(json.containsKey(RAW_DATA)) {
                JSONArray dataArrays = (JSONArray) json.get(RAW_DATA);
                List<Double> data = new ArrayList<>();
                for(Object value : dataArrays){
                    data.add((Double)value);
                }
                samples.addSamples(data);
            }
            if(json.containsKey(MAGNITUDES)) {
                JSONArray magnitudeArrays = (JSONArray) json.get(MAGNITUDES);
                List<List<Double> > mags = new ArrayList<>();
                for(Object arr: magnitudeArrays) {
                    JSONArray jsonArr = (JSONArray) arr;
                    List<Double> magList = new ArrayList<>();
                    for(Object val : jsonArr){
                        magList.add((Double) val);
                    }
                    mags.add(magList);
                }
                samples.setMagnitudes(mags);
            }
        }catch (ParseException e) {
            e.printStackTrace();
        }
        return samples;
    }
}
