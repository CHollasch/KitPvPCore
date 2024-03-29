package us.supremeprison.kitpvp.core.user.attachment;

import us.supremeprison.kitpvp.core.KitPvP;

import java.util.Collection;
import java.util.HashMap;

/**
 * @author Connor Hollasch
 * @since 3/13/2015
 */
public class AttachmentManager {

    private HashMap<String, Attachment<?>> user_attachments = new HashMap<>();

    public void put(Attachment<?> attachment) {
        user_attachments.put(attachment.getAttachment_label().toLowerCase(), attachment);
        KitPvP.getPlugin_instance().logMessage("New attachment (&d" + attachment.getDefault_value().getClass().getName() + ", " + attachment.getAttachment_label() + "&e) registered!");
    }

    public Collection<Attachment<?>> getAllAttachments() {
        return user_attachments.values();
    }

    public HashMap<String, Attachment<?>> getRegistered_attachments() {
        return user_attachments;
    }

    public Attachment<?> getAttachment(String label) {
        return user_attachments.get(label.toLowerCase());
    }
}
