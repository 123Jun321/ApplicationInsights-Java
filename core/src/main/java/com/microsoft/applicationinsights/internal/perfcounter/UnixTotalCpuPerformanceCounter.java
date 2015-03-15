/*
 * AppInsights-Java
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the ""Software""), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package com.microsoft.applicationinsights.internal.perfcounter;

import java.io.BufferedReader;
import java.io.FileReader;

import com.google.common.base.Strings;
import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.telemetry.PerformanceCounterTelemetry;
import com.microsoft.applicationinsights.telemetry.Telemetry;

/**
 * The class supplies the overall cpu usage of the machine.
 *
 * Created by gupele on 3/8/2015.
 */
final class UnixTotalCpuPerformanceCounter extends AbstractUnixPerformanceCounterBase {

    private long[] prevCpuCounters;
    private long prevTotalCpuValue;

    public UnixTotalCpuPerformanceCounter() {
        super("/proc/stat");
        prevCpuCounters = null;
    }

    @Override
    public String getId() {
        return Constants.TOTAL_CPU_PC_ID;
    }

    @Override
    public void report(TelemetryClient telemetryClient) {
        String line = getLineOfData();

        if (!Strings.isNullOrEmpty(line)) {
            String[] stringValues = line.split(" ");
            if (prevCpuCounters == null) {
                firstCounters(stringValues);
                return;
            }

            double totalCpuUsage = calculateTotalCpuUsage(stringValues);
            Telemetry telemetry = new PerformanceCounterTelemetry(
                    Constants.TOTAL_CPU_PC_CATEGORY_NAME,
                    Constants.CPU_PC_COUNTER_NAME,
                    "",
                    totalCpuUsage);

            telemetryClient.track(telemetry);
        }
    }

    private String getLineOfData() {
        BufferedReader bufferedReader = null;

        String line = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(getProcessFile()));
            line = bufferedReader.readLine();
        } catch (Exception e) {
            logError("Error while parsing file: '%s'", e.getMessage());
        } finally {
            if (bufferedReader != null ) {
                try {
                    bufferedReader.close();
                } catch (Exception e) {
                    logError("Error while closing file : '%s'", e.getMessage());
                }
            }
        }

        return line;
    }

    private void firstCounters(String[] stringValues) {
        prevCpuCounters = new long[stringValues.length - 1];
        prevTotalCpuValue = 0;
        for (int i = 0; i < stringValues.length; ++i) {
            long value = Long.parseLong(stringValues[i]);
            prevCpuCounters[i] = value;
            prevTotalCpuValue += value;
        }
    }

    private double calculateTotalCpuUsage(String[] stringValues) {
        long[] cpuCounters = new long[stringValues.length - 1];
        long totalCpuValue = 0;
        double diffIdle = 0.0;
        for (int j = 0, i = 1; i < stringValues.length; ++i, ++j) {
            long value = Long.parseLong(stringValues[i]);
            cpuCounters[j] = value - prevCpuCounters[j];
            prevCpuCounters[i] = value;

            totalCpuValue += value;
            if (i == 3) {
                diffIdle = cpuCounters[j];
            }
        }

        double totalDiff = totalCpuValue - prevTotalCpuValue;
        double result = 100 * ((totalDiff - diffIdle) / (totalDiff));

        prevTotalCpuValue = totalCpuValue;

        return result;
    }
}
