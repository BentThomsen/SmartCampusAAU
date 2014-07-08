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
DISCLAIMED. IN NO EVENT SHALL AAlBORG UNIVERSITY BE LIABLE FOR ANY
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

namespace com.smartcampus.wifi
{
    public class WifiMeasurement
    {
        private DateTime measTimeStart, measTimeEnd;
        
        //we save a histogram of recorded values for each mac. 
        //E.g., if mac MAC1 has registered value -40 6 times, and value -45 5 times, the corresponding histogram would be
        //histogram[MAC1][-40] = 6; histogram[MAC1][-45] = 5.
        private Dictionary<String, Dictionary<int, int>> histogram = new Dictionary<String, Dictionary<int, int>>();
        private Dictionary<String, MacInfo> additionalInfo = new Dictionary<String, MacInfo>();
    
        private const int no_vertex = 42;
    
        public static String discardLastCharIfMacIsFull(String mac)
        {
            String result;
            //we strip away the last character of the mac address
            if (mac.Length == 17)
                result = mac.Substring(0, 16);
            else
                result = mac;
            return result;
        }
                
        public WifiMeasurement()
        {
            measTimeStart = DateTime.Now;
            measTimeEnd = DateTime.Now;
        }

        public void addValue(String mac, int ssVal)
        {
            //we discard the last char if it is a full mac as an 
            //AP can have several 'sub' macs for the same AP.
            mac = discardLastCharIfMacIsFull(mac);
            //Add mac if not contained
            if (!histogram.ContainsKey(mac))
            {
                histogram.Add(mac, new Dictionary<int, int>());            
            } 
            //Add ssVal if not contained
            if (!histogram[mac].ContainsKey(ssVal))
            {
                histogram[mac].Add(ssVal, 0);
            }
            //Add the value to the existing count
            int existingCount = histogram[mac][ssVal];
            histogram[mac][ssVal] = existingCount + 1;
        }

        public void addValue(String mac, int ssVal, MacInfo macInfo)
        {
            mac = discardLastCharIfMacIsFull(mac);
            if (macInfo != null)
                additionalInfo.Add(mac, macInfo);
            addValue(mac, ssVal);
        }

        public bool containsMac(String mac)
        {
            return histogram.ContainsKey(mac);
        }
            
        //We are not too concerned with decimal numbers, so we return the avg as an int
        //(Moreover, this is a legacy from the WinMobile client and the Streamspin server)
        //TODO: Tak a look at his - does not take negative numbers into account
        public int getAvgDbM(String mac)
        {
            int totalVal = 0;
            int totalCount = 0; //the total number of distinct values
            int curCount = 0;
            //calculate the total
            foreach (int curVal in histogram[mac].Keys)
            {
                curCount = histogram[mac][curVal];
                totalVal += curVal * curCount;
                totalCount += curCount;
            }

            return totalVal / totalCount;            
        }
        public Dictionary<int, int> GetHistogram(String mac)
        {
            return histogram[mac];
        }
        public Dictionary<String, Dictionary<int, int>> getHistograms() {
            return this.histogram;
        }
        public MacInfo getMacInfo(String mac)
        {
            return additionalInfo[mac];
        }
   
        public Dictionary<String, MacInfo> getMacInfos() {
            return this.additionalInfo;
        }
    
        public HashSet<String> getMACs()
        {
            HashSet<String> result = new HashSet<string>(); 
            foreach (String mac in this.histogram.Keys)
                result.Add(mac);
            return result;
        }
    
        public DateTime getMeasTimeEnd()
        {
            return measTimeEnd;
        }
                
        //The time the measurement was taken
        public DateTime getMeasTimeStart()
        {
            return measTimeStart;
        }
    
        public int getNumMACs()
        {
            return histogram.Keys.Count;
        }   
    
        public double GetStdDev(String mac)
        {
            double total = 0;
            int mean = this.getAvgDbM(mac);
            int allValues = 0;

            foreach (int val in histogram[mac].Keys) //val represents each of the distinct recorded values
            {
                int numVals = histogram[mac][val]; //the number of times the given val occurs
                allValues += numVals;

                for (int i = 1; i <= numVals; i++)
                {
                    total += Math.Pow(val - mean, 2);
                }
            }
            return Math.Sqrt(total / allValues);            
        }

        public int getStrongestDbM(String mac)
        {
            int max = -255; //lowest possible RSSI value
            foreach (int ss in histogram[mac].Keys)
                if (ss > max)
                    max = ss;
            return max;            
        }
            
        public int getWeakestDbM(String mac)
        {
            int min = 0; //largets possible RSSI value
            foreach (int ss in histogram[mac].Keys)
                if (ss < min)
                    min = ss;
            return min;
        }
        
        public void removeMac(String mac)
        {
            if (histogram.ContainsKey(mac))
                histogram.Remove(mac);
            if (additionalInfo.ContainsKey(mac))
                additionalInfo.Remove(mac);
        }
    
        public void removeMacs(List<String> macs)
        {
            if (macs == null)
                return;
        
            for (int i = 0; i < macs.Count; i++)
            {
                this.removeMac(macs[i]);
            }
        }
     
        public void setHistogram(Histogram hist)
        {
            histogram.Add(hist.getMac(), new Dictionary<int, int>());
            histogram[hist.getMac()].Add(hist.getValue(), hist.getCount());
        }
    
        public void setMeasTimeEnd(DateTime time)
        {
            this.measTimeEnd = time;
        }
    
        public void setMeasTimeStart(DateTime time)
        {
            this.measTimeStart = time;
        }    
    }
}
