package kitpvp.core.user.attachment.common;

import kitpvp.core.user.attachment.Attachment;

/**
 * @author Connor Hollasch
 * @since 3/15/2015
 */
public class StringAttachment extends Attachment<String> {

    public StringAttachment(String label, String default_value) {
        super(label, default_value);
    }

    @Override
    public String serialize(String value) {
        return value;
    }

    @Override
    public String deserialize(String in) {
        return in;
    }
}
