package us.supremeprison.kitpvp.core.user.attachment;

import java.util.Collection;
import java.util.HashMap;

/**
 * @author Connor Hollasch
 * @since 3/13/2015
 */
public class AttachmentManager {

    private HashMap<String, Attachment<?>> user_attachments = new HashMap<>();

    public void put(Attachment<?> attachment) {
        user_attachments.put(attachment.getAttachment_label(), attachment);
    }

    public Collection<Attachment<?>> getAllAttachments() {
        return user_attachments.values();
    }

    public Attachment<?> getAttachment(String label) {
        return user_attachments.get(label);
    }
}
