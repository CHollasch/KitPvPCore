package kitpvp.core.user.attachment.common;

import kitpvp.core.user.attachment.Attachment;

/**
 * @author Connor Hollasch
 * @since 3/15/2015
 */
public class IntegerAttachment extends Attachment<Integer> {

    public IntegerAttachment(String label, Integer default_value) {
        super(label, default_value);
    }

    @Override
    public String serialize(Integer value) {
        return value.toString();
    }

    @Override
    public Integer deserialize(String in) {
        return Integer.parseInt(in);
    }
}
