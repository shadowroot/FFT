package data;

import config.Config;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.DoubleBuffer;
import java.nio.charset.StandardCharsets;
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

    public void encode(Config config, Samples samples, OutputStream os) throws Exception {
        Map<String, Object> values = new HashMap<>();
        JSONObject obj = new JSONObject();
        JSONObject optsMap = new JSONObject();
        for(String key: values.keySet()){
            optsMap.put(key, values.get(key));
        }
        obj.put(CONFIG_MAP, optsMap);
        if(config.ShouldWriteRAWData()) {
            obj.put(RAW_DATA, convertFromDoubleBuffer(samples.getSamples()));
        }
        JSONArray magnitudes = new JSONArray();
        for(Object magnitudeValues : samples.getMagnitudes()) {
            magnitudes.put(convertFromDoubleArray((double[])magnitudeValues));
        }
        obj.put(MAGNITUDES, magnitudes);
        os.write(obj.toString().getBytes());
    }

    private JSONArray convertFromDoubleArray(double[] db){
        JSONArray array = new JSONArray();
        for(int i=0; i < db.length; i++){
            array.put(db[i]);
        }
        return array;
    }

    private JSONArray convertFromDoubleBuffer(List db){
        JSONArray array = new JSONArray();
        for(int i=0; i < db.size(); i++){
            array.put(db.get(i));
        }
        return array;
    }

    private List<Double> convertToDoubleList(JSONArray array){
        ArrayList<Double> values = new ArrayList<>();
        for(Object val : array){
            values.add((Double)val);
        }
        return values;
    }

    private double[] convertToDoubleArray(JSONArray array){
        double[] ret = new double[array.length()];
        int idx = 0;
        for(Object val : array){
            ret[idx] = (Double)val;
            idx++;
        }
        return ret;
    }

    public Samples decode(Config config, InputStream is) throws IOException {
        JSONParser parser = new JSONParser();
        Samples samples = null;
        try {
            String jsonString = IOUtils.toString(is, String.valueOf(StandardCharsets.UTF_8));
            Object obj = parser.parse(jsonString);
            JSONObject json = (JSONObject)obj;
            JSONObject configOpts = (JSONObject)json.get(CONFIG_MAP);
            Map<String, Object> newOpts = new HashMap<>();
            for(String key : configOpts.keySet()){
                newOpts.put(key, configOpts.get(key));
            }
            config.setKwOptions(newOpts);
            samples = new Samples<Double>();
            JSONArray dataArrays = (JSONArray) json.get(RAW_DATA);
            for(Object rawData : dataArrays) {
                samples.addSamples(convertToDoubleList((JSONArray)rawData));
            }
            if(json.has(MAGNITUDES)) {
                JSONArray magnitudeArrays = (JSONArray) json.get(MAGNITUDES);
                List<double[]> newMagnitudes = new ArrayList<>();
                for(Object magnitudeArrayO : magnitudeArrays){
                    JSONArray magnitudeArray = (JSONArray)magnitudeArrayO;
                    newMagnitudes.add(convertToDoubleArray(magnitudeArray));
                }
                samples.setMagnitudes(newMagnitudes);
            }
        }catch (ParseException e) {
            e.printStackTrace();
        }
        return samples;
    }
}
