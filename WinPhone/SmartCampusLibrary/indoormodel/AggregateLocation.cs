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

namespace com.smartcampus.indoormodel
{
    public class AggregateLocation
    {        
	    private SymbolicLocation symbolicLocation;
	    //This is a required location format. Used to uniquely identify a location
	    private AbsoluteLocation absoluteLocation;
	
	    public AggregateLocation()
	    {
		
	    }
	
	    public AggregateLocation(AbsoluteLocation absoluteLocation)
	    {
		    this.absoluteLocation = absoluteLocation;
	    }
	
	    public AggregateLocation(AbsoluteLocation absoluteLocation, SymbolicLocation symbolicLocation)
	    {
		    this.absoluteLocation = absoluteLocation;
		    this.symbolicLocation = symbolicLocation;
	    }
	
	    /*
         * (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         * We compare AggregateLocations by their absoluteLocation
         * which is the only required location
         */
    
        /// <summary>
        /// We compare AggregateLocations by their absoluteLocation which is the only required location
        /// </summary>
        /// <param name="other"></param>
        /// <returns></returns>
        public override bool Equals(Object other)
        {
    	    if (other == null)
    		    return false;
    	
    	    if (!(other is AggregateLocation))
    		    return false;
    	
    	    AbsoluteLocation otherAbsLocation = ((AggregateLocation)other).getAbsoluteLocation();
    	    AbsoluteLocation thisAbsLocation = this.getAbsoluteLocation();
    	    return thisAbsLocation.Equals(otherAbsLocation);    	
        }

	    public AbsoluteLocation getAbsoluteLocation()
	    {
		    return this.absoluteLocation;
	    }
	
	    public SymbolicLocation getSymbolicLocation()
	    {
		    return symbolicLocation;
	    }
	

        public override int GetHashCode()
        {
    	    return this.getAbsoluteLocation().GetHashCode();
        }
	
	    public void setAbsoluteLocation(AbsoluteLocation location)
	    {
		    this.absoluteLocation = location;
	    }
	
	    public void setSymbolicLocation(SymbolicLocation location)
	    {
		    this.symbolicLocation = location;
	    }
    }
}
