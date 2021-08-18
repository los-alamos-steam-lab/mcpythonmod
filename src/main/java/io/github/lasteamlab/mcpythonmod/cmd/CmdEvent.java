package io.github.lasteamlab.raspberryjuice2.cmd;

import io.github.lasteamlab.raspberryjuice2.RemoteSession;

public class CmdEvent {
    private final String preFix = "events.";
    private RemoteSession session;

    public CmdEvent(RemoteSession session) {
        this.session = session;
    }

    public void execute(String command, String[] args) {
        // events.clear
        if (command.equals("clear")) {
            session.interactEventQueue.clear();
            session.chatPostedQueue.clear();

            // events.block.hits
        } else if (command.equals("block.hits")) {
			session.send(session.getBlockHits());

            // events.chat.posts
        } else if (command.equals("chat.posts")) {
			session.send(session.getChatPosts());

            // events.projectile.hits
        } else if(command.equals("projectile.hits")) {
        	session.send(session.getProjectileHits());

        } else {
            session.plugin.getLogger().warning(preFix + command + " is not supported.");
            session.send("Fail," + preFix + command + " is not supported.");
        }
    }
}
