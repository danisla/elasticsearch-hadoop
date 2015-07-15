/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.elasticsearch.hadoop.util;

import java.util.Calendar;

import javax.xml.bind.DatatypeConverter;

/**
 * Utility used for parsing date ISO8601.
 *
 */
public abstract class DateUtils {

    // Parses ISO date through the JDK XML bind class. However the spec doesn't support all ISO8601 formats which this class tries to address
    // in particular Time offsets from UTC are available in 3 forms:
    //
    // Time offsets from UTC
    //
    // The offset from UTC is appended to the time in the same way that 'Z' was above, in the form ±hh]:[mm], ±[hh][mm], or ±[hh].
    // XML Bind supports only the first one.

    public static Calendar parseDateJdk(String value) {
        // check for colon in the time offset
        int timeZoneIndex = value.indexOf("T");
        int dashIndex = value.indexOf("-");
        int secondDashIndex = value.indexOf("-", dashIndex + 1);

        if (timeZoneIndex > 0) {
            int sign = value.indexOf("+", timeZoneIndex);
            if (sign < 0) {
                sign = value.indexOf("-", timeZoneIndex);
            }

            // +4 means it's either hh:mm or hhmm
            if (sign > 0) {
                // +3 points to either : or m
                int colonIndex = sign + 3;
                // +hh - need to add :mm
                if (colonIndex >= value.length()) {
                    value = value + ":00";
                }
                else if (value.charAt(colonIndex) != ':') {
                    value = value.substring(0, colonIndex) + ":" + value.substring(colonIndex);
                }
            }

            if (dashIndex > 0 && secondDashIndex == -1) {
              // We have an ISO 8601 ordinal date time, convert to xsd:dateTime
              // so that DatetypeConverter can parse it into a Calendar.
              // There may be a better way to do this, but hey, it's probably faster than SimpleDateFormat.

              Calendar cal = Calendar.getInstance();
              int year = Integer.parseInt(value.substring(0, dashIndex));
              int doy = Integer.parseInt(value.substring(dashIndex+1, timeZoneIndex));
              cal.set(Calendar.YEAR, year);
              cal.set(Calendar.DAY_OF_YEAR, doy);
              value = String.format("%1$tY-%1$tm-%1$td", cal) + value.substring(timeZoneIndex);
            }
        }

        return DatatypeConverter.parseDateTime(value);
    }
}
