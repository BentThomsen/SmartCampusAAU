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

public class SymbolicLocation
{
	//WARNING: You do NOT EVER change the order!!! Only add
	public enum InfoType 
	{
		NONE,
		OFFICE,
		DEFIBRELLATOR,
		FIRST_AID_KIT,
		FIRE_EXTINGUISHER,
		TOILET, 
		FOOD,
		LECTURE_ROOM, 
		STJERNE_DAG; //kun relevant for stjernedag
		
		/**
		 * @param index Index of desired infotype value
		 * @return Returns the type corresponding to the specified index.
		 * If no type is found, the default (NONE) is returned.  
		 */
	    public static InfoType getValue(int index)
	    {
	    	int i = 0;
	    	for (InfoType it : InfoType.values())
	    	{
	    		if (i == index)
	    			return it;
	    		i++;
	    	}
	    	return NONE;
	    }
	    
	    public static String prettyPrint(InfoType uglybuggly)
	    {
	    	String res = "None";
	    	switch (uglybuggly)
	    	{
	    	case DEFIBRELLATOR: 
	    		res = "Defibrellator";
	    		break;
	    	case FIRE_EXTINGUISHER:
	    		res = "Fire Extinguisher";
	    		break;
	    	case FIRST_AID_KIT:
	    		res = "First Aid Kit";
	    		break;
	    	case OFFICE:
	    		res = "Office";
	    		break;
	    	case TOILET:
	    		res = "Toilet";
	    		break;
	    	case FOOD:
	    		res = "Food";
	    		break;
	    	case LECTURE_ROOM:
	    		res = "Lecture Room";
	    		break;	
	    	case STJERNE_DAG:
	    		res = "Stjerne Stand";
	    		break;
	    	default: 
	    		res = "None";
	    		break;
	    	}
	    	return res;
	    }
	}
	
	private String title;
    private String description;
    private String url;
    private boolean isEntrance;
    private int ID;
    private InfoType type;
    
    public SymbolicLocation()
    {
    	this(-1, "No Title", "No Description", "No Url");
    }
    
    public SymbolicLocation(int id, String title, String description, String url)
    {
    	this.description = description;
    	this.title = title;
    	this.url = url;
    	this.ID = id;
    }
    
    public SymbolicLocation(String title, String description, String url)
    {
    	this(-1, title, description, url);
    }
    
    public String getDescription()
    {
        return description;
    }
    
    public int getId() {
    	return this.ID;
    }
    
    public String getTitle()
    {
    	return this.title;
    }
    
    public InfoType getType()
    {
    	return this.type;
    }
    
    public String getUrl()
    {
    	return this.url;
    }
    
    public boolean isEntrance()
    {
    	return this.isEntrance;
    }

    public void setDescription(String value)
    {
        description = value; 
    }
    
    public void setEntrance(boolean isEntrance)
    {
    	this.isEntrance = isEntrance;
    }
    
    public void setId(int value)
    {
    	this.ID = value;
    }
    
    public void setTitle(String value)
    {
    	this.title = value;
    }
    
    public void setType(InfoType type)
    {
    	this.type = type;
    }    
    
    public void setUrl(String value)
    {
    	this.url = value;
    }
}