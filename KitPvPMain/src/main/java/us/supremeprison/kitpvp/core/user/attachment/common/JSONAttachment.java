package us.supremeprison.kitpvp.core.user.attachment.common;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import us.supremeprison.kitpvp.core.user.attachment.Attachment;

/**
 * @author Connor Hollasch
 * @since 3/16/2015
 */
public class JSONAttachment extends Attachment<JSONObject> {

    public JSONAttachment(String label, JSONObject default_value) {
        super(label, default_value);
    }

    @Override
    public String serialize(JSONObject value) {
        return value.toJSONString();
    }

    @Override
    public JSONObject deserialize(String in) {
        try {
            JSONParser parser = new JSONParser();
            JSONObject object = null;

            object = (JSONObject) parser.parse(in);
            return object;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }
}
