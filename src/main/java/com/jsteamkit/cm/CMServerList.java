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
        // Sourced from https://api.steampowered.com/ISteamDirectory/GetCMList/v1/?cellid=0
        list.add(new InitialCMServer("162.254.198.130", 27018));
        list.add(new InitialCMServer("162.254.198.132", 27019));
        list.add(new InitialCMServer("162.254.198.131", 27017));
        list.add(new InitialCMServer("162.254.198.133", 27018));
        list.add(new InitialCMServer("162.254.198.131", 27019));
        list.add(new InitialCMServer("162.254.198.133", 27019));
        list.add(new InitialCMServer("162.254.198.131", 27018));
        list.add(new InitialCMServer("162.254.198.130", 27017));
        list.add(new InitialCMServer("162.254.198.132", 27018));
        list.add(new InitialCMServer("162.254.198.133", 27017));
        list.add(new InitialCMServer("162.254.198.132", 27017));
        list.add(new InitialCMServer("162.254.198.130", 27019));
        list.add(new InitialCMServer("162.254.197.40", 27018));
        list.add(new InitialCMServer("162.254.196.83", 27018));
        list.add(new InitialCMServer("162.254.196.84", 27017));
        list.add(new InitialCMServer("162.254.196.68", 27017));
        list.add(new InitialCMServer("155.133.248.52", 27018));
        list.add(new InitialCMServer("162.254.196.67", 27017));
        list.add(new InitialCMServer("155.133.248.53", 27018));
        list.add(new InitialCMServer("155.133.248.51", 27019));
        list.add(new InitialCMServer("162.254.196.68", 27019));
        list.add(new InitialCMServer("162.254.196.83", 27017));
        list.add(new InitialCMServer("162.254.197.42", 27017));
        list.add(new InitialCMServer("155.133.248.53", 27019));
        list.add(new InitialCMServer("155.133.248.50", 27017));
        list.add(new InitialCMServer("162.254.196.67", 27019));
        list.add(new InitialCMServer("162.254.196.83", 27019));
        list.add(new InitialCMServer("155.133.248.50", 27019));
        list.add(new InitialCMServer("155.133.248.53", 27017));
        list.add(new InitialCMServer("155.133.248.51", 27017));
        list.add(new InitialCMServer("155.133.248.50", 27018));
        list.add(new InitialCMServer("162.254.196.67", 27018));
        list.add(new InitialCMServer("162.254.197.40", 27017));
        list.add(new InitialCMServer("162.254.196.84", 27019));
        list.add(new InitialCMServer("162.254.197.181", 27018));
        list.add(new InitialCMServer("162.254.197.40", 27019));
        list.add(new InitialCMServer("155.133.248.52", 27017));
        list.add(new InitialCMServer("155.133.248.52", 27019));
        list.add(new InitialCMServer("162.254.196.68", 27018));
        list.add(new InitialCMServer("155.133.248.51", 27018));
        list.add(new InitialCMServer("162.254.197.181", 27019));
        list.add(new InitialCMServer("162.254.197.42", 27018));
        list.add(new InitialCMServer("162.254.197.180", 27019));
        list.add(new InitialCMServer("162.254.196.84", 27018));
        list.add(new InitialCMServer("162.254.197.42", 27019));
        list.add(new InitialCMServer("162.254.197.181", 27017));
        list.add(new InitialCMServer("162.254.197.180", 27018));
        list.add(new InitialCMServer("162.254.197.180", 27017));
        list.add(new InitialCMServer("146.66.152.10", 27019));
        list.add(new InitialCMServer("146.66.152.10", 27018));
        list.add(new InitialCMServer("146.66.152.11", 27018));
        list.add(new InitialCMServer("146.66.152.11", 27017));
        list.add(new InitialCMServer("146.66.152.11", 27019));
        list.add(new InitialCMServer("146.66.152.10", 27017));
        list.add(new InitialCMServer("185.25.182.77", 27019));
        list.add(new InitialCMServer("185.25.182.77", 27018));
        list.add(new InitialCMServer("185.25.182.76", 27018));
        list.add(new InitialCMServer("185.25.182.77", 27017));
        list.add(new InitialCMServer("185.25.182.76", 27019));
        list.add(new InitialCMServer("185.25.182.76", 27017));
        list.add(new InitialCMServer("162.254.192.101", 27019));
        list.add(new InitialCMServer("162.254.192.101", 27017));
        list.add(new InitialCMServer("162.254.192.101", 27018));
        list.add(new InitialCMServer("162.254.192.100", 27018));
        list.add(new InitialCMServer("162.254.192.108", 27017));
        list.add(new InitialCMServer("162.254.192.100", 27019));
        list.add(new InitialCMServer("162.254.192.108", 27018));
        list.add(new InitialCMServer("162.254.192.100", 27017));
        list.add(new InitialCMServer("162.254.192.108", 27019));
        list.add(new InitialCMServer("162.254.192.109", 27018));
        list.add(new InitialCMServer("155.133.246.68", 27017));
        list.add(new InitialCMServer("162.254.192.109", 27019));
        list.add(new InitialCMServer("162.254.192.109", 27017));
        list.add(new InitialCMServer("155.133.246.68", 27018));
        list.add(new InitialCMServer("155.133.246.69", 27017));
        list.add(new InitialCMServer("155.133.246.69", 27018));
        list.add(new InitialCMServer("146.66.155.101", 27018));
        list.add(new InitialCMServer("146.66.155.101", 27017));
        list.add(new InitialCMServer("155.133.246.68", 27019));
        list.add(new InitialCMServer("146.66.155.100", 27019));
        list.add(new InitialCMServer("146.66.155.101", 27019));
        list.add(new InitialCMServer("155.133.246.69", 27019));
        list.add(new InitialCMServer("146.66.155.100", 27018));
        list.add(new InitialCMServer("146.66.155.100", 27017));
        list.add(new InitialCMServer("162.254.193.47", 27017));
        list.add(new InitialCMServer("162.254.193.46", 27017));
        list.add(new InitialCMServer("162.254.193.6", 27017));
        list.add(new InitialCMServer("162.254.193.6", 27019));
        list.add(new InitialCMServer("162.254.193.46", 27019));
        list.add(new InitialCMServer("162.254.193.7", 27017));
        list.add(new InitialCMServer("162.254.193.46", 27018));
        list.add(new InitialCMServer("162.254.193.47", 27018));
        list.add(new InitialCMServer("162.254.193.7", 27018));
        list.add(new InitialCMServer("162.254.193.47", 27019));
        list.add(new InitialCMServer("162.254.193.7", 27019));
        list.add(new InitialCMServer("162.254.193.6", 27018));
        list.add(new InitialCMServer("155.133.230.50", 27019));
        list.add(new InitialCMServer("155.133.230.34", 27018));
        list.add(new InitialCMServer("155.133.230.50", 27018));
        list.add(new InitialCMServer("155.133.230.34", 27017));

        // Remove servers that appear to be down
        for (Iterator<InitialCMServer> iterator = list.iterator(); iterator.hasNext();) {
            InitialCMServer server = iterator.next();
            if (server.lastPing == -1) {
                System.out.println("Warning: the CM server " + server.ip + ":" + server.port + " appears to be down.");
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
