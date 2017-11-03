/*
 * Ported from https://github.com/geel9/SteamAuth/blob/master/SteamAuth/TimeAligner.cs
 */
package com.jsteamkit.steam.guard;

import com.google.common.base.Throwables;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;

public class TimeAligner {

    private static final String TWO_FACTOR_TIME_QUERY
            = "https://api.steampowered.com/ITwoFactorService/QueryTime/v0001";

    private static boolean aligned = false;
    private static long timeDifference = 0;

    public static long getSteamTime() {
        if (!aligned) {
            alignTime();
        }
        return getUnixTime() + timeDifference;
    }

    private static void alignTime() {
        long currentTime = getUnixTime();

        try {
            // https://stackoverflow.com/a/3324964/4313694
            String encodedData = URLEncoder.encode( "steamid=0", "UTF-8" );

            URL url = new URL(TWO_FACTOR_TIME_QUERY);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length", String.valueOf(encodedData.length()));

            try(OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(encodedData.getBytes());
            }

            try(BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                TimeQuery jsonObject = new Gson().fromJson(reader, TimeQuery.class);

                timeDifference = jsonObject.response.server_time - currentTime;
                aligned = true;
            }
        } catch (IOException e) {
            Throwables.propagate(e);
        }
    }

    private static long getUnixTime() {
        return new Date().getTime() / 1000;
    }

    private static class TimeQuery {

        public TimeQueryResponse response;

        private static class TimeQueryResponse {

            long server_time;
        }
    }
}
