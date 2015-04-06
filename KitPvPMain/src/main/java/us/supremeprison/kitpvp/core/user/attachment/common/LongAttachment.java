package us.supremeprison.kitpvp.core.user.attachment.common;

import us.supremeprison.kitpvp.core.user.attachment.Attachment;

/**
 * @author Connor Hollasch
 * @since 3/25/2015
 */
public class LongAttachment extends Attachment<Long> {

    public LongAttachment(String label, Long default_value) {
        super(label, default_value);
    }

    @Override
    public String serialize(Long value) {
        return value.toString();
    }

    @Override
    public Long deserialize(String in) {
        return Long.parseLong(in);
    }
}
