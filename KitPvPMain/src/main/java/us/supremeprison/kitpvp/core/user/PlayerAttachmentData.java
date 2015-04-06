package us.supremeprison.kitpvp.core.user;

import us.supremeprison.kitpvp.core.user.attachment.Attachment;
import us.supremeprison.kitpvp.core.user.attachment.AttachmentManager;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Connor Hollasch
 * @since 3/15/2015
 */
public class PlayerAttachmentData {

    private ConcurrentHashMap<String, Object> attachments = new ConcurrentHashMap<>();

    public PlayerAttachmentData(ConcurrentHashMap<String, Object> initial) {
        if (initial != null)
            this.attachments = initial;
    }

    public void registerAttachment(Attachment attachment) {
        attachments.put(attachment.getAttachment_label().toLowerCase(), attachment.getDefault_value());
    }

    public void putDeserializedAttachment(String label, Object value) {
        attachments.put(label.toLowerCase(), value);
    }

    public void unregisterAttachment(Attachment attachment) {
        attachments.remove(attachment.getAttachment_label().toLowerCase());
    }

    public boolean isRegistered(Attachment attachment) {
        return attachments.containsKey(attachment.getAttachment_label().toLowerCase());
    }

    public void changeAttachment(String label, Object value) {
        if (attachments.containsKey(label.toLowerCase())) {
            attachments.remove(label.toLowerCase());
            attachments.put(label.toLowerCase(), value);
        }
    }

    public <T> T getAttachment(String label) {
        if (attachments.containsKey(label.toLowerCase())) {
            return (T) attachments.get(label.toLowerCase());
        } else {
            AttachmentManager defaultManager = User.getAttachments_manager();
            if (defaultManager.getAttachment(label) != null)
                return (T) defaultManager.getAttachment(label).getDefault_value();
            else
                return null;
        }
    }

    protected ConcurrentHashMap<String, Object> protectedGetAllAttachments() {
        return attachments;
    }
}
