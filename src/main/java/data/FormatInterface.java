package data;

import config.Config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface FormatInterface {

    void encode(Config config, Samples samples, OutputStream os) throws Exception;
    Samples decode(Config config, InputStream is) throws IOException;

}