/*
Copyright (c) 2014, Aalborg University
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the <organization> nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using WifiSnifferPositioningService.radiomap;

namespace WifiSnifferPositioningService.extensions.wifi
{
    public static class SnifferWifiMeasurementExtensions
    {
        //We are not too concerned with decimal numbers, so we return the avg as an int
        //(Moreover, this is a legacy from the WinMobile client and the Streamspin server)
        public static double getAvgDbM(this SnifferWifiMeasurement source, string mac)
        {
            int totalVal = 0;
            int totalCount = 0; //the total number of distinct values
            //calculate the total
            foreach (SnifferHistogram sniff in source.SnifferHistograms)
            {
                if (sniff.Mac == mac)
                {
                    totalVal += sniff.value * sniff.count;
                    totalCount += sniff.count;
                }
            }              

            return totalVal / totalCount;        
        }     

        public static List<string> getMACs(this SnifferWifiMeasurement source)
        {
            List<String> result = new List<string>();
            foreach (SnifferHistogram h in source.SnifferHistograms)
            {
                if (!result.Contains(h.Mac))
                    result.Add(h.Mac);
            }
            return result;
        }
        
        public static bool containsMac(this SnifferWifiMeasurement source, string mac)
        {
            foreach (SnifferHistogram h in source.SnifferHistograms)
            {
                if (h.Mac.Equals(mac))
                    return true;
            }
            return false;
        }
    }
}
