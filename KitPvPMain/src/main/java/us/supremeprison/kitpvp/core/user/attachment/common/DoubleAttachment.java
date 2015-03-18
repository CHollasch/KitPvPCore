package us.supremeprison.kitpvp.core.user.attachment.common;

import us.supremeprison.kitpvp.core.user.attachment.Attachment;

/**
 * @author Connor Hollasch
 * @since 3/15/2015
 */
public class DoubleAttachment extends Attachment<Double> {

    public DoubleAttachment(String label, Double default_value) {
        super(label, default_value);
    }

    @Override
    public String serialize(Double value) {
        return value.toString();
    }

    @Override
    public Double deserialize(String in) {
        return Double.parseDouble(in);
    }
}
