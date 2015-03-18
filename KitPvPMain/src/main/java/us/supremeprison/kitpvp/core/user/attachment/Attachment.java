package us.supremeprison.kitpvp.core.user.attachment;

import lombok.Getter;

/**
 * @author Connor Hollasch
 * @since 3/13/2015
 */
public abstract class Attachment<K> {

    @Getter
    private String attachment_label;

    @Getter
    private K default_value;

    public Attachment(String label, K default_value) {
        this.attachment_label = label;
        this.default_value = default_value;
    }

    public abstract String serialize(K value);

    public abstract K deserialize(String in);
}
