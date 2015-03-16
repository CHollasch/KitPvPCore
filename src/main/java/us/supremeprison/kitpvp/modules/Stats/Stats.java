package us.supremeprison.kitpvp.modules.Stats;

import us.supremeprison.kitpvp.core.module.Module;
import us.supremeprison.kitpvp.core.user.User;
import us.supremeprison.kitpvp.core.user.attachment.common.IntegerAttachment;

/**
 * @author Connor Hollasch
 * @since 3/15/2015
 */
public class Stats extends Module {

    @Override
    public void onEnable() {
        User.getAttachments_manager().put(new IntegerAttachment("kills", 0));
        User.getAttachments_manager().put(new IntegerAttachment("deaths", 0));
    }
}
