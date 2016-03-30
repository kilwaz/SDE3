package application.utils;

import application.error.Error;
import org.apache.log4j.Logger;

import java.io.*;

public class Serializer {

    private static Logger log = Logger.getLogger(Serializer.class);

    // Return the input stream ready to be used of the object passed in, generally this will be saved to the database straight away
    public static InputStream serializeToInputStream(Object object) {
        InputStream inputStream = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            inputStream = new ByteArrayInputStream(baos.toByteArray());
            baos.close();
            oos.close();
        } catch (NotSerializableException ex) {
            Error.NOT_SERIALIZABLE_OBJECT.record().additionalInformation(object.toString()).hideStackInLog().create(ex);
        } catch (IOException ex) {
            Error.SERIALIZE_OBJECT.record().create(ex);
        }

        return inputStream;
    }

    // Given an input stream construct the object
    public static Object deserialize(InputStream inputStream) {
        Object output = null;
        try {
            byte[] buff = new byte[8000];
            int bytesRead;

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            while ((bytesRead = inputStream.read(buff)) != -1) {
                baos.write(buff, 0, bytesRead);
            }

            byte[] data = baos.toByteArray();
            baos.close();

            ByteArrayInputStream bin = new ByteArrayInputStream(data);

            ObjectInputStream in = new ObjectInputStream(bin);
            output = in.readObject();

            bin.close();
        } catch (IOException | ClassNotFoundException ex) {
            Error.DESERIALIZE_OBJECT.record().create(ex);
        }

        return output;
    }
}
