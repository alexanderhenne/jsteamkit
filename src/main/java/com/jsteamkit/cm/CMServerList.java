package com.jsteamkit.cm;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class CMServerList {

    private static InitialCMServer bestServer;

    private static List<InitialCMServer> list = new ArrayList<>();
    static {
        // Stockholm, Sweden
        list.add(new InitialCMServer("185.25.180.15", 27017));
        list.add(new InitialCMServer("185.25.180.15", 27018));
        list.add(new InitialCMServer("185.25.180.15", 27019));
        list.add(new InitialCMServer("185.25.180.15", 27020));

        // London, UK
        list.add(new InitialCMServer("162.254.196.40", 27017));
        list.add(new InitialCMServer("162.254.196.40", 27018));
        list.add(new InitialCMServer("162.254.196.40", 27019));
        list.add(new InitialCMServer("162.254.196.40", 27020));
        list.add(new InitialCMServer("162.254.196.40", 27021));

        list.add(new InitialCMServer("162.254.196.41", 27017));
        list.add(new InitialCMServer("162.254.196.41", 27018));
        list.add(new InitialCMServer("162.254.196.41", 27019));
        list.add(new InitialCMServer("162.254.196.41", 27020));
        list.add(new InitialCMServer("162.254.196.41", 27021));

        // Chicago, USA
        list.add(new InitialCMServer("162.254.196.42", 27017));
        list.add(new InitialCMServer("162.254.196.42", 27018));
        list.add(new InitialCMServer("162.254.196.42", 27019));
        list.add(new InitialCMServer("162.254.196.42", 27020));
        list.add(new InitialCMServer("162.254.196.42", 27021));

        list.add(new InitialCMServer("162.254.193.44", 27018));
        list.add(new InitialCMServer("162.254.193.44", 27019));
        list.add(new InitialCMServer("162.254.193.44", 27020));
        list.add(new InitialCMServer("162.254.193.44", 27021));

        list.add(new InitialCMServer("162.254.193.45", 27017));
        list.add(new InitialCMServer("162.254.193.45", 27018));
        list.add(new InitialCMServer("162.254.193.45", 27019));
        list.add(new InitialCMServer("162.254.193.45", 27020));
        list.add(new InitialCMServer("162.254.193.45", 27021));

        // Remove servers that appear to be down
        for (Iterator<InitialCMServer> iterator = list.iterator(); iterator.hasNext();) {
            if (iterator.next().lastPing == -1) {
                iterator.remove();
            }
        }

        // Sort servers as to keep the ones with the lowest pings first in the list
        Collections.sort(CMServerList.list, (o1, o2) -> (int) (o1.lastPing - o2.lastPing));
    }

    public static InitialCMServer getBestServer() {
        if (bestServer == null) {
            InitialCMServer server = list.get(0);
            if (server != null) {
                System.out.println(MessageFormat.format("Found CM server {0}:{1} to be the best server with a ping of {2}ms.",
                        server.ip,
                        String.valueOf(server.port),
                        server.lastPing / 1000000));
                bestServer = server;
            } else {
                System.out.println("The library cannot function without any CM servers.");
            }
        }
        return bestServer;
    }
}
