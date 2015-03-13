package us.supremeprison.kitpvp.core.user.attachment;

import lombok.Getter;

import java.io.Serializable;

/**
 * @author Connor Hollasch
 * @since 3/13/2015
 */
public class Attachment<K extends Serializable> {

    @Getter
    private String attachment_label;

    private K value;
    private K default_value;

    public Attachment(String label, K default_value) {
        this.attachment_label = label;
        this.default_value = default_value;
    }

    public K get() {
        return value;
    }

    public void put(K attachment_value) {
        this.value = attachment_value;
    }

    public K getDefault_value() {
        return default_value;
    }
}
