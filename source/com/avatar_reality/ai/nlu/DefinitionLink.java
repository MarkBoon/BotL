package com.avatar_reality.ai.nlu;

public class DefinitionLink
{
	private LinkType type;
	private String wordDefinitionId;

	public LinkType getType()
	{
		return type;
	}
	public void setType(LinkType type) 
	{
		this.type = type;
	}
	public String getWordDefinitionId() 
	{
		return wordDefinitionId;
	}
	public void setWordDefinitionId(String wordDefinitionId) 
	{
		this.wordDefinitionId = wordDefinitionId;
	}
}
