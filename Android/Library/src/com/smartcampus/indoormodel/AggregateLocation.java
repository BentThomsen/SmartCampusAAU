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

package com.smartcampus.indoormodel;

public class AggregateLocation {
	private PixelLocation pixelLocation;
	private RelativeLocation relativeLocation;
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
	
	public AggregateLocation(AbsoluteLocation absoluteLocation, RelativeLocation relativeLocation, SymbolicLocation symbolicLocation, PixelLocation pixelLocation)
	{
		this.absoluteLocation = absoluteLocation;
		this.relativeLocation = relativeLocation;
		this.symbolicLocation = symbolicLocation;
		this.pixelLocation = pixelLocation;
	}
	
	/*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     * We compare AggregateLocations by their absoluteLocation
     * which is the only required location
     */
    @Override
    public boolean equals(Object other)
    {
    	if (other == null)
    		return false;
    	
    	if (!(other instanceof AggregateLocation))
    		return false;
    	
    	AbsoluteLocation otherAbsLocation = ((AggregateLocation)other).getAbsoluteLocation();
    	AbsoluteLocation thisAbsLocation = this.getAbsoluteLocation();
    	return thisAbsLocation.equals(otherAbsLocation);    	
    }
	public AbsoluteLocation getAbsoluteLocation()
	{
		return this.absoluteLocation;
	}
	
	public PixelLocation getPixelLocation()
	{
		return pixelLocation;
	}
	public RelativeLocation getRelativeLocation()
	{
		return relativeLocation;
	}
	
	public SymbolicLocation getSymbolicLocation()
	{
		return symbolicLocation;
	}
	@Override
    public int hashCode()
    {
    	return this.getAbsoluteLocation().hashCode();
    }
	
	public void setAbsoluteLocation(AbsoluteLocation location)
	{
		this.absoluteLocation = location;
	}
	
	public void setPixelLocation(PixelLocation location)
	{
		this.pixelLocation = location;
	}
	
	public void setRelativeLocation(RelativeLocation location)
	{
		this.relativeLocation = location;
	}
    
    public void setSymbolicLocation(SymbolicLocation location)
	{
		this.symbolicLocation = location;
	}
}
